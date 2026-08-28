package com.agendabeach.dto;

public record CreateUserDTO(
        String name,
        String email,
        String password
) {
}