package com.bookings.dto;

import jakarta.validation.constraints.*;

import java.time.LocalDateTime;

public record CreateBookingDTO(
        Long userId,
        @NotNull Long courtId,
        @NotNull LocalDateTime startTime,
        @NotNull LocalDateTime endTime,
        Boolean useCredit) {}
