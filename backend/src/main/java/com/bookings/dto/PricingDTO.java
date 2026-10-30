package com.bookings.dto;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public record PricingDTO(
        @NotNull @DecimalMin("0.00") @Digits(integer = 8, fraction = 2) BigDecimal regularPrice,
        @NotNull @DecimalMin("0.00") @Digits(integer = 8, fraction = 2) BigDecimal peakPrice) {}
