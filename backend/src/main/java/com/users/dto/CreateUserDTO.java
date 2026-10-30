package com.users.dto;

import jakarta.validation.constraints.*;

public record CreateUserDTO(
        @NotBlank String name,
        @NotBlank @Email String email,
        @NotBlank @Size(min = 8, max = 72) String password,
        @NotBlank String cpf) {}
