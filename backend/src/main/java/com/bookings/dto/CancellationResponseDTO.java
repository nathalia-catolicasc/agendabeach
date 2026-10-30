package com.bookings.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record CancellationResponseDTO(
        Long bookingId,
        BigDecimal refundAmount,
        BigDecimal creditAmount,
        String status,
        LocalDateTime cancelledAt) {}
