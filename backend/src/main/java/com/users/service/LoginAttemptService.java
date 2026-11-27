package com.users.service;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Limita tentativas de login por e-mail: após {@value #MAX_FAILURES} senhas erradas dentro de
 * {@link #WINDOW}, o e-mail fica bloqueado por {@link #LOCK_DURATION}. Um login bem-sucedido zera
 * a contagem.
 *
 * <p>O controle é em memória, suficiente para uma instância da API. Com várias instâncias, o
 * estado precisa ir para um armazenamento compartilhado.
 */
@Service
public class LoginAttemptService {
    static final int MAX_FAILURES = 5;
    static final Duration WINDOW = Duration.ofMinutes(15);
    static final Duration LOCK_DURATION = Duration.ofMinutes(15);

    private record Attempts(int failures, Instant firstFailure, Instant blockedUntil) {}

    private final Map<String, Attempts> attempts = new ConcurrentHashMap<>();
    private final Clock clock;

    public LoginAttemptService(Clock clock) {
        this.clock = clock;
    }

    public void ensureAllowed(String email) {
        Attempts current = attempts.get(key(email));
        Instant now = clock.instant();
        if (current != null
                && current.blockedUntil() != null
                && now.isBefore(current.blockedUntil())) {
            long minutes =
                    Math.max(1, Duration.between(now, current.blockedUntil()).toSeconds() / 60);
            throw new ResponseStatusException(
                    HttpStatus.TOO_MANY_REQUESTS,
                    "Muitas tentativas de login. Tente novamente em " + minutes + " minuto(s)");
        }
    }

    public void recordFailure(String email) {
        Instant now = clock.instant();
        attempts.compute(
                key(email),
                (ignored, current) -> {
                    if (current == null || expired(current, now)) {
                        return new Attempts(1, now, null);
                    }
                    int failures = current.failures() + 1;
                    Instant blockedUntil =
                            failures >= MAX_FAILURES ? now.plus(LOCK_DURATION) : null;
                    return new Attempts(failures, current.firstFailure(), blockedUntil);
                });
    }

    public void recordSuccess(String email) {
        attempts.remove(key(email));
    }

    /** Remove contagens antigas para o mapa não crescer indefinidamente. */
    public void purgeStale() {
        Instant now = clock.instant();
        attempts.values().removeIf(current -> expired(current, now));
    }

    private static boolean expired(Attempts current, Instant now) {
        if (current.blockedUntil() != null) return !now.isBefore(current.blockedUntil());
        return !now.isBefore(current.firstFailure().plus(WINDOW));
    }

    private static String key(String email) {
        return email == null ? "" : email.trim().toLowerCase(Locale.ROOT);
    }
}
