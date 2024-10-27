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

    @InjectMocks
    private WebFormsUserDetailsService userDetailsService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testLoadUserByUsername_UserFound() {
        var user = new User();
        user.setId(1);
        user.setUsername("testuser");
        user.setPassword("password");
        user.setRoles(new HashSet<>(Collections.emptyList()));

        when(userRepository.findByUsernameIgnoreCase("testuser")).thenReturn(user);

        WebFormsUserDetails userDetails = userDetailsService.loadUserByUsername("testuser");

        assertNotNull(userDetails);
        assertEquals("testuser", userDetails.getUsername());
        assertEquals("password", userDetails.getPassword());
        assertTrue(userDetails.getAuthorities().isEmpty());
        verify(userRepository, times(1)).findByUsernameIgnoreCase("testuser");
    }

    @Test
    void testLoadUserByUsername_UserNotFound() {
        when(userRepository.findByUsernameIgnoreCase("unknownuser")).thenReturn(null);

        assertThrows(UsernameNotFoundException.class, () -> {
            userDetailsService.loadUserByUsername("unknownuser");
        });

        verify(userRepository, times(1)).findByUsernameIgnoreCase("unknownuser");
    }

    @Test
    void testLoadUserByUsername_UserWithRoles() {
        var user = new User();
        user.setId(1);
        user.setUsername("testuser");
        user.setPassword("password");
        var role = new Role();
        role.setName("ROLE_USER");
        user.setRoles(new HashSet<>(List.of(role)));

        when(userRepository.findByUsernameIgnoreCase("testuser")).thenReturn(user);

        WebFormsUserDetails userDetails = userDetailsService.loadUserByUsername("testuser");

        assertNotNull(userDetails);
        assertEquals("testuser", userDetails.getUsername());
        assertEquals("password", userDetails.getPassword());
        assertEquals(1, userDetails.getAuthorities().size());
        assertTrue(userDetails.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_USER")));
        verify(userRepository, times(1)).findByUsernameIgnoreCase("testuser");
    }
}