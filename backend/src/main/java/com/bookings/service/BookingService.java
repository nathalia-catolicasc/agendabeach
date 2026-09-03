package com.bookings.service;

import com.bookings.dto.BookingResponseDTO;
import com.bookings.dto.CancellationResponseDTO;
import com.bookings.dto.CreateBookingDTO;
import com.bookings.dto.UpdateBookingDTO;
import com.bookings.entity.Booking;
import com.bookings.enums.BookingStatus;
import com.bookings.entity.Court;
import com.bookings.repository.BookingRepository;
import com.bookings.repository.CourtRepository;
import com.users.entity.User;
import com.users.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class BookingService {

    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final CourtRepository courtRepository;

    public BookingService(
            BookingRepository bookingRepository,
            UserRepository userRepository,
            CourtRepository courtRepository
    ) {
        this.bookingRepository = bookingRepository;
        this.userRepository = userRepository;
        this.courtRepository = courtRepository;
    }

    public BookingResponseDTO create(CreateBookingDTO dto) {

        User user = userRepository.findById(dto.userId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        Court court = courtRepository.findById(dto.courtId())
                .orElseThrow(() -> new RuntimeException("Court not found"));

        if (court.getStatus().name().equals("INACTIVE")) {
            throw new RuntimeException("Court is inactive");
        }

        if (!dto.endTime().isAfter(dto.startTime())) {
            throw new RuntimeException("End time must be after start time");
        }

        Booking booking = new Booking();

        booking.setUser(user);
        booking.setCourt(court);
        booking.setStartTime(dto.startTime());
        booking.setEndTime(dto.endTime());
        booking.setStatus(BookingStatus.SCHEDULED);
        booking.setCreatedAt(LocalDateTime.now());
        booking.setCourtPrice(BigDecimal.ZERO);

        Booking savedBooking = bookingRepository.save(booking);

        return new BookingResponseDTO(
                savedBooking.getId(),
                savedBooking.getUser().getId(),
                savedBooking.getCourt().getId(),
                savedBooking.getStartTime(),
                savedBooking.getEndTime(),
                savedBooking.getCourtPrice(),
                savedBooking.getStatus()
        );
    }

    public List<BookingResponseDTO> listAll() {
        return bookingRepository.findAll().stream()
                .map(b -> new BookingResponseDTO(
                        b.getId(),
                        b.getUser().getId(),
                        b.getCourt().getId(),
                        b.getStartTime(),
                        b.getEndTime(),
                        b.getCourtPrice(),
                        b.getStatus()
                ))
                .collect(Collectors.toList());
    }

    public List<BookingResponseDTO> listByDate(LocalDate date) {
        LocalDateTime start = date.atStartOfDay();
        LocalDateTime end = date.plusDays(1).atStartOfDay();
        return bookingRepository.findAllByStartTimeBetween(start, end).stream()
                .map(b -> new BookingResponseDTO(
                        b.getId(),
                        b.getUser().getId(),
                        b.getCourt().getId(),
                        b.getStartTime(),
                        b.getEndTime(),
                        b.getCourtPrice(),
                        b.getStatus()
                ))
                .collect(Collectors.toList());
    }

    public List<BookingResponseDTO> listByDateAndCourt(LocalDate date, Long courtId) {
        LocalDateTime start = date.atStartOfDay();
        LocalDateTime end = date.plusDays(1).atStartOfDay();
        return bookingRepository.findAllByCourt_IdAndStartTimeBetween(courtId, start, end).stream()
                .map(b -> new BookingResponseDTO(
                        b.getId(),
                        b.getUser().getId(),
                        b.getCourt().getId(),
                        b.getStartTime(),
                        b.getEndTime(),
                        b.getCourtPrice(),
                        b.getStatus()
                ))
                .collect(Collectors.toList());
    }

    public List<BookingResponseDTO> listByUser(Long userId) {
        return bookingRepository.findAllByUserId(userId).stream()
                .map(b -> new BookingResponseDTO(
                        b.getId(),
                        b.getUser().getId(),
                        b.getCourt().getId(),
                        b.getStartTime(),
                        b.getEndTime(),
                        b.getCourtPrice(),
                        b.getStatus()
                ))
                .collect(Collectors.toList());
    }

    public BookingResponseDTO getById(Long id) {
        Booking b = bookingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Booking not found"));
        return new BookingResponseDTO(
                b.getId(),
                b.getUser().getId(),
                b.getCourt().getId(),
                b.getStartTime(),
                b.getEndTime(),
                b.getCourtPrice(),
                b.getStatus()
        );
    }

    public BookingResponseDTO update(Long id, UpdateBookingDTO dto) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Booking not found"));

        if (dto.userId() != null) {
            User user = userRepository.findById(dto.userId())
                    .orElseThrow(() -> new RuntimeException("User not found"));
            booking.setUser(user);
        }
        if (dto.courtId() != null) {
            Court court = courtRepository.findById(dto.courtId())
                    .orElseThrow(() -> new RuntimeException("Court not found"));
            if (court.getStatus().name().equals("INACTIVE")) {
                throw new RuntimeException("Court is inactive");
            }
            booking.setCourt(court);
        }
        if (dto.startTime() != null) {
            booking.setStartTime(dto.startTime());
        }
        if (dto.endTime() != null) {
            booking.setEndTime(dto.endTime());
        }

        if (!booking.getEndTime().isAfter(booking.getStartTime())) {
            throw new RuntimeException("End time must be after start time");
        }

        Booking saved = bookingRepository.save(booking);
        return new BookingResponseDTO(
                saved.getId(),
                saved.getUser().getId(),
                saved.getCourt().getId(),
                saved.getStartTime(),
                saved.getEndTime(),
                saved.getCourtPrice(),
                saved.getStatus()
        );
    }

    public void delete(Long id) {
        if (!bookingRepository.existsById(id)) {
            throw new RuntimeException("Booking not found");
        }
        bookingRepository.deleteById(id);
    }

    public CancellationResponseDTO cancel(Long id) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Booking not found"));

        if (booking.getStatus() == BookingStatus.CANCELLED) {
            throw new RuntimeException("Booking already cancelled");
        }
        if (booking.getStatus() == BookingStatus.COMPLETED) {
            throw new RuntimeException("Completed bookings cannot be cancelled");
        }

        LocalDateTime now = LocalDateTime.now();
        if (now.isAfter(booking.getStartTime())) {
            throw new RuntimeException("Cannot cancel a booking that has already started");
        }

        long hoursUntilStart = java.time.Duration.between(now, booking.getStartTime()).toHours();
        BigDecimal price = booking.getCourtPrice() == null ? BigDecimal.ZERO : booking.getCourtPrice();
        BigDecimal refund;
        if (hoursUntilStart >= 24) {
            refund = price; // 100%
        } else if (hoursUntilStart >= 12) {
            refund = price.multiply(new BigDecimal("0.5"));
        } else {
            refund = BigDecimal.ZERO;
        }

        booking.setStatus(BookingStatus.CANCELLED);
        booking.setCancelledAt(now);
        bookingRepository.save(booking);

        return new CancellationResponseDTO(
                booking.getId(),
                refund,
                booking.getStatus().name(),
                booking.getCancelledAt()
        );
    }
}