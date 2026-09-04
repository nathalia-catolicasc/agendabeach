package com.users.service;

import com.users.entity.User;
import com.users.enums.ProfileType;
import com.users.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AuthService {

    private final UserRepository userRepository;

    public AuthService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User getCurrentUser(HttpServletRequest request) {
        String idHeader = request.getHeader("X-User-Id");
        if (idHeader == null || idHeader.isBlank()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Missing X-User-Id header");
        }
        Long userId;
        try {
            userId = Long.parseLong(idHeader);
        } catch (NumberFormatException e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid X-User-Id header");
        }
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));
    }

    public void ensureAdmin(HttpServletRequest request) {
        User user = getCurrentUser(request);
        if (user.getProfile() != ProfileType.ADMIN) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Admin only");
        }
    }
}
