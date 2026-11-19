package br.dev.ampliar.caipora.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.session.FindByIndexNameSessionRepository;
import org.springframework.session.Session;
import org.springframework.stereotype.Service;

import java.util.Objects;

/**
 * Ends the active sessions of a user, so that changes to an account (disabled, deleted, new password, different
 * roles) take effect immediately instead of when the sessions expire.
 * <p>
 * Remember-me cookies need no extra handling: they are rejected once the account is disabled or deleted, are signed
 * with the password hash (so a new password invalidates them) and re-load the roles on every automatic sign-in.
 */
@Service
public class UserSessionService {

    private static final Logger log = LoggerFactory.getLogger(UserSessionService.class);

    private final ObjectProvider<FindByIndexNameSessionRepository<? extends Session>> sessionRepository;

    public UserSessionService(final ObjectProvider<FindByIndexNameSessionRepository<? extends Session>> sessionRepository) {
        this.sessionRepository = sessionRepository;
    }

    /**
     * @param username         the principal name the sessions were created with
     * @param keepSessionId    id of a session to keep (the caller's own), or {@code null}
     * @return number of sessions ended
     */
    public int invalidateSessions(final String username, final String keepSessionId) {
        final var repository = sessionRepository.getIfAvailable();
        if (repository == null) {
            log.warn("No indexed session repository available: sessions of user {} cannot be ended", username);
            return 0;
        }
        if (username == null) {
            return 0;
        }
        int count = 0;
        for (final String sessionId : repository.findByPrincipalName(username).keySet()) {
            if (!Objects.equals(sessionId, keepSessionId)) {
                repository.deleteById(sessionId);
                count++;
            }
        }
        if (count > 0) {
            log.info("Ended {} active session(s) of user {}", count, username);
        }
        return count;
    }
}
