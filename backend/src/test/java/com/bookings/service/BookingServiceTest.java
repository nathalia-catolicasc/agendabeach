package com.bookings.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.bookings.dto.BookingResponseDTO;
import com.bookings.dto.CancellationResponseDTO;
import com.bookings.dto.CreateBookingDTO;
import com.bookings.entity.Booking;
import com.bookings.entity.Court;
import com.bookings.enums.BookingStatus;
import com.bookings.enums.CourtStatus;
import com.bookings.enums.CourtType;
import com.bookings.repository.BookingRepository;
import com.bookings.repository.CourtRepository;
import com.users.entity.User;
import com.users.repository.UserRepository;
import com.users.service.AuthService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
class BookingServiceTest {
    private static final LocalDateTime NOW = LocalDateTime.of(2026, 10, 21, 6, 0);
    private static final LocalDateTime START = LocalDateTime.of(2026, 10, 23, 10, 0);

    @Mock private BookingRepository bookingRepository;
    @Mock private UserRepository userRepository;
    @Mock private CourtRepository courtRepository;
    @Mock private PricingService pricingService;
    @Mock private AuthService authService;

    private BookingService bookingService;

    @BeforeEach
    void setup() {
        bookingService =
                new BookingService(
                        bookingRepository,
                        userRepository,
                        courtRepository,
                        pricingService,
                        authService,
                        Clock.fixed(NOW.toInstant(ZoneOffset.UTC), ZoneOffset.UTC));
    }

    @Test
    void create_ShouldPersistAndReturnResponse() {
        prepareUser();
        when(courtRepository.lockById(2L)).thenReturn(Optional.of(activeCourt()));
        when(pricingService.price(START)).thenReturn(new BigDecimal("60.00"));
        when(bookingRepository.saveAndFlush(any(Booking.class)))
                .thenAnswer(
                        invocation -> {
                            Booking booking = invocation.getArgument(0);
                            ReflectionTestUtils.setField(booking, "id", 10L);
                            return booking;
                        });

        BookingResponseDTO response =
                bookingService.create(
                        new CreateBookingDTO(1L, 2L, START, START.plusHours(1), false));

        assertEquals(10L, response.id());
        assertEquals(1L, response.userId());
        assertEquals(2L, response.courtId());
        assertEquals(START, response.startTime());
        assertEquals(START.plusHours(1), response.endTime());
        assertEquals(new BigDecimal("60.00"), response.courtPrice());
        assertEquals(BookingStatus.SCHEDULED, response.status());
    }

    @Test
    void create_ShouldThrow_WhenEndBeforeStart() {
        prepareUser();

        IllegalArgumentException error =
                assertThrows(
                        IllegalArgumentException.class,
                        () ->
                                bookingService.create(
                                        new CreateBookingDTO(
                                                1L, 2L, START, START.minusMinutes(30), false)));

        assertTrue(error.getMessage().toLowerCase().contains("end time"));
        verifyNoInteractions(courtRepository, pricingService);
        verify(bookingRepository, never()).saveAndFlush(any());
    }

    @Test
    void create_ShouldThrow_WhenCourtInactive() {
        prepareUser();
        Court court = activeCourt();
        court.setStatus(CourtStatus.INACTIVE);
        when(courtRepository.lockById(2L)).thenReturn(Optional.of(court));

        IllegalStateException error =
                assertThrows(
                        IllegalStateException.class,
                        () ->
                                bookingService.create(
                                        new CreateBookingDTO(
                                                1L, 2L, START, START.plusHours(1), false)));

        assertEquals("Court is inactive", error.getMessage());
        verifyNoInteractions(pricingService);
        verify(bookingRepository, never()).saveAndFlush(any());
    }

    @Test
    void create_ShouldThrow_WhenUserNotFound() {
        when(authService.currentId()).thenReturn(1L);
        when(userRepository.lockById(1L)).thenReturn(Optional.empty());

        ResponseStatusException error =
                assertThrows(
                        ResponseStatusException.class,
                        () ->
                                bookingService.create(
                                        new CreateBookingDTO(
                                                1L, 2L, START, START.plusHours(1), false)));

        assertEquals(HttpStatus.NOT_FOUND, error.getStatusCode());
        assertEquals("User not found", error.getReason());
        verifyNoInteractions(courtRepository, pricingService);
    }

    @Test
    void create_ShouldThrow_WhenCourtNotFound() {
        prepareUser();
        when(courtRepository.lockById(2L)).thenReturn(Optional.empty());

        ResponseStatusException error =
                assertThrows(
                        ResponseStatusException.class,
                        () ->
                                bookingService.create(
                                        new CreateBookingDTO(
                                                1L, 2L, START, START.plusHours(1), false)));

        assertEquals(HttpStatus.NOT_FOUND, error.getStatusCode());
        assertEquals("Court not found", error.getReason());
        verifyNoInteractions(pricingService);
    }

