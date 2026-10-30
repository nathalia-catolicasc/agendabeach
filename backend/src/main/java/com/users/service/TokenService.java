package com.users.service;

import com.users.dto.*;
import com.users.entity.*;
import com.users.repository.*;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.*;
import java.time.*;
import java.util.*;

@Service
public class TokenService {
    private final AuthTokenRepository tokens;
    private final UserRepository users;
    private final UserService userService;
    private final Clock clock;

    public TokenService(
            AuthTokenRepository tokens,
            UserRepository users,
            UserService userService,
            Clock clock) {
        this.tokens = tokens;
        this.users = users;
        this.userService = userService;
        this.clock = clock;
    }

    @Transactional
    public LoginResponseDTO login(LoginDTO dto) {
        UserResponseDTO response = userService.login(dto);
        User user = users.findById(response.id()).orElseThrow();
        byte[] random = new byte[32];
        new SecureRandom().nextBytes(random);
        String token = Base64.getUrlEncoder().withoutPadding().encodeToString(random);
        LocalDateTime expiry = LocalDateTime.now(clock).plusHours(8);
        tokens.save(new AuthToken(hash(token), user, expiry));
        return new LoginResponseDTO(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getProfile(),
                token,
                "Bearer",
                expiry);
    }

    @Transactional(readOnly = true)
    public Optional<User> authenticate(String token) {
        return tokens.findById(hash(token))
                .filter(t -> t.getExpiresAt().isAfter(LocalDateTime.now(clock)))
                .map(AuthToken::getUser);
    }

    @Transactional
    public void logout(String token) {
        tokens.deleteById(hash(token));
    }

    private String hash(String token) {
        try {
            return HexFormat.of()
                    .formatHex(
                            MessageDigest.getInstance("SHA-256")
                                    .digest(token.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }
}
