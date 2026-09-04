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

    private final UserService userService;
    private final BookingService bookingService;

    public UserController(UserService userService, BookingService bookingService) {
        this.userService = userService;
        this.bookingService = bookingService;
    }

    @PostMapping
    public UserResponseDTO create(@RequestBody CreateUserDTO dto) {
        return userService.create(dto);
    }

    @PostMapping("/login")
    public UserResponseDTO login(@RequestBody LoginDTO dto) {
        return userService.login(dto);
    }

    @GetMapping
    public List<UserResponseDTO> listAll() {
        return userService.listAll();
    }

    @GetMapping("/{id}")
    public UserResponseDTO getById(@PathVariable Long id) {
        return userService.getById(id);
    }

    @PutMapping("/{id}")
    public UserResponseDTO update(@PathVariable Long id, @RequestBody UpdateUserDTO dto) {
        return userService.update(id, dto);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        userService.delete(id);
    }

    @GetMapping("/{id}/bookings")
    public List<BookingResponseDTO> listUserBookings(@PathVariable Long id) {
        return bookingService.listByUser(id);
    }
}