package com.users.dto;

import jakarta.validation.constraints.*;

public record UpdateUserDTO(
        String name, @Email String email, @Size(min = 8, max = 72) String password, String cpf) {}
