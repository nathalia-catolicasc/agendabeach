package com.bookings.dto;

import java.time.LocalDateTime;

public record UpdateBookingDTO(
        Long userId,
        Long courtId,
        LocalDateTime startTime,
        LocalDateTime endTime
) {
}
