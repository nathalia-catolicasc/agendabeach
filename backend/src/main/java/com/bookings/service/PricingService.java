package com.bookings.service;

import com.bookings.dto.PricingDTO;
import com.bookings.repository.*;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.*;
import java.util.Set;

@Service
public class PricingService {
    private final PricingPolicyRepository policies;
    private final HolidayRepository holidays;
    private static final Set<MonthDay> NATIONAL =
            Set.of(
                    MonthDay.of(1, 1),
                    MonthDay.of(4, 21),
                    MonthDay.of(5, 1),
                    MonthDay.of(9, 7),
                    MonthDay.of(10, 12),
                    MonthDay.of(11, 2),
                    MonthDay.of(11, 15),
                    MonthDay.of(11, 20),
                    MonthDay.of(12, 25));

    public PricingService(PricingPolicyRepository policies, HolidayRepository holidays) {
        this.policies = policies;
        this.holidays = holidays;
    }

    public PricingDTO get() {
        var p =
                policies.findById(1L)
                        .orElseThrow(() -> new IllegalStateException("Pricing policy missing"));
        return new PricingDTO(p.getRegularPrice(), p.getPeakPrice());
    }

    @Transactional
    public PricingDTO update(PricingDTO dto) {
        var p = policies.findById(1L).orElseThrow();
        p.setRegularPrice(dto.regularPrice());
        p.setPeakPrice(dto.peakPrice());
        policies.save(p);
        return dto;
    }

    public java.util.List<BigDecimal> dayPrices(LocalDate date) {
        var p = get();
        boolean special = premiumDate(date);
        return java.util.stream.IntStream.range(6, 23)
                .mapToObj(hour -> special || hour >= 17 ? p.peakPrice() : p.regularPrice())
                .toList();
    }

    private boolean premiumDate(LocalDate date) {
        return date.getDayOfWeek().getValue() >= 6
                || NATIONAL.contains(MonthDay.from(date))
                || holidays.existsById(date);
    }

    public BigDecimal price(LocalDateTime start) {
        var p = get();
        LocalDate date = start.toLocalDate();
        boolean peak = start.getHour() >= 17 || premiumDate(date);
        return peak ? p.peakPrice() : p.regularPrice();
    }
}
