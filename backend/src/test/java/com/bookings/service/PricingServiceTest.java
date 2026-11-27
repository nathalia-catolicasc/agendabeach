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

    @ParameterizedTest
    @CsvSource({"2024,2024-03-31", "2025,2025-04-20", "2026,2026-04-05", "2027,2027-03-28"})
    void calculatesEasterSunday(int year, String easter) {
        assertEquals(LocalDate.parse(easter), PricingService.easterSunday(year));
    }

    @ParameterizedTest
    @CsvSource({
        // Sexta-feira Santa (Páscoa - 2) e Corpus Christi (Páscoa + 60), em dias úteis
        "2027-03-26T10:00,100.00",
        "2027-05-27T10:00,100.00",
        "2026-04-03T10:00,100.00",
        "2026-06-04T10:00,100.00",
        // Quinta-feira Santa e o dia seguinte a Corpus Christi seguem a regra de dia útil
        "2027-03-25T10:00,60.00",
        "2027-05-28T10:00,60.00"
    })
    void movableHolidaysUsePeakPrice(String date, String price) {
        var policies = mock(PricingPolicyRepository.class);
        var holidays = mock(HolidayRepository.class);
        when(policies.findById(1L)).thenReturn(Optional.of(new PricingPolicy()));
        assertEquals(
                new BigDecimal(price),
                new PricingService(policies, holidays).price(LocalDateTime.parse(date)));
    }

    @ParameterizedTest
    @CsvSource({"2026-10-21", "2026-10-24", "2027-03-26", "2026-12-25"})
    void dayPricesMatchSinglePriceForEverySlot(String day) {
        var policies = mock(PricingPolicyRepository.class);
        var holidays = mock(HolidayRepository.class);
        when(policies.findById(1L)).thenReturn(Optional.of(new PricingPolicy()));
        var service = new PricingService(policies, holidays);
        LocalDate date = LocalDate.parse(day);

        var grid = service.dayPrices(date);

        assertEquals(BusinessHours.CLOSING_HOUR - BusinessHours.OPENING_HOUR, grid.size());
        for (int hour = BusinessHours.OPENING_HOUR; hour < BusinessHours.CLOSING_HOUR; hour++) {
            assertEquals(
                    service.price(date.atTime(hour, 0)),
                    grid.get(hour - BusinessHours.OPENING_HOUR),
                    "Grade e cobrança divergem às " + hour + "h");
        }
    }

    @org.junit.jupiter.api.Test
    void missingPolicyIsReportedInPortuguese() {
        var policies = mock(PricingPolicyRepository.class);
        when(policies.findById(1L)).thenReturn(Optional.empty());
        var error =
                assertThrows(
                        IllegalStateException.class,
                        () -> new PricingService(policies, mock(HolidayRepository.class)).get());
        assertEquals("Política de preços não cadastrada", error.getMessage());
    }
}
