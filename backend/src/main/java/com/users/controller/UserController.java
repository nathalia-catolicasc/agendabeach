package com.users.controller;

import com.bookings.dto.BookingResponseDTO;
import com.bookings.service.BookingService;
import com.users.dto.CreateUserDTO;
import com.users.dto.LoginDTO;
import com.users.dto.LoginResponseDTO;
import com.users.dto.UpdateUserDTO;
import com.users.dto.UserResponseDTO;
import com.users.service.AuthService;
import com.users.service.TokenService;
import com.users.service.UserService;

import io.swagger.v3.oas.annotations.security.SecurityRequirements;

import jakarta.validation.Valid;

import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/users")
public class UserController {
    private static final String BEARER_PREFIX = "Bearer ";

    private final AuthService auth;
    private final TokenService tokens;
    private final UserService userService;
    private final BookingService bookingService;

    public UserController(
            UserService userService,
            BookingService bookingService,
            AuthService auth,
            TokenService tokens) {
        this.auth = auth;
        this.tokens = tokens;
        this.userService = userService;
        this.bookingService = bookingService;
    }

    @SecurityRequirements
    @PostMapping
    public UserResponseDTO create(@Valid @RequestBody CreateUserDTO dto) {
        return userService.create(dto);
    }

    @SecurityRequirements
    @PostMapping("/login")
    public LoginResponseDTO login(@Valid @RequestBody LoginDTO dto) {
        return tokens.login(dto);
    }

    @PostMapping("/logout")
    public void logout(@RequestHeader("Authorization") String header) {
        tokens.logout(header.substring(BEARER_PREFIX.length()));
    }

    @GetMapping("/me/credits")
    public Map<String, BigDecimal> credits() {
        return Map.of("creditBalance", auth.current().getCreditBalance());
    }

    @GetMapping
    public List<UserResponseDTO> listAll() {
        auth.ensureAdmin();
        return userService.listAll();
    }

    @GetMapping("/{id}")
    public UserResponseDTO getById(@PathVariable Long id) {
        auth.ensureOwner(id);
        return userService.getById(id);
    }

    @PutMapping("/{id}")
    public UserResponseDTO update(@PathVariable Long id, @Valid @RequestBody UpdateUserDTO dto) {
        auth.ensureOwner(id);
        return userService.update(id, dto);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        auth.ensureOwner(id);
        userService.delete(id);
    }

    @GetMapping("/{id}/bookings")
    public List<BookingResponseDTO> listUserBookings(@PathVariable Long id) {
        auth.ensureOwner(id);
        return bookingService.listByUser(id);
    }
}
