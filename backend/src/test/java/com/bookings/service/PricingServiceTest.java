package com.bookings.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.bookings.entity.PricingPolicy;
import com.bookings.repository.*;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.math.BigDecimal;
import java.time.*;
import java.util.Optional;

class PricingServiceTest {
    @ParameterizedTest
    @CsvSource({
        "2026-10-21T06:00,60.00",
        "2026-10-21T16:00,60.00",
        "2026-10-21T17:00,100.00",
        "2026-10-24T06:00,100.00",
        "2026-10-25T06:00,100.00",
        "2026-12-25T06:00,100.00"
    })
    void pricesByWeekdayPeakAndHoliday(String date, String price) {
        var policies = mock(PricingPolicyRepository.class);
        var holidays = mock(HolidayRepository.class);
        when(policies.findById(1L)).thenReturn(Optional.of(new PricingPolicy()));
        assertEquals(
                new BigDecimal(price),
                new PricingService(policies, holidays).price(LocalDateTime.parse(date)));
    }

    @org.junit.jupiter.api.Test
    void customHolidayAndAdministrativePrices() {
        var policies = mock(PricingPolicyRepository.class);
        var holidays = mock(HolidayRepository.class);
        var policy = new PricingPolicy();
        policy.setPeakPrice(new BigDecimal("135.00"));
        when(policies.findById(1L)).thenReturn(Optional.of(policy));
        when(holidays.existsById(any())).thenReturn(true);
        assertEquals(
                new BigDecimal("135.00"),
                new PricingService(policies, holidays)
                        .price(LocalDateTime.parse("2026-10-21T06:00")));
    }
}
