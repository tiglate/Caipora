package br.dev.ampliar.caipora.service;

import br.dev.ampliar.caipora.domain.Role;
import br.dev.ampliar.caipora.domain.User;
import br.dev.ampliar.caipora.model.WebFormsUserDetails;
import br.dev.ampliar.caipora.repos.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class WebFormsUserDetailsServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private LoginAttemptService loginAttemptService;

    @InjectMocks
    private WebFormsUserDetailsService userDetailsService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    private static User user(final Boolean enabled) {
        var user = new User();
        user.setId(1);
        user.setUsername("testuser");
        user.setName("Test User");
        user.setPassword("password");
        user.setEnabled(enabled);
        user.setRoles(new HashSet<>(Collections.emptyList()));
        return user;
    }

    @Test
    void testLoadUserByUsername_UserFound() {
        when(userRepository.findByUsernameIgnoreCase("testuser")).thenReturn(user(true));

        WebFormsUserDetails userDetails = userDetailsService.loadUserByUsername("testuser");

        assertNotNull(userDetails);
        assertEquals("testuser", userDetails.getUsername());
        assertEquals("Test User", userDetails.getDisplayName());
        assertEquals("password", userDetails.getPassword());
        assertTrue(userDetails.isEnabled());
        assertTrue(userDetails.isAccountNonLocked());
        assertTrue(userDetails.getAuthorities().isEmpty());
        verify(userRepository, times(1)).findByUsernameIgnoreCase("testuser");
    }

    @Test
    void testLoadUserByUsername_UsesCanonicalUsername() {
        when(userRepository.findByUsernameIgnoreCase("TESTUSER")).thenReturn(user(true));

        WebFormsUserDetails userDetails = userDetailsService.loadUserByUsername("TESTUSER");

        assertEquals("testuser", userDetails.getUsername());
    }

    @Test
    void testLoadUserByUsername_DisabledUserIsNotEnabled() {
        when(userRepository.findByUsernameIgnoreCase("testuser")).thenReturn(user(false));

        WebFormsUserDetails userDetails = userDetailsService.loadUserByUsername("testuser");

        assertFalse(userDetails.isEnabled());
    }

    @Test
    void testLoadUserByUsername_LockedAfterTooManyAttempts() {
        when(userRepository.findByUsernameIgnoreCase("testuser")).thenReturn(user(true));
        when(loginAttemptService.isLocked("testuser")).thenReturn(true);

        WebFormsUserDetails userDetails = userDetailsService.loadUserByUsername("testuser");

        assertFalse(userDetails.isAccountNonLocked());
    }

    @Test
    void testLoadUserByUsername_UserNotFound() {
        when(userRepository.findByUsernameIgnoreCase("unknownuser")).thenReturn(null);

        assertThrows(UsernameNotFoundException.class, () -> userDetailsService.loadUserByUsername("unknownuser"));

        verify(userRepository, times(1)).findByUsernameIgnoreCase("unknownuser");
    }

    @Test
    void testLoadUserByUsername_UserWithRoles() {
        var user = user(true);
        var role = new Role();
        role.setName("ROLE_USER");
        user.setRoles(new HashSet<>(List.of(role)));

        when(userRepository.findByUsernameIgnoreCase("testuser")).thenReturn(user);

        WebFormsUserDetails userDetails = userDetailsService.loadUserByUsername("testuser");

        assertNotNull(userDetails);
        assertEquals(1, userDetails.getAuthorities().size());
        assertTrue(userDetails.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_USER")));
    }
}
