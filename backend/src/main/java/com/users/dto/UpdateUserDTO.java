package com.users.dto;

public record UpdateUserDTO(
        String name,
        String email,
        String password
) {
}
