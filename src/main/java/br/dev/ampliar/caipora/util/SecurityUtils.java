package br.dev.ampliar.caipora.util;

import br.dev.ampliar.caipora.model.WebFormsUserDetails;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

/**
 * Access to the currently authenticated user.
 */
public final class SecurityUtils {

    private SecurityUtils() {
        throw new AssertionError("No instances for you!");
    }

    public static Optional<WebFormsUserDetails> currentUser() {
        final Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()
                && authentication.getPrincipal() instanceof WebFormsUserDetails principal) {
            return Optional.of(principal);
        }
        return Optional.empty();
    }

    public static Integer currentUserId() {
        return currentUser().map(user -> user.id).orElse(null);
    }

}
