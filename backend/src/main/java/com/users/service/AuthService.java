package com.users.service;

import com.users.entity.User;
import com.users.repository.UserRepository;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AuthService {
    private final UserRepository users;

    public AuthService(UserRepository users) {
        this.users = users;
    }

    public Long currentId() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof Long id))
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        return id;
    }

    public User current() {
        return users.findById(currentId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));
    }

    public User getCurrentUser(HttpServletRequest request) {
        return current();
    }

    public void ensureAdmin(HttpServletRequest request) {
        ensureAdmin();
    }

    public void ensureAdmin() {
        currentId();
        if (SecurityContextHolder.getContext().getAuthentication().getAuthorities().stream()
                .noneMatch(a -> a.getAuthority().equals("ROLE_ADMIN")))
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
    }

    public void ensureOwner(Long id) {
        if (!currentId().equals(id)) ensureAdmin();
    }
}
