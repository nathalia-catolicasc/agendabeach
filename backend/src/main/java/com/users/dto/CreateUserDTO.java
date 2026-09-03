package com.users.dto;

public record CreateUserDTO(
        String name,
        String email,
        String password
) {
}