package br.dev.ampliar.caipora.model;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.util.Collection;
import java.util.Objects;


/**
 * Extension of Spring Security User class to store additional data.
 */
public class WebFormsUserDetails extends User {

    @java.io.Serial
    private static final long serialVersionUID = 3L;

    public final Integer id;

    /**
     * Display name of the user (not used for authentication).
     */
    private final String displayName;

    /**
     * The user must change the password before doing anything else.
     */
    private final boolean passwordChangeRequired;

    public WebFormsUserDetails(final Integer id, final String username, final String hash,
                               final Collection<? extends GrantedAuthority> authorities) {
        this(id, username, username, hash, true, true, false, authorities);
    }

    public WebFormsUserDetails(final Integer id, final String username, final String displayName, final String hash,
                               final boolean enabled, final boolean accountNonLocked,
                               final boolean passwordChangeRequired,
                               final Collection<? extends GrantedAuthority> authorities) {
        super(username, hash, enabled, true, true, accountNonLocked, authorities);
        this.id = id;
        this.displayName = displayName == null || displayName.isBlank() ? username : displayName;
        this.passwordChangeRequired = passwordChangeRequired;
    }

    public boolean isPasswordChangeRequired() {
        return passwordChangeRequired;
    }

    public String getDisplayName() {
        return displayName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        WebFormsUserDetails that = (WebFormsUserDetails) o;

        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), id);
    }
}
