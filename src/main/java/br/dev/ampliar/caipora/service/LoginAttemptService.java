package br.dev.ampliar.caipora.service;

import br.dev.ampliar.caipora.domain.LoginAttempt;
import br.dev.ampliar.caipora.repos.LoginAttemptRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.EventListener;
import org.springframework.security.authentication.event.AuthenticationFailureBadCredentialsEvent;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.WebAuthenticationDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Locale;

/**
 * Brute-force protection for the login form.
 * <p>
 * After {@code maxAttempts} consecutive failures for a username <em>from the same client address</em>, further
 * attempts from that address are refused for {@code lockDuration}; a successful login clears the counters.
 * Keying on the address means an attacker cannot lock a victim out from somewhere else (a denial of service that a
 * per-username lock would allow). Counters are stored in the database, so every application instance shares them
 * and they survive restarts.
 * <p>
 * Behind a reverse proxy, configure {@code server.forward-headers-strategy} so the real client address is used.
 */
@Service
public class LoginAttemptService {

    private static final Logger log = LoggerFactory.getLogger(LoginAttemptService.class);
    private static final String UNKNOWN_ADDRESS = "unknown";
    private static final int MAX_USERNAME_LENGTH = 255;

    private final LoginAttemptRepository repository;
    private final TransactionTemplate transactionTemplate;
    private final int maxAttempts;
    private final Duration lockDuration;
    private final Clock clock;

    @Autowired
    public LoginAttemptService(final LoginAttemptRepository repository,
                               final PlatformTransactionManager transactionManager,
                               @Value("${security.login.max-attempts:5}") final int maxAttempts,
                               @Value("${security.login.lock-duration:PT15M}") final Duration lockDuration) {
        this(repository, new TransactionTemplate(transactionManager), maxAttempts, lockDuration, Clock.systemUTC());
    }

    LoginAttemptService(final LoginAttemptRepository repository, final TransactionTemplate transactionTemplate,
                        final int maxAttempts, final Duration lockDuration, final Clock clock) {
        this.repository = repository;
        this.transactionTemplate = transactionTemplate;
        this.maxAttempts = maxAttempts;
        this.lockDuration = lockDuration;
        this.clock = clock;
    }

    @EventListener
    public void onFailure(final AuthenticationFailureBadCredentialsEvent event) {
        loginFailed(event.getAuthentication().getName(), clientAddress(event.getAuthentication()));
    }

    @EventListener
    public void onSuccess(final AuthenticationSuccessEvent event) {
        loginSucceeded(event.getAuthentication().getName());
    }

    public void loginFailed(final String username, final String clientAddress) {
        final String key = normalize(username);
        if (key == null) {
            return;
        }
        final String address = clientAddress == null ? UNKNOWN_ADDRESS : clientAddress;
        final Instant now = clock.instant();
        // Never let the bookkeeping break the sign-in flow (e.g. two concurrent inserts of the same key).
        try {
            transactionTemplate.executeWithoutResult(status -> {
                repository.deleteAllExpired(now.minus(lockDuration));
                repository.findByUsernameAndClientIp(key, address).ifPresentOrElse(
                        attempt -> attempt.registerFailure(now, lockDuration),
                        () -> repository.save(new LoginAttempt(key, address, now)));
            });
        } catch (RuntimeException e) {
            log.warn("Could not record a failed login attempt", e);
        }
    }

    public void loginSucceeded(final String username) {
        final String key = normalize(username);
        if (key == null) {
            return;
        }
        try {
            transactionTemplate.executeWithoutResult(status -> repository.deleteAllByUsername(key));
        } catch (RuntimeException e) {
            log.warn("Could not reset the failed login attempts", e);
        }
    }

    /**
     * @return whether the username is locked for the client address of the current HTTP request
     */
    public boolean isLocked(final String username) {
        return isLocked(username, currentRequestAddress());
    }

    public boolean isLocked(final String username, final String clientAddress) {
        final String key = normalize(username);
        if (key == null) {
            return false;
        }
        final String address = clientAddress == null ? UNKNOWN_ADDRESS : clientAddress;
        return repository.findByUsernameAndClientIp(key, address)
                .filter(attempt -> !attempt.getLastFailure().plus(lockDuration).isBefore(clock.instant()))
                .map(attempt -> attempt.getFailures() >= maxAttempts)
                .orElse(false);
    }

    private static String clientAddress(final Authentication authentication) {
        if (authentication.getDetails() instanceof WebAuthenticationDetails details) {
            return details.getRemoteAddress();
        }
        return currentRequestAddress();
    }

    private static String currentRequestAddress() {
        if (RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes attributes) {
            return attributes.getRequest().getRemoteAddr();
        }
        return null;
    }

    private static String normalize(final String username) {
        if (username == null || username.isBlank()) {
            return null;
        }
        final String key = username.trim().toLowerCase(Locale.ROOT);
        return key.length() > MAX_USERNAME_LENGTH ? key.substring(0, MAX_USERNAME_LENGTH) : key;
    }
}
