package br.dev.ampliar.caipora.model;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.util.Collection;


/**
 * Extension of Spring Security User class to store additional data.
 */
public class WebFormsUserDetails extends User {

    public final Integer id;

    public WebFormsUserDetails(final Integer id, final String username, final String hash,
                               final Collection<? extends GrantedAuthority> authorities) {
        super(username, hash, authorities);
        this.id = id;
    }
}
