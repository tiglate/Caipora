package br.dev.ampliar.caipora.service;

import br.dev.ampliar.caipora.domain.LoginAttempt;
import br.dev.ampliar.caipora.repos.LoginAttemptRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class LoginAttemptServiceTest {

    private static final String IP = "10.0.0.1";
    private static final String OTHER_IP = "10.0.0.2";

    private final Map<String, LoginAttempt> table = new HashMap<>();
    private MutableClock clock;
    private LoginAttemptService service;

    @BeforeEach
    @SuppressWarnings("unchecked")
    void setUp() {
        clock = new MutableClock(Instant.parse("2026-01-01T10:00:00Z"));
        final LoginAttemptRepository repository = mock(LoginAttemptRepository.class);
        when(repository.findByUsernameAndClientIp(anyString(), anyString()))
                .thenAnswer(inv -> Optional.ofNullable(table.get(inv.getArgument(0) + "|" + inv.getArgument(1))));
        when(repository.save(any(LoginAttempt.class))).thenAnswer(inv -> {
            final LoginAttempt attempt = inv.getArgument(0);
            table.put(attempt.getUsername() + "|" + attempt.getClientIp(), attempt);
            return attempt;
        });
        when(repository.deleteAllByUsername(anyString())).thenAnswer(inv -> {
            final String username = inv.getArgument(0);
            table.keySet().removeIf(key -> key.startsWith(username + "|"));
            return 0;
        });
        final TransactionTemplate transactionTemplate = mock(TransactionTemplate.class);
        doAnswer(inv -> {
            ((Consumer<Object>) inv.getArgument(0)).accept(null);
            return null;
        }).when(transactionTemplate).executeWithoutResult(any());
        service = new LoginAttemptService(repository, transactionTemplate, 3, Duration.ofMinutes(15), clock);
    }

    @Test
    void locksAfterMaxAttempts() {
        service.loginFailed("alice", IP);
        service.loginFailed("ALICE", IP);
        assertFalse(service.isLocked("alice", IP));

        service.loginFailed("alice", IP);

        assertTrue(service.isLocked("Alice", IP));
    }

    @Test
    void lockIsPerClientAddress() {
        for (int i = 0; i < 3; i++) {
            service.loginFailed("alice", IP);
        }

        // an attacker at IP cannot lock the legitimate user out of another address
        assertFalse(service.isLocked("alice", OTHER_IP));
    }

    @Test
    void lockExpires() {
        for (int i = 0; i < 3; i++) {
            service.loginFailed("alice", IP);
        }
        clock.advance(Duration.ofMinutes(16));

        assertFalse(service.isLocked("alice", IP));
    }

    @Test
    void streakRestartsAfterWindow() {
        service.loginFailed("alice", IP);
        service.loginFailed("alice", IP);
        clock.advance(Duration.ofMinutes(16));
        service.loginFailed("alice", IP);

        assertFalse(service.isLocked("alice", IP));
    }

    @Test
    void successResetsCounter() {
        service.loginFailed("alice", IP);
        service.loginFailed("alice", IP);
        service.loginSucceeded("alice");
        service.loginFailed("alice", IP);

        assertFalse(service.isLocked("alice", IP));
    }

    @Test
    void blankUsernamesAreIgnored() {
        service.loginFailed(null, IP);
        service.loginFailed(" ", IP);

        assertFalse(service.isLocked(null, IP));
        assertTrue(table.isEmpty());
    }

    private static final class MutableClock extends Clock {
        private Instant now;

        MutableClock(final Instant now) {
            this.now = now;
        }

        void advance(final Duration duration) {
            now = now.plus(duration);
        }

        @Override
        public ZoneOffset getZone() {
            return ZoneOffset.UTC;
        }

        @Override
        public Clock withZone(final ZoneId zone) {
            return this;
        }

        @Override
        public Instant instant() {
            return now;
        }
    }
}
