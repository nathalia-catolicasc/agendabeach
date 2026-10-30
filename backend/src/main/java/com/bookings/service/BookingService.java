package com.bookings.service;

import com.bookings.dto.*;
import com.bookings.entity.*;
import com.bookings.enums.*;
import com.bookings.repository.*;
import com.users.entity.User;
import com.users.repository.UserRepository;
import com.users.service.AuthService;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.*;
import java.time.*;
import java.util.*;

@Service
@Transactional
public class BookingService {
    private final BookingRepository bookings;
    private final UserRepository users;
    private final CourtRepository courts;
    private final PricingService prices;
    private final AuthService auth;
    private final Clock clock;

    public BookingService(
            BookingRepository bookings,
            UserRepository users,
            CourtRepository courts,
            PricingService prices,
            AuthService auth,
            Clock clock) {
        this.bookings = bookings;
        this.users = users;
        this.courts = courts;
        this.prices = prices;
        this.auth = auth;
        this.clock = clock;
    }

    public BookingResponseDTO create(CreateBookingDTO dto) {
        Long current = auth.currentId();
        if (dto.userId() != null && !dto.userId().equals(current)) auth.ensureAdmin();
        Long userId = dto.userId() == null ? current : dto.userId();
        // The user lock serializes the quota and wallet across different courts and API instances.
        User user = users.lockById(userId).orElseThrow(() -> missing("User"));
        if (user.getCpf() == null)
            throw new IllegalStateException("Register a valid CPF before booking");
        validateSlot(dto.startTime(), dto.endTime());
        if (bookings.countByUserIdAndStatusAndStartTimeAfter(userId, BookingStatus.SCHEDULED, now())
                >= 3) throw new IllegalStateException("Maximum of three future bookings per CPF");
        Court court = courts.lockById(dto.courtId()).orElseThrow(() -> missing("Court"));
        validateCourt(court);
        ensureVacant(court.getId(), dto.startTime(), dto.endTime(), null);
        Booking booking = new Booking();
        booking.setUser(user);
        booking.setCourt(court);
        booking.setStartTime(dto.startTime());
        booking.setEndTime(dto.endTime());
        booking.setStatus(BookingStatus.SCHEDULED);
        booking.setCreatedAt(now());
        BigDecimal price = prices.price(dto.startTime());
        booking.setCourtPrice(price);
        if (Boolean.TRUE.equals(dto.useCredit())) {
            BigDecimal used = user.getCreditBalance().min(price);
            user.setCreditBalance(user.getCreditBalance().subtract(used));
            booking.setCreditUsed(used);
        }
        return response(bookings.saveAndFlush(booking));
    }

    public BookingResponseDTO update(Long id, UpdateBookingDTO dto) {
        Long owner = bookings.ownerId(id).orElseThrow(() -> missing("Booking"));
        auth.ensureOwner(owner);
        User user = users.lockById(owner).orElseThrow();
        Booking b = bookings.lockById(id).orElseThrow(() -> missing("Booking"));
        requireFutureScheduled(b);
        if (Duration.between(now(), b.getStartTime()).compareTo(Duration.ofHours(24)) <= 0)
            throw new IllegalStateException(
                    "Rescheduling requires more than 24 hours notice; cancel and rebook");
        if (dto.userId() != null && !dto.userId().equals(user.getId()))
            throw new IllegalArgumentException("Booking owner cannot change");
        if (b.getCreditUsed().signum() != 0)
            throw new IllegalStateException("Cancel and rebook reservations paid with credit");
        LocalDateTime start = dto.startTime() == null ? b.getStartTime() : dto.startTime();
        LocalDateTime end = dto.endTime() == null ? b.getEndTime() : dto.endTime();
        validateSlot(start, end);
        Long courtId = dto.courtId() == null ? b.getCourt().getId() : dto.courtId();
        Court court = courts.lockById(courtId).orElseThrow(() -> missing("Court"));
        validateCourt(court);
        ensureVacant(courtId, start, end, id);
        if (prices.price(start).compareTo(b.getCourtPrice()) != 0)
            throw new IllegalStateException("Price changes require cancellation and a new booking");
        b.setCourt(court);
        b.setStartTime(start);
        b.setEndTime(end);
        return response(bookings.saveAndFlush(b));
    }

    public CancellationResponseDTO cancel(Long id) {
        Long owner = bookings.ownerId(id).orElseThrow(() -> missing("Booking"));
        auth.ensureOwner(owner);
        User user = users.lockById(owner).orElseThrow();
        Booking b = bookings.lockById(id).orElseThrow(() -> missing("Booking"));
        requireFutureScheduled(b);
        Duration remaining = Duration.between(now(), b.getStartTime());
        BigDecimal refund = BigDecimal.ZERO.setScale(2);
        BigDecimal credit = BigDecimal.ZERO.setScale(2);
        if (remaining.compareTo(Duration.ofHours(24)) > 0) {
            refund = b.getCourtPrice().subtract(b.getCreditUsed());
            credit = b.getCreditUsed().setScale(2);
        } else if (remaining.compareTo(Duration.ofHours(2)) >= 0) {
            credit =
                    b.getCourtPrice()
                            .multiply(new BigDecimal("0.50"))
                            .setScale(2, RoundingMode.HALF_UP);
        }
        b.setRefundAmount(refund);
        b.setCancellationCredit(credit);
        user.setCreditBalance(user.getCreditBalance().add(credit));
        b.setStatus(BookingStatus.CANCELLED);
        b.setCancelledAt(now());
        bookings.saveAndFlush(b);
        return new CancellationResponseDTO(
                id, refund, credit, b.getStatus().name(), b.getCancelledAt());
    }

