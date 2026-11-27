package com.bookings.dto;

import com.bookings.enums.BookingStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * @param cancellationPolicy política de cancelamento vigente; {@code null} quando a reserva não
 *     pode mais ser cancelada (já cancelada, concluída ou já iniciada)
 */
public record BookingResponseDTO(
        Long id,
        Long userId,
        Long courtId,
        LocalDateTime startTime,
        LocalDateTime endTime,
        BigDecimal courtPrice,
        BigDecimal creditUsed,
        BigDecimal amountDue,
        BookingStatus status,
        CancellationPolicyDTO cancellationPolicy) {}