    @Test
    void create_ShouldApplyPeakPrice_ForOpenCourt() {
        prepareUser();
        when(courtRepository.lockById(2L)).thenReturn(Optional.of(activeCourt()));
        LocalDateTime start = START.withHour(18);
        when(pricingService.price(start)).thenReturn(new BigDecimal("100.00"));
        when(bookingRepository.saveAndFlush(any(Booking.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        bookingService.create(new CreateBookingDTO(1L, 2L, start, start.plusHours(1), false));

        ArgumentCaptor<Booking> captor = ArgumentCaptor.forClass(Booking.class);
        verify(bookingRepository).saveAndFlush(captor.capture());
        assertEquals(new BigDecimal("100.00"), captor.getValue().getCourtPrice());
        verify(pricingService).price(start);
    }

    @Test
    void cancel_ShouldRefundFully_WhenMoreThan24HoursBefore() {
        Booking booking = scheduledBookingStartingIn(30);
        prepareCancellation(booking);

        CancellationResponseDTO response = bookingService.cancel(5L);

        assertEquals(new BigDecimal("100.00"), response.refundAmount());
        assertEquals(0, response.creditAmount().signum());
        assertEquals(BookingStatus.CANCELLED, booking.getStatus());
        assertEquals(NOW, booking.getCancelledAt());
        verify(authService).ensureOwner(1L);
        verify(bookingRepository).saveAndFlush(booking);
    }

    @Test
    void cancel_ShouldGrantCreditWithoutCashRefund_WhenThreeHoursBefore() {
        Booking booking = scheduledBookingStartingIn(3);
        prepareCancellation(booking);

        CancellationResponseDTO response = bookingService.cancel(5L);

        assertEquals(0, response.refundAmount().signum());
        assertEquals(new BigDecimal("50.00"), response.creditAmount());
        assertEquals(new BigDecimal("50.00"), booking.getUser().getCreditBalance());
        assertEquals(BookingStatus.CANCELLED, booking.getStatus());
        verify(bookingRepository).saveAndFlush(booking);
    }

    @Test
    void cancel_ShouldThrow_WhenAlreadyCancelled() {
        Booking booking = scheduledBookingStartingIn(30);
        booking.setStatus(BookingStatus.CANCELLED);
        prepareCancellation(booking);

        IllegalStateException error =
                assertThrows(IllegalStateException.class, () -> bookingService.cancel(5L));

        assertEquals("Only future scheduled bookings can change", error.getMessage());
        assertEquals(0, booking.getUser().getCreditBalance().signum());
        verify(bookingRepository, never()).saveAndFlush(any());
    }

    @Test
    void delete_ShouldThrow_WhenBookingNotFound() {
        when(bookingRepository.ownerId(99L)).thenReturn(Optional.empty());

        ResponseStatusException error =
                assertThrows(ResponseStatusException.class, () -> bookingService.delete(99L));

        assertEquals(HttpStatus.NOT_FOUND, error.getStatusCode());
        assertEquals("Booking not found", error.getReason());
        verify(bookingRepository, never()).deleteById(any());
        verify(bookingRepository, never()).saveAndFlush(any());
        verifyNoInteractions(userRepository);
    }

    private void prepareUser() {
        when(authService.currentId()).thenReturn(1L);
        when(userRepository.lockById(1L)).thenReturn(Optional.of(user()));
    }

    private User user() {
        User user = new User();
        ReflectionTestUtils.setField(user, "id", 1L);
        user.setCpf("52998224725");
        return user;
    }

    private Court activeCourt() {
        Court court = new Court();
        ReflectionTestUtils.setField(court, "id", 2L);
        court.setStatus(CourtStatus.ACTIVE);
        court.setType(CourtType.OPEN);
        return court;
    }

    private Booking scheduledBookingStartingIn(long hours) {
        Booking booking = new Booking();
        ReflectionTestUtils.setField(booking, "id", 5L);
        booking.setUser(user());
        booking.setStartTime(NOW.plusHours(hours));
        booking.setEndTime(booking.getStartTime().plusHours(1));
        booking.setCourtPrice(new BigDecimal("100.00"));
        booking.setStatus(BookingStatus.SCHEDULED);
        return booking;
    }

    private void prepareCancellation(Booking booking) {
        when(bookingRepository.ownerId(5L)).thenReturn(Optional.of(1L));
        when(userRepository.lockById(1L)).thenReturn(Optional.of(booking.getUser()));
        when(bookingRepository.lockById(5L)).thenReturn(Optional.of(booking));
    }
}
