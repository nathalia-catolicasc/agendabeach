package com.bookings.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record AvailabilityDTO(
        Long courtId,
        LocalDateTime startTime,
        LocalDateTime endTime,
        boolean available,
        BigDecimal price) {}
