package com.users.controller;

import com.bookings.dto.BookingResponseDTO;
import com.bookings.service.BookingService;
import com.users.dto.CreateUserDTO;
import com.users.dto.LoginDTO;
import com.users.dto.UpdateUserDTO;
import com.users.dto.UserResponseDTO;
import com.users.service.UserService;

import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/users")
public class UserController {

    private final com.users.service.AuthService auth;
    private final com.users.service.TokenService tokens;
    private final UserService userService;
    private final BookingService bookingService;

    public UserController(
            UserService userService,
            BookingService bookingService,
            com.users.service.AuthService auth,
            com.users.service.TokenService tokens) {
        this.auth = auth;
        this.tokens = tokens;
        this.userService = userService;
        this.bookingService = bookingService;
    }

    @io.swagger.v3.oas.annotations.security.SecurityRequirements
    @PostMapping
    public UserResponseDTO create(@jakarta.validation.Valid @RequestBody CreateUserDTO dto) {
        return userService.create(dto);
    }

    @io.swagger.v3.oas.annotations.security.SecurityRequirements
    @PostMapping("/login")
    public com.users.dto.LoginResponseDTO login(
            @jakarta.validation.Valid @RequestBody LoginDTO dto) {
        return tokens.login(dto);
    }

    @PostMapping("/logout")
    public void logout(@RequestHeader("Authorization") String header) {
        tokens.logout(header.substring(7));
    }

    @GetMapping("/me/credits")
    public java.util.Map<String, java.math.BigDecimal> credits() {
        return java.util.Map.of("creditBalance", auth.current().getCreditBalance());
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
    public UserResponseDTO update(
            @PathVariable Long id, @jakarta.validation.Valid @RequestBody UpdateUserDTO dto) {
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
