package br.dev.ampliar.caipora.model;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.util.Collection;
import java.util.Objects;


/**
 * Extension of Spring Security User class to store additional data.
 */
public class WebFormsUserDetails extends User {

    public final Integer id;

    public WebFormsUserDetails(final Integer id, final String username, final String hash, final Collection<? extends GrantedAuthority> authorities) {
        super(username, hash, authorities);
        this.id = id;
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
