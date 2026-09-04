package com.bookings.dto;

import com.bookings.enums.CourtStatus;
import com.bookings.enums.CourtType;

public record CreateCourtDTO(
        String name,
        CourtType type,
        CourtStatus status
) {
}
