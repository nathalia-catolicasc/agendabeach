package com.bookings.service;

import static com.bookings.service.BusinessHours.*;

import com.bookings.dto.PricingDTO;
import com.bookings.entity.PricingPolicy;
import com.bookings.repository.*;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.*;
import java.util.List;
import java.util.Set;
import java.util.stream.IntStream;

@Service
public class PricingService {
    private static final long POLICY_ID = 1L;

    /** Feriados nacionais de data fixa. Os móveis são calculados a partir da Páscoa. */
    private static final Set<MonthDay> FIXED_NATIONAL_HOLIDAYS =
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

    private final PricingPolicyRepository policies;
    private final HolidayRepository holidays;

    public PricingService(PricingPolicyRepository policies, HolidayRepository holidays) {
        this.policies = policies;
        this.holidays = holidays;
    }

    public PricingDTO get() {
        PricingPolicy policy = policy();
        return new PricingDTO(policy.getRegularPrice(), policy.getPeakPrice());
    }

    @Transactional
    public PricingDTO update(PricingDTO dto) {
        PricingPolicy policy = policy();
        policy.setRegularPrice(dto.regularPrice());
        policy.setPeakPrice(dto.peakPrice());
        policies.save(policy);
        return dto;
    }

    /** Preço cobrado por uma reserva que começa em {@code start}. */
    public BigDecimal price(LocalDateTime start) {
        return rate(get(), premiumDate(start.toLocalDate()), start.getHour());
    }

    /** Preço de cada horário do dia, das 06:00 às 22:00, na mesma ordem da grade. */
    public List<BigDecimal> dayPrices(LocalDate date) {
        PricingDTO policy = get();
        boolean premium = premiumDate(date);
        return IntStream.range(OPENING_HOUR, CLOSING_HOUR)
                .mapToObj(hour -> rate(policy, premium, hour))
                .toList();
    }

    /** Regra única de RN02: pico a partir das 17h, fins de semana e feriados. */
    private static BigDecimal rate(PricingDTO policy, boolean premiumDate, int hour) {
        return premiumDate || hour >= PEAK_START_HOUR ? policy.peakPrice() : policy.regularPrice();
    }

    private boolean premiumDate(LocalDate date) {
        return date.getDayOfWeek() == DayOfWeek.SATURDAY
                || date.getDayOfWeek() == DayOfWeek.SUNDAY
                || isNationalHoliday(date)
                || holidays.existsById(date);
    }

    /** Feriados nacionais fixos, Sexta-feira Santa e Corpus Christi. */
    static boolean isNationalHoliday(LocalDate date) {
        if (FIXED_NATIONAL_HOLIDAYS.contains(MonthDay.from(date))) return true;
        LocalDate easter = easterSunday(date.getYear());
        return date.equals(easter.minusDays(2)) || date.equals(easter.plusDays(60));
    }

    /** Domingo de Páscoa no calendário gregoriano (algoritmo de Meeus/Jones/Butcher). */
    static LocalDate easterSunday(int year) {
        int a = year % 19;
        int b = year / 100;
        int c = year % 100;
        int d = b / 4;
        int e = b % 4;
        int f = (b + 8) / 25;
        int g = (b - f + 1) / 3;
        int h = (19 * a + b - d - g + 15) % 30;
        int i = c / 4;
        int k = c % 4;
        int l = (32 + 2 * e + 2 * i - h - k) % 7;
        int m = (a + 11 * h + 22 * l) / 451;
        int month = (h + l - 7 * m + 114) / 31;
        int day = ((h + l - 7 * m + 114) % 31) + 1;
        return LocalDate.of(year, month, day);
    }

    private PricingPolicy policy() {
        return policies.findById(POLICY_ID)
                .orElseThrow(() -> new IllegalStateException("Política de preços não cadastrada"));
    }
}
