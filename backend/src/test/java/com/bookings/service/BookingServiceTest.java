package com.bookings.service;

import com.bookings.dto.BookingResponseDTO;
import com.bookings.dto.CreateBookingDTO;
import com.bookings.entity.Booking;
import com.bookings.entity.Court;
import com.bookings.enums.CourtStatus;
import com.bookings.repository.BookingRepository;
import com.bookings.repository.CourtRepository;
import com.users.entity.User;
import com.users.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BookingServiceTest {

    @Mock
    private BookingRepository bookingRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private CourtRepository courtRepository;

    @InjectMocks
    private BookingService bookingService;

    @Test
    void create_ShouldPersistAndReturnResponse() {
        User user = new User();
        Court court = new Court();
        court.setStatus(CourtStatus.ACTIVE);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(courtRepository.findById(2L)).thenReturn(Optional.of(court));
        when(bookingRepository.save(any(Booking.class))).thenAnswer(inv -> {
            Booking b = inv.getArgument(0);
            try {
                var idField = Booking.class.getDeclaredField("id");
                idField.setAccessible(true);
                idField.set(b, 10L);
            } catch (Exception ignored) {}
            return b;
        });

        var start = LocalDateTime.now().plusHours(1);
        var end = start.plusHours(1);
        CreateBookingDTO dto = new CreateBookingDTO(1L, 2L, start, end);

        BookingResponseDTO res = bookingService.create(dto);

        assertNotNull(res);
        assertEquals(10L, res.id());
        assertEquals(1L, res.userId());
        assertEquals(2L, res.courtId());
        assertEquals(start, res.startTime());
        assertEquals(end, res.endTime());
        assertNotNull(res.status());
    }

    @Test
    void create_ShouldThrow_WhenEndBeforeStart() {
        User user = new User();
        Court court = new Court();
        court.setStatus(CourtStatus.ACTIVE);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(courtRepository.findById(2L)).thenReturn(Optional.of(court));

        var start = LocalDateTime.now().plusHours(2);
        var end = start.minusMinutes(30);
        CreateBookingDTO dto = new CreateBookingDTO(1L, 2L, start, end);

        RuntimeException ex = assertThrows(RuntimeException.class, () -> bookingService.create(dto));
        assertTrue(ex.getMessage().toLowerCase().contains("end time"));
    }

    @Test
    void create_ShouldThrow_WhenCourtInactive() {
        User user = new User();
        Court court = new Court();
        court.setStatus(CourtStatus.INACTIVE);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(courtRepository.findById(2L)).thenReturn(Optional.of(court));

        var start = LocalDateTime.now().plusHours(1);
        var end = start.plusHours(1);
        CreateBookingDTO dto = new CreateBookingDTO(1L, 2L, start, end);

        RuntimeException ex = assertThrows(RuntimeException.class, () -> bookingService.create(dto));
        assertTrue(ex.getMessage().toLowerCase().contains("inactive"));
    }
}
