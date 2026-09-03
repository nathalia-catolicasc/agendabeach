package com.users.dto;

public record UserResponseDTO(
        Long id,
        String name,
        String email
) {
}