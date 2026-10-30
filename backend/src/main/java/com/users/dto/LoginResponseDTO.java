package com.users.dto;

import java.time.LocalDateTime;

public record LoginResponseDTO(
        Long id,
        String name,
        String email,
        com.users.enums.ProfileType profile,
        String accessToken,
        String tokenType,
        LocalDateTime expiresAt) {}
