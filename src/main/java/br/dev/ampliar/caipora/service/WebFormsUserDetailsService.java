package br.dev.ampliar.caipora.service;

import br.dev.ampliar.caipora.model.WebFormsUserDetails;
import br.dev.ampliar.caipora.repos.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;


@Service
public class WebFormsUserDetailsService implements UserDetailsService {

    private static final Logger log = LoggerFactory.getLogger(WebFormsUserDetailsService.class);

    private final UserRepository userRepository;
    private final LoginAttemptService loginAttemptService;

    public WebFormsUserDetailsService(final UserRepository userRepository,
                                      final LoginAttemptService loginAttemptService) {
        this.userRepository = userRepository;
        this.loginAttemptService = loginAttemptService;
    }

    @Override
    @Transactional(readOnly = true)
    public WebFormsUserDetails loadUserByUsername(final String username) {
        final var user = userRepository.findByUsernameIgnoreCase(username);
        if (user == null) {
            // Do not echo the raw (user-controlled) value into the logs: it may contain CR/LF
            // characters that could forge log entries.
            log.debug("Authentication attempt for an unknown username");
            throw new UsernameNotFoundException("Bad credentials");
        }
        final List<SimpleGrantedAuthority> authorities = user.getRoles() == null ? Collections.emptyList() :
                user.getRoles()
                .stream()
                .map(roleRef -> new SimpleGrantedAuthority(roleRef.getName()))
                .toList();
        // Disabled accounts must not be able to authenticate (previously every account was treated as enabled).
        final boolean enabled = Boolean.TRUE.equals(user.getEnabled());
        // Too many failed attempts: the account is temporarily locked (checked before the password is verified).
        final boolean locked = loginAttemptService.isLocked(username);
        return new WebFormsUserDetails(user.getId(), user.getUsername(), user.getName(), user.getPassword(),
                enabled, !locked, Boolean.TRUE.equals(user.getPasswordChangeRequired()), authorities);
    }

}
