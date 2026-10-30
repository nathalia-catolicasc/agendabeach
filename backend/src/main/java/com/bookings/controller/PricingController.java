package com.bookings.controller;

import com.bookings.dto.PricingDTO;
import com.bookings.entity.Holiday;
import com.bookings.repository.HolidayRepository;
import com.bookings.service.PricingService;
import com.users.service.AuthService;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/pricing")
public class PricingController {
    private final PricingService prices;
    private final HolidayRepository holidays;
    private final AuthService auth;

    public PricingController(PricingService prices, HolidayRepository holidays, AuthService auth) {
        this.prices = prices;
        this.holidays = holidays;
        this.auth = auth;
    }

    @GetMapping
    public PricingDTO get() {
        return prices.get();
    }

    @PutMapping
    public PricingDTO update(@Valid @RequestBody PricingDTO dto) {
        auth.ensureAdmin();
        return prices.update(dto);
    }

    public record HolidayDTO(
            @NotNull LocalDate date, @NotBlank @Size(max = 150) String description) {}

    @GetMapping("/holidays")
    public List<HolidayDTO> holidays() {
        return holidays.findAll().stream()
                .map(h -> new HolidayDTO(h.getDate(), h.getDescription()))
                .toList();
    }

    @PutMapping("/holidays")
    public HolidayDTO holiday(@Valid @RequestBody HolidayDTO dto) {
        auth.ensureAdmin();
        holidays.save(new Holiday(dto.date(), dto.description()));
        return dto;
    }

    @DeleteMapping("/holidays/{date}")
    public void deleteHoliday(@PathVariable LocalDate date) {
        auth.ensureAdmin();
        holidays.deleteById(date);
    }
}
