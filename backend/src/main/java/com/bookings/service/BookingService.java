package com.bookings.service;

import static com.bookings.service.BusinessHours.*;

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
    static final String USER_NOT_FOUND = "Usuário não encontrado";
    static final String COURT_NOT_FOUND = "Quadra não encontrada";
    static final String BOOKING_NOT_FOUND = "Reserva não encontrada";
    static final String COURT_INACTIVE = "Quadra inativa";
    static final String NOT_CHANGEABLE = "Apenas reservas futuras e agendadas podem ser alteradas";
    static final String INVALID_DURATION = "A reserva deve ter exatamente 1 hora de duração";

    private static final int MAX_FUTURE_BOOKINGS = 3;
    private static final Duration FULL_REFUND_NOTICE = Duration.ofHours(24);
    private static final Duration CREDIT_NOTICE = Duration.ofHours(2);
    private static final BigDecimal CREDIT_RATE = new BigDecimal("0.50");
    private static final BigDecimal ZERO = BigDecimal.ZERO.setScale(2);

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
        User user = users.lockById(userId).orElseThrow(() -> missing(USER_NOT_FOUND));
        if (user.getCpf() == null)
            throw new IllegalStateException("Cadastre um CPF válido antes de reservar");
        validateSlot(dto.startTime(), dto.endTime());
        if (bookings.countByUserIdAndStatusAndStartTimeAfter(userId, BookingStatus.SCHEDULED, now())
                >= MAX_FUTURE_BOOKINGS)
            throw new IllegalStateException("Limite de três reservas futuras por CPF atingido");
        Court court = courts.lockById(dto.courtId()).orElseThrow(() -> missing(COURT_NOT_FOUND));
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
        Long owner = bookings.ownerId(id).orElseThrow(() -> missing(BOOKING_NOT_FOUND));
        auth.ensureOwner(owner);
        User user = users.lockById(owner).orElseThrow(() -> missing(USER_NOT_FOUND));
        Booking booking = bookings.lockById(id).orElseThrow(() -> missing(BOOKING_NOT_FOUND));
        requireFutureScheduled(booking);
        if (Duration.between(now(), booking.getStartTime()).compareTo(FULL_REFUND_NOTICE) <= 0)
            throw new IllegalStateException(
                    "Remarcação exige mais de 24 horas de antecedência; cancele e faça uma nova"
                            + " reserva");
        if (dto.userId() != null && !dto.userId().equals(user.getId()))
            throw new IllegalArgumentException("O titular da reserva não pode ser alterado");
        if (booking.getCreditUsed().signum() != 0)
            throw new IllegalStateException(
                    "Reservas pagas com crédito não podem ser remarcadas; cancele e faça uma nova"
                            + " reserva");
        LocalDateTime start = dto.startTime() == null ? booking.getStartTime() : dto.startTime();
        LocalDateTime end = dto.endTime() == null ? booking.getEndTime() : dto.endTime();
        validateSlot(start, end);
        Long courtId = dto.courtId() == null ? booking.getCourt().getId() : dto.courtId();
        Court court = courts.lockById(courtId).orElseThrow(() -> missing(COURT_NOT_FOUND));
        validateCourt(court);
        ensureVacant(courtId, start, end, id);
        if (prices.price(start).compareTo(booking.getCourtPrice()) != 0)
            throw new IllegalStateException(
                    "O novo horário tem outro preço; cancele e faça uma nova reserva");
        booking.setCourt(court);
        booking.setStartTime(start);
        booking.setEndTime(end);
        return response(bookings.saveAndFlush(booking));
    }

    public CancellationResponseDTO cancel(Long id) {
        Long owner = bookings.ownerId(id).orElseThrow(() -> missing(BOOKING_NOT_FOUND));
        auth.ensureOwner(owner);
        User user = users.lockById(owner).orElseThrow(() -> missing(USER_NOT_FOUND));
        Booking booking = bookings.lockById(id).orElseThrow(() -> missing(BOOKING_NOT_FOUND));
        requireFutureScheduled(booking);
        CancellationPolicyDTO policy = cancellationPolicy(booking);
        booking.setRefundAmount(policy.refundAmount());
        booking.setCancellationCredit(policy.creditAmount());
        user.setCreditBalance(user.getCreditBalance().add(policy.creditAmount()));
        booking.setStatus(BookingStatus.CANCELLED);
        booking.setCancelledAt(now());
        bookings.saveAndFlush(booking);
        return new CancellationResponseDTO(
                id,
                policy.refundAmount(),
                policy.creditAmount(),
                booking.getStatus().name(),
                booking.getCancelledAt());
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
        Court court = courts.findById(courtId).orElseThrow(() -> missing(COURT_NOT_FOUND));
        var occupied =
                bookings.overlapping(
                        courtId, date.atTime(OPENING_HOUR, 0), date.atTime(CLOSING_HOUR, 0));
        var slots = new ArrayList<AvailabilityDTO>();
        var dayPrices = prices.dayPrices(date);
        for (int hour = OPENING_HOUR; hour < CLOSING_HOUR; hour++) {
            var start = date.atTime(hour, 0);
            var end = start.plusHours(1);
            boolean free =
                    court.getStatus() == CourtStatus.ACTIVE
                            && start.isAfter(now())
                            && occupied.stream()
                                    .noneMatch(
                                            other ->
                                                    other.getStartTime().isBefore(end)
                                                            && other.getEndTime().isAfter(start));
            slots.add(
                    new AvailabilityDTO(
                            courtId, start, end, free, dayPrices.get(hour - OPENING_HOUR)));
        }
        return slots;
    }

    /**
     * RN04: mais de 24h devolve tudo (dinheiro e o crédito usado); de 24h até 2h devolve 50% em
     * crédito; menos de 2h não devolve nada. É a mesma regra usada em {@link #cancel(Long)}.
     */
    private CancellationPolicyDTO cancellationPolicy(Booking booking) {
        Duration remaining = Duration.between(now(), booking.getStartTime());
        LocalDateTime refundDeadline = booking.getStartTime().minus(FULL_REFUND_NOTICE);
        LocalDateTime creditDeadline = booking.getStartTime().minus(CREDIT_NOTICE);
        if (remaining.compareTo(FULL_REFUND_NOTICE) > 0)
            return new CancellationPolicyDTO(
                    CancellationType.REFUND,
                    new BigDecimal("100"),
                    booking.getCourtPrice().subtract(booking.getCreditUsed()),
                    booking.getCreditUsed().setScale(2),
                    refundDeadline,
                    creditDeadline);
        if (remaining.compareTo(CREDIT_NOTICE) >= 0)
            return new CancellationPolicyDTO(
                    CancellationType.CREDIT,
                    new BigDecimal("50"),
                    ZERO,
                    booking.getCourtPrice().multiply(CREDIT_RATE).setScale(2, RoundingMode.HALF_UP),
                    refundDeadline,
                    creditDeadline);
        return new CancellationPolicyDTO(
                CancellationType.NONE, BigDecimal.ZERO, ZERO, ZERO, refundDeadline, creditDeadline);
    }

    private Booking owned(Long id) {
        Booking booking = bookings.findById(id).orElseThrow(() -> missing(BOOKING_NOT_FOUND));
        auth.ensureOwner(booking.getUser().getId());
        return booking;
    }

    private boolean isFutureScheduled(Booking booking) {
        return booking.getStatus() == BookingStatus.SCHEDULED
                && booking.getStartTime().isAfter(now());
    }

    private void requireFutureScheduled(Booking booking) {
        if (!isFutureScheduled(booking)) throw new IllegalStateException(NOT_CHANGEABLE);
    }

    private void validateCourt(Court court) {
        if (court.getStatus() != CourtStatus.ACTIVE) throw new IllegalStateException(COURT_INACTIVE);
    }

    public void validateSlot(LocalDateTime start, LocalDateTime end) {
        if (start == null || end == null || !end.equals(start.plusHours(1)))
            throw new IllegalArgumentException(INVALID_DURATION);
        if (!start.toLocalDate().equals(end.toLocalDate())
                || start.getHour() < OPENING_HOUR
                || end.toLocalTime().isAfter(LocalTime.of(CLOSING_HOUR, 0))
                || start.getMinute() != 0
                || start.getSecond() != 0
                || start.getNano() != 0)
            throw new IllegalArgumentException(
                    "Reservas devem ocupar horários cheios entre 06:00 e 23:00");
        if (!start.isAfter(now()))
            throw new IllegalArgumentException("A reserva deve começar no futuro");
    }

    private void ensureVacant(Long court, LocalDateTime start, LocalDateTime end, Long exclude) {
        if (bookings.overlapping(court, start, end).stream()
                .anyMatch(other -> !Objects.equals(other.getId(), exclude)))
            throw new IllegalStateException("Quadra já reservada neste horário");
    }

    private LocalDateTime now() {
        return LocalDateTime.now(clock);
    }

    private static ResponseStatusException missing(String message) {
        return new ResponseStatusException(HttpStatus.NOT_FOUND, message);
    }

    private BookingResponseDTO response(Booking booking) {
        return new BookingResponseDTO(
                booking.getId(),
                booking.getUser().getId(),
                booking.getCourt().getId(),
                booking.getStartTime(),
                booking.getEndTime(),
                booking.getCourtPrice(),
                booking.getCreditUsed(),
                booking.getCourtPrice().subtract(booking.getCreditUsed()),
                booking.getStatus(),
                isFutureScheduled(booking) ? cancellationPolicy(booking) : null);
    }
}
