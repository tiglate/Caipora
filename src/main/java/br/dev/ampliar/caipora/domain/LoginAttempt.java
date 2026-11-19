package br.dev.ampliar.caipora.domain;

import jakarta.persistence.*;

import java.time.Instant;

/**
 * Consecutive failed sign-in attempts of a username from a client address (see LoginAttemptService).
 */
@Entity
@Table(name = "tb_login_attempt",
        uniqueConstraints = @UniqueConstraint(name = "uq_login_attempt", columnNames = {"username", "client_ip"}))
public class LoginAttempt {

    @Id
    @Column(name = "id_login_attempt", nullable = false, updatable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String username;

    @Column(name = "client_ip", nullable = false, length = 64)
    private String clientIp;

    @Column(nullable = false)
    private int failures;

    @Column(name = "last_failure", nullable = false)
    private Instant lastFailure;

    protected LoginAttempt() {
    }

    public LoginAttempt(final String username, final String clientIp, final Instant now) {
        this.username = username;
        this.clientIp = clientIp;
        this.failures = 1;
        this.lastFailure = now;
    }

    public Long getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getClientIp() {
        return clientIp;
    }

    public int getFailures() {
        return failures;
    }

    public Instant getLastFailure() {
        return lastFailure;
    }

    /**
     * Registers one more failure; a streak older than the lock window starts over.
     */
    public void registerFailure(final Instant now, final java.time.Duration window) {
        if (lastFailure.plus(window).isBefore(now)) {
            failures = 1;
        } else {
            failures++;
        }
        lastFailure = now;
    }
}
