package com.users.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.users.dto.LoginDTO;
import com.users.dto.UserResponseDTO;
import com.users.entity.AuthToken;
import com.users.entity.User;
import com.users.enums.ProfileType;
import com.users.repository.AuthTokenRepository;
import com.users.repository.UserRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.server.ResponseStatusException;

import java.time.*;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
class TokenServiceTest {
    private static final Instant NOW = Instant.parse("2026-11-27T12:00:00Z");
    private static final LoginDTO WRONG = new LoginDTO("ana@example.com", "wrong-password");
    private static final LoginDTO RIGHT = new LoginDTO("ana@example.com", "password123");

    @Mock private AuthTokenRepository tokens;
    @Mock private UserRepository users;
    @Mock private UserService userService;

    private TokenService tokenService;

    @BeforeEach
    void setup() {
        Clock clock = Clock.fixed(NOW, ZoneOffset.UTC);
        tokenService =
                new TokenService(
                        tokens, users, userService, new LoginAttemptService(clock), clock);
    }

    @Test
    void login_ShouldBlockAfterRepeatedWrongPasswords() {
        when(userService.login(WRONG))
                .thenThrow(
                        new ResponseStatusException(
                                HttpStatus.UNAUTHORIZED, UserService.INVALID_CREDENTIALS));
        for (int i = 0; i < LoginAttemptService.MAX_FAILURES; i++) {
            var error =
                    assertThrows(ResponseStatusException.class, () -> tokenService.login(WRONG));
            assertEquals(HttpStatus.UNAUTHORIZED, error.getStatusCode());
        }

        var blocked = assertThrows(ResponseStatusException.class, () -> tokenService.login(RIGHT));

        assertEquals(HttpStatus.TOO_MANY_REQUESTS, blocked.getStatusCode());
        verify(userService, times(LoginAttemptService.MAX_FAILURES)).login(WRONG);
        verify(userService, never()).login(RIGHT);
        verify(tokens, never()).save(any());
    }

    @Test
    void login_ShouldIssueEightHourToken() {
        User user = new User();
        ReflectionTestUtils.setField(user, "id", 1L);
        user.setName("Ana");
        user.setEmail("ana@example.com");
        user.setProfile(ProfileType.CLIENT);
        when(userService.login(RIGHT))
                .thenReturn(new UserResponseDTO(1L, "Ana", "ana@example.com", ProfileType.CLIENT));
        when(users.findById(1L)).thenReturn(Optional.of(user));

        var response = tokenService.login(RIGHT);

        assertNotNull(response.accessToken());
        assertEquals(LocalDateTime.ofInstant(NOW, ZoneOffset.UTC).plusHours(8), response.expiresAt());
        verify(tokens).save(any(AuthToken.class));
    }

    @Test
    void purgeExpiredTokens_ShouldDeleteTokensExpiredUntilNow() {
        when(tokens.deleteExpired(LocalDateTime.ofInstant(NOW, ZoneOffset.UTC))).thenReturn(4);

        assertEquals(4, tokenService.purgeExpiredTokens());
    }
}
