package com.agendabeach.service;

import com.agendabeach.dto.BookingResponseDTO;
import com.agendabeach.dto.CreateBookingDTO;
import com.agendabeach.entity.Booking;
import com.agendabeach.enum.BookingStatus;
import com.agendabeach.entity.Court;
import com.agendabeach.entity.User;
import com.agendabeach.repository.BookingRepository;
import com.agendabeach.repository.CourtRepository;
import com.agendabeach.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class BookingService {

    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final CourtRepository courtRepository;

    public BookingService(
            BookingRepository bookingRepository,
            UserRepository userRepository,
            CourtRepository courtRepository
    ) {
        this.bookingRepository = bookingRepository;
        this.userRepository = userRepository;
        this.courtRepository = courtRepository;
    }

    public BookingResponseDTO create(CreateBookingDTO dto) {

        User user = userRepository.findById(dto.userId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        Court court = courtRepository.findById(dto.courtId())
                .orElseThrow(() -> new RuntimeException("Court not found"));

        if (court.getStatus().name().equals("INACTIVE")) {
            throw new RuntimeException("Court is inactive");
        }

        if (!dto.endTime().isAfter(dto.startTime())) {
            throw new RuntimeException("End time must be after start time");
        }

        Booking booking = new Booking();

        booking.setUser(user);
        booking.setCourt(court);
        booking.setStartTime(dto.startTime());
        booking.setEndTime(dto.endTime());
        booking.setStatus(BookingStatus.SCHEDULED);
        booking.setCreatedAt(LocalDateTime.now());

        // Defina aqui a regra de preço da sua tarefa
        // booking.setCourtPrice(...);

        Booking savedBooking = bookingRepository.save(booking);

        return new BookingResponseDTO(
                savedBooking.getId(),
                savedBooking.getUser().getId(),
                savedBooking.getCourt().getId(),
                savedBooking.getStartTime(),
                savedBooking.getEndTime(),
                savedBooking.getCourtPrice(),
                savedBooking.getStatus()
        );
    }
}