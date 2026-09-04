package com.bookings.controller;

import com.bookings.dto.CourtResponseDTO;
import com.bookings.dto.CreateCourtDTO;
import com.bookings.dto.UpdateCourtDTO;
import com.bookings.service.CourtService;
import com.users.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/courts")
public class CourtController {

    private final CourtService courtService;
    private final AuthService authService;

    public CourtController(CourtService courtService, AuthService authService) {
        this.courtService = courtService;
        this.authService = authService;
    }

    @PostMapping
    public CourtResponseDTO create(@RequestBody CreateCourtDTO dto, HttpServletRequest request) {
        authService.ensureAdmin(request);
        return courtService.create(dto);
    }

    @GetMapping
    public List<CourtResponseDTO> listAll() {
        return courtService.listAll();
    }

    @GetMapping("/{id}")
    public CourtResponseDTO getById(@PathVariable Long id) {
        return courtService.getById(id);
    }

    @PutMapping("/{id}")
    public CourtResponseDTO update(@PathVariable Long id, @RequestBody UpdateCourtDTO dto, HttpServletRequest request) {
        authService.ensureAdmin(request);
        return courtService.update(id, dto);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id, HttpServletRequest request) {
        authService.ensureAdmin(request);
        courtService.delete(id);
    }
}
