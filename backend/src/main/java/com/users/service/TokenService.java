package com.users.service;

import com.users.dto.*;
import com.users.entity.*;
import com.users.repository.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.nio.charset.StandardCharsets;
import java.security.*;
import java.time.*;
import java.util.*;

@Service
public class TokenService {
    private static final Logger log = LoggerFactory.getLogger(TokenService.class);
    private static final Duration TOKEN_VALIDITY = Duration.ofHours(8);

    private final AuthTokenRepository tokens;
    private final UserRepository users;
    private final UserService userService;
    private final LoginAttemptService loginAttempts;
    private final Clock clock;
    private final SecureRandom random = new SecureRandom();

    public TokenService(
            AuthTokenRepository tokens,
            UserRepository users,
            UserService userService,
            LoginAttemptService loginAttempts,
            Clock clock) {
        this.tokens = tokens;
        this.users = users;
        this.userService = userService;
        this.loginAttempts = loginAttempts;
        this.clock = clock;
    }

    @Transactional
    public LoginResponseDTO login(LoginDTO dto) {
        loginAttempts.ensureAllowed(dto.email());
        UserResponseDTO response;
        try {
            response = userService.login(dto);
        } catch (ResponseStatusException e) {
            loginAttempts.recordFailure(dto.email());
            throw e;
        }
        loginAttempts.recordSuccess(dto.email());
        User user = users.findById(response.id()).orElseThrow();
        byte[] bytes = new byte[32];
        random.nextBytes(bytes);
        String token = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        LocalDateTime expiry = LocalDateTime.now(clock).plus(TOKEN_VALIDITY);
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

    /** Apaga diariamente os tokens expirados (padrão: 03:00, horário de Brasília). */
    @Scheduled(
            cron = "${agendabeach.auth.token-cleanup-cron:0 0 3 * * *}",
            zone = "America/Sao_Paulo")
    @Transactional
    public int purgeExpiredTokens() {
        int removed = tokens.deleteExpired(LocalDateTime.now(clock));
        loginAttempts.purgeStale();
        log.info("Limpeza de tokens expirados: {} removido(s)", removed);
        return removed;
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