    // Preserve cancellation rules and its audit trail instead of physically deleting reservations.
    public void delete(Long id) {
        cancel(id);
    }

    @Transactional(readOnly = true)
    public BookingResponseDTO getById(Long id) {
        return response(owned(id));
    }

    @Transactional(readOnly = true)
    public List<BookingResponseDTO> listAll() {
        auth.ensureAdmin();
        return bookings.findAll().stream().map(this::response).toList();
    }

    @Transactional(readOnly = true)
    public List<BookingResponseDTO> listByUser(Long id) {
        auth.ensureOwner(id);
        return bookings.findAllByUserId(id).stream().map(this::response).toList();
    }

    @Transactional(readOnly = true)
    public List<BookingResponseDTO> listByDate(LocalDate date) {
        auth.ensureAdmin();
        return bookings
                .findAllByStartTimeBetween(date.atStartOfDay(), date.plusDays(1).atStartOfDay())
                .stream()
                .map(this::response)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<BookingResponseDTO> listByDateAndCourt(LocalDate date, Long court) {
        auth.ensureAdmin();
        return bookings
                .findAllByCourt_IdAndStartTimeBetween(
                        court, date.atStartOfDay(), date.plusDays(1).atStartOfDay())
                .stream()
                .map(this::response)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<AvailabilityDTO> availability(LocalDate date, Long courtId) {
        Court court = courts.findById(courtId).orElseThrow(() -> missing("Court"));
        var occupied = bookings.overlapping(courtId, date.atTime(6, 0), date.atTime(23, 0));
        var slots = new ArrayList<AvailabilityDTO>();
        var dayPrices = prices.dayPrices(date);
        for (int hour = 6; hour < 23; hour++) {
            var start = date.atTime(hour, 0);
            var end = start.plusHours(1);
            boolean free =
                    court.getStatus() == CourtStatus.ACTIVE
                            && start.isAfter(now())
                            && occupied.stream()
                                    .noneMatch(
                                            b ->
                                                    b.getStartTime().isBefore(end)
                                                            && b.getEndTime().isAfter(start));
            slots.add(new AvailabilityDTO(courtId, start, end, free, dayPrices.get(hour - 6)));
        }
        return slots;
    }

    private Booking owned(Long id) {
        Booking b = bookings.findById(id).orElseThrow(() -> missing("Booking"));
        auth.ensureOwner(b.getUser().getId());
        return b;
    }

    private void requireFutureScheduled(Booking b) {
        if (b.getStatus() != BookingStatus.SCHEDULED || !b.getStartTime().isAfter(now()))
            throw new IllegalStateException("Only future scheduled bookings can change");
    }

    private void validateCourt(Court court) {
        if (court.getStatus() != CourtStatus.ACTIVE)
            throw new IllegalStateException("Court is inactive");
    }

    public void validateSlot(LocalDateTime start, LocalDateTime end) {
        if (start == null || end == null || !end.equals(start.plusHours(1)))
            throw new IllegalArgumentException(
                    "End time must be exactly one hour after start time");
        if (!start.toLocalDate().equals(end.toLocalDate())
                || start.getHour() < 6
                || end.toLocalTime().isAfter(LocalTime.of(23, 0))
                || start.getMinute() != 0
                || start.getSecond() != 0
                || start.getNano() != 0)
            throw new IllegalArgumentException(
                    "Bookings require whole-hour slots from 06:00 to 23:00");
        if (!start.isAfter(now()))
            throw new IllegalArgumentException("Booking must start in the future");
    }

    private void ensureVacant(Long court, LocalDateTime start, LocalDateTime end, Long exclude) {
        if (bookings.overlapping(court, start, end).stream()
                .anyMatch(b -> !Objects.equals(b.getId(), exclude)))
            throw new IllegalStateException("Court already booked for this interval");
    }

    private LocalDateTime now() {
        return LocalDateTime.now(clock);
    }

    private ResponseStatusException missing(String type) {
        return new ResponseStatusException(HttpStatus.NOT_FOUND, type + " not found");
    }

    private BookingResponseDTO response(Booking b) {
        return new BookingResponseDTO(
                b.getId(),
                b.getUser().getId(),
                b.getCourt().getId(),
                b.getStartTime(),
                b.getEndTime(),
                b.getCourtPrice(),
                b.getCreditUsed(),
                b.getCourtPrice().subtract(b.getCreditUsed()),
                b.getStatus());
    }
}
