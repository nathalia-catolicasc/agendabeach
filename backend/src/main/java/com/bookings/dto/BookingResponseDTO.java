package com.agendabeach.dto;

import com.agendabeach.entity.BookingStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record BookingResponseDTO(
        Long id,
        Long userId,
        Long courtId,
        LocalDateTime startTime,
        LocalDateTime endTime,
        BigDecimal courtPrice,
        BookingStatus status
) {
}