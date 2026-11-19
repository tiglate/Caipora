package br.dev.ampliar.caipora.config;

import br.dev.ampliar.caipora.domain.Department;
import br.dev.ampliar.caipora.domain.Role;
import br.dev.ampliar.caipora.domain.User;
import br.dev.ampliar.caipora.repos.DepartmentRepository;
import br.dev.ampliar.caipora.repos.RoleRepository;
import br.dev.ampliar.caipora.repos.UserRepository;
import br.dev.ampliar.caipora.util.UserRoles;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class AdminAccountInitializerTest {

    private UserRepository userRepository;
    private RoleRepository roleRepository;
    private DepartmentRepository departmentRepository;
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);
        roleRepository = mock(RoleRepository.class);
        departmentRepository = mock(DepartmentRepository.class);
        passwordEncoder = mock(PasswordEncoder.class);
        when(passwordEncoder.encode("initial-secret")).thenReturn("{bcrypt}encoded");
        when(roleRepository.findAll()).thenReturn(List.of(new Role(), new Role()));
        when(departmentRepository.findByNameIgnoreCase("IT")).thenReturn(Optional.of(new Department()));
    }

    private AdminAccountInitializer initializer(final String password) {
        return new AdminAccountInitializer(userRepository, roleRepository, departmentRepository, passwordEncoder,
                "admin", password);
    }

    @Test
    void createsAdministratorWithTemporaryPassword() {
        initializer("initial-secret").run(null);

        final ArgumentCaptor<User> saved = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(saved.capture());
        final User admin = saved.getValue();
        assertEquals("admin", admin.getUsername());
        assertEquals("{bcrypt}encoded", admin.getPassword());
        assertTrue(admin.getPasswordChangeRequired());
        assertTrue(admin.getEnabled());
        assertEquals(2, admin.getRoles().size());
    }

    @Test
    void createsDepartmentWhenMissing() {
        when(departmentRepository.findByNameIgnoreCase("IT")).thenReturn(Optional.empty());
        when(departmentRepository.save(any(Department.class))).thenAnswer(inv -> inv.getArgument(0));

        initializer("initial-secret").run(null);

        verify(departmentRepository).save(any(Department.class));
    }

    @Test
    void refusesToStartWithoutVariable() {
        assertThrows(IllegalStateException.class, () -> initializer("").run(null));
        assertThrows(IllegalStateException.class, () -> initializer("short").run(null));
        verify(userRepository, never()).save(any());
    }

    @Test
    void doesNothingWhenAnAdministratorExists() {
        when(userRepository.existsByEnabledTrueAndRolesName(UserRoles.ROLE_USER_MANAGE)).thenReturn(true);

        initializer("").run(null);

        verify(userRepository, never()).save(any());
    }

    @Test
    void neverTakesOverAnExistingAccount() {
        when(userRepository.existsByUsernameIgnoreCase("admin")).thenReturn(true);

        assertThrows(IllegalStateException.class, () -> initializer("initial-secret").run(null));
        verify(userRepository, never()).save(any());
    }
}
