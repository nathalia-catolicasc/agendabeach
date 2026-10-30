package com.bookings.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.bookings.dto.*;
import com.bookings.entity.*;
import com.bookings.enums.*;
import com.bookings.repository.*;
import com.users.entity.User;
import com.users.repository.UserRepository;
import com.users.service.AuthService;

import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.*;
import java.util.*;

class BookingRulesTest {
    BookingRepository bookings;
    UserRepository users;
    CourtRepository courts;
    PricingService prices;
    AuthService auth;
    BookingService service;
    User user;
    Court court;
    LocalDateTime now = LocalDateTime.of(2026, 10, 21, 6, 0);

    @BeforeEach
    void setup() {
        bookings = mock(BookingRepository.class);
        users = mock(UserRepository.class);
        courts = mock(CourtRepository.class);
        prices = mock(PricingService.class);
        auth = mock(AuthService.class);
        service =
                new BookingService(
                        bookings,
                        users,
                        courts,
                        prices,
                        auth,
                        Clock.fixed(now.toInstant(ZoneOffset.UTC), ZoneOffset.UTC));
        user = new User();
        user.setCpf("52998224725");
        ReflectionTestUtils.setField(user, "id", 1L);
        court = new Court();
        court.setStatus(CourtStatus.ACTIVE);
        ReflectionTestUtils.setField(court, "id", 2L);
        when(auth.currentId()).thenReturn(1L);
        when(users.lockById(1L)).thenReturn(Optional.of(user));
        when(courts.lockById(2L)).thenReturn(Optional.of(court));
        when(bookings.overlapping(any(), any(), any())).thenReturn(List.of());
        when(prices.price(any())).thenReturn(new BigDecimal("60.00"));
        when(bookings.saveAndFlush(any())).thenAnswer(i -> i.getArgument(0));
    }

    @Test
    void createsOneHourBookingAndConsumesCreditOnlyWhenRequested() {
        user.setCreditBalance(new BigDecimal("80.00"));
        var start = now.plusDays(2);
        var response =
                service.create(new CreateBookingDTO(1L, 2L, start, start.plusHours(1), true));
        assertEquals(new BigDecimal("60.00"), response.courtPrice());
        assertEquals(new BigDecimal("20.00"), user.getCreditBalance());
        verify(bookings)
                .saveAndFlush(argThat(b -> b.getCreditUsed().equals(new BigDecimal("60.00"))));
    }

    @ParameterizedTest
    @CsvSource({"5,60", "6,90", "22,120", "23,60"})
    void rejectsInvalidHoursAndDurations(int hour, int minutes) {
        var start = now.plusDays(2).withHour(hour);
        assertThrows(
                IllegalArgumentException.class,
                () ->
                        service.create(
                                new CreateBookingDTO(
                                        1L, 2L, start, start.plusMinutes(minutes), false)));
        verify(bookings, never()).saveAndFlush(any());
    }

    @Test
    void acceptsLastSlotAndRejectsPastAndFractionalSlots() {
        var start = now.plusDays(2).withHour(22);
        assertNotNull(
                service.create(new CreateBookingDTO(1L, 2L, start, start.plusHours(1), false)));
        assertThrows(
                IllegalArgumentException.class,
                () -> service.validateSlot(now.minusDays(1), now.minusDays(1).plusHours(1)));
        assertThrows(
                IllegalArgumentException.class,
                () -> service.validateSlot(start.plusMinutes(30), start.plusMinutes(90)));
    }

    @Test
    void enforcesQuota() {
        when(bookings.countByUserIdAndStatusAndStartTimeAfter(
                        eq(1L), eq(BookingStatus.SCHEDULED), any()))
                .thenReturn(3L);
        assertThrows(
                IllegalStateException.class,
                () ->
                        service.create(
                                new CreateBookingDTO(
                                        1L,
                                        2L,
                                        now.plusDays(1),
                                        now.plusDays(1).plusHours(1),
                                        false)));
    }

    @Test
    void rejectsInactiveCourtAndMissingCpf() {
        court.setStatus(CourtStatus.INACTIVE);
        assertThrows(
                IllegalStateException.class,
                () ->
                        service.create(
                                new CreateBookingDTO(
                                        1L,
                                        2L,
                                        now.plusDays(1),
                                        now.plusDays(1).plusHours(1),
                                        false)));
        user.setCpf(null);
        assertThrows(
                IllegalStateException.class,
                () ->
                        service.create(
                                new CreateBookingDTO(
                                        1L,
                                        2L,
                                        now.plusDays(1),
                                        now.plusDays(1).plusHours(1),
                                        false)));
    }

    @ParameterizedTest
    @CsvSource({"86401,100.00,0.00", "86400,0.00,50.00", "7200,0.00,50.00", "7199,0.00,0.00"})
    void cancellationBoundaries(long seconds, String refund, String credit) {
        Booking b = new Booking();
        b.setUser(user);
        b.setStatus(BookingStatus.SCHEDULED);
        b.setStartTime(now.plusSeconds(seconds));
        b.setCourtPrice(new BigDecimal("100.00"));
        when(bookings.ownerId(10L)).thenReturn(Optional.of(1L));
        when(bookings.lockById(10L)).thenReturn(Optional.of(b));
        var result = service.cancel(10L);
        assertEquals(new BigDecimal(refund), result.refundAmount());
        assertEquals(new BigDecimal(credit), result.creditAmount());
        assertEquals(new BigDecimal(credit), user.getCreditBalance());
        assertThrows(IllegalStateException.class, () -> service.cancel(10L));
    }

    @Test
    void fullCancellationRestoresCreditAndRefundsOnlyCashPortion() {
        Booking b = new Booking();
        b.setUser(user);
        b.setStatus(BookingStatus.SCHEDULED);
        b.setStartTime(now.plusDays(2));
        b.setCourtPrice(new BigDecimal("100.00"));
        b.setCreditUsed(new BigDecimal("40.00"));
        when(bookings.ownerId(10L)).thenReturn(Optional.of(1L));
        when(bookings.lockById(10L)).thenReturn(Optional.of(b));
        var result = service.cancel(10L);
        assertEquals(new BigDecimal("60.00"), result.refundAmount());
        assertEquals(new BigDecimal("40.00"), result.creditAmount());
    }
}
