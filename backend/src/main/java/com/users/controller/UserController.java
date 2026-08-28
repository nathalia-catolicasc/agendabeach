package com.agendabeach.controller;

import com.agendabeach.dto.CreateUserDTO;
import com.agendabeach.dto.LoginDTO;
import com.agendabeach.dto.UserResponseDTO;
import com.agendabeach.service.UserService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping
    public UserResponseDTO create(@RequestBody CreateUserDTO dto) {
        return userService.create(dto);
    }

    @PostMapping("/login")
    public UserResponseDTO login(@RequestBody LoginDTO dto) {
        return userService.login(dto);
    }
}