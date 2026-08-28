package com.agendabeach.controller;

import com.agendabeach.dto.BookingResponseDTO;
import com.agendabeach.dto.CreateBookingDTO;
import com.agendabeach.service.BookingService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/bookings")
public class BookingController {

    private final BookingService bookingService;

    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @PostMapping
    public BookingResponseDTO create(
            @RequestBody CreateBookingDTO dto
    ) {
        return bookingService.create(dto);
    }
}