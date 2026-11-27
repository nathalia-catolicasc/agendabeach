package com.bookings.controller;

import com.bookings.dto.AvailabilityDTO;
import com.bookings.dto.BookingResponseDTO;
import com.bookings.dto.CancellationResponseDTO;
import com.bookings.dto.CreateBookingDTO;
import com.bookings.dto.UpdateBookingDTO;
import com.bookings.service.BookingService;

import jakarta.validation.Valid;

import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/bookings")
public class BookingController {

    private final BookingService bookingService;

    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @PostMapping
    public BookingResponseDTO create(@Valid @RequestBody CreateBookingDTO dto) {
        return bookingService.create(dto);
    }

    @GetMapping
    public List<BookingResponseDTO> listAll() {
        return bookingService.listAll();
    }

    @GetMapping("/{id}")
    public BookingResponseDTO getById(@PathVariable Long id) {
        return bookingService.getById(id);
    }

    @PutMapping("/{id}")
    public BookingResponseDTO update(@PathVariable Long id, @RequestBody UpdateBookingDTO dto) {
        return bookingService.update(id, dto);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        bookingService.delete(id);
    }

    @GetMapping("/availability")
    public List<AvailabilityDTO> availability(
            @RequestParam LocalDate date, @RequestParam Long courtId) {
        return bookingService.availability(date, courtId);
    }

    @GetMapping("/search")
    public List<BookingResponseDTO> searchByDate(
            @RequestParam("date") LocalDate date,
            @RequestParam(value = "courtId", required = false) Long courtId) {
        if (courtId != null) {
            return bookingService.listByDateAndCourt(date, courtId);
        }
        return bookingService.listByDate(date);
    }

    @PostMapping("/{id}/cancel")
    public CancellationResponseDTO cancel(@PathVariable Long id) {
        return bookingService.cancel(id);
    }
}
