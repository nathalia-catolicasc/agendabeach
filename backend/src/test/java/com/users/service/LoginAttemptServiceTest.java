package com.users.service;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.time.*;

class LoginAttemptServiceTest {
    private static final String EMAIL = "ana@example.com";

    private MutableClock clock;
    private LoginAttemptService attempts;

    @BeforeEach
    void setup() {
        clock = new MutableClock(Instant.parse("2026-11-27T12:00:00Z"));
        attempts = new LoginAttemptService(clock);
    }

    @Test
    void blocksAfterMaxFailuresWithinWindow() {
        failTimes(LoginAttemptService.MAX_FAILURES - 1);
        assertDoesNotThrow(() -> attempts.ensureAllowed(EMAIL));

        attempts.recordFailure(EMAIL);

        var error =
                assertThrows(ResponseStatusException.class, () -> attempts.ensureAllowed(EMAIL));
        assertEquals(HttpStatus.TOO_MANY_REQUESTS, error.getStatusCode());
        assertTrue(error.getReason().startsWith("Muitas tentativas de login"));
    }

    @Test
    void emailComparisonIgnoresCaseAndSpaces() {
        for (int i = 0; i < LoginAttemptService.MAX_FAILURES; i++) {
            attempts.recordFailure("  ANA@Example.com ");
        }
        assertThrows(ResponseStatusException.class, () -> attempts.ensureAllowed(EMAIL));
    }

    @Test
    void unlocksAfterLockDuration() {
        failTimes(LoginAttemptService.MAX_FAILURES);

        clock.advance(LoginAttemptService.LOCK_DURATION);

        assertDoesNotThrow(() -> attempts.ensureAllowed(EMAIL));
    }

    @Test
    void failuresOutsideWindowStartNewCount() {
        failTimes(LoginAttemptService.MAX_FAILURES - 1);
        clock.advance(LoginAttemptService.WINDOW);

        attempts.recordFailure(EMAIL);

        assertDoesNotThrow(() -> attempts.ensureAllowed(EMAIL));
    }

    @Test
    void successResetsFailures() {
        failTimes(LoginAttemptService.MAX_FAILURES - 1);
        attempts.recordSuccess(EMAIL);

        attempts.recordFailure(EMAIL);

        assertDoesNotThrow(() -> attempts.ensureAllowed(EMAIL));
    }

    @Test
    void purgeStaleRemovesExpiredBlocks() {
        failTimes(LoginAttemptService.MAX_FAILURES);
        clock.advance(LoginAttemptService.LOCK_DURATION);
        attempts.purgeStale();

        attempts.recordFailure(EMAIL);

        assertDoesNotThrow(() -> attempts.ensureAllowed(EMAIL));
    }

    private void failTimes(int times) {
        for (int i = 0; i < times; i++) attempts.recordFailure(EMAIL);
    }

    static final class MutableClock extends Clock {
        private Instant now;

        MutableClock(Instant now) {
            this.now = now;
        }

        void advance(Duration duration) {
            now = now.plus(duration);
        }

        @Override
        public ZoneId getZone() {
            return ZoneOffset.UTC;
        }

        @Override
        public Clock withZone(ZoneId zone) {
            return this;
        }

        @Override
        public Instant instant() {
            return now;
        }
    }
}
