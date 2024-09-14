package br.dev.ampliar.caipora.service;

import br.dev.ampliar.caipora.domain.User;
import br.dev.ampliar.caipora.model.WebFormsUserDetails;
import br.dev.ampliar.caipora.repos.UserRepository;
import java.util.Collections;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;


@Service
public class WebFormsUserDetailsService implements UserDetailsService {

    private static final Logger log = LoggerFactory.getLogger(WebFormsUserDetailsService.class);

    private final UserRepository userRepository;

    public WebFormsUserDetailsService(final UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public WebFormsUserDetails loadUserByUsername(final String username) {
        final User user = userRepository.findByUsernameIgnoreCase(username);
        if (user == null) {
            log.warn("user not found: {}", username);
            throw new UsernameNotFoundException("User " + username + " not found");
        }
        final List<SimpleGrantedAuthority> authorities = user.getRoles() == null ? Collections.emptyList() : 
                user.getRoles()
                .stream()
                .map(roleRef -> new SimpleGrantedAuthority(roleRef.getName()))
                .toList();
        return new WebFormsUserDetails(user.getId(), username, user.getPassword(), authorities);
    }

}
