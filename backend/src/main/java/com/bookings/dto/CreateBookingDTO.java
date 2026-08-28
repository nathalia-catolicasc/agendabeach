package com.agendabeach.dto;

import java.time.LocalDateTime;

public record CreateBookingDTO(
        Long userId,
        Long courtId,
        LocalDateTime startTime,
        LocalDateTime endTime
) {
}