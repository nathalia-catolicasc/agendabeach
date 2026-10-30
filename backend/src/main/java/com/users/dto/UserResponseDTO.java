package com.users.dto;

import com.users.enums.ProfileType;

public record UserResponseDTO(Long id, String name, String email, ProfileType profile) {}
