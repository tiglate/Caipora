package br.dev.ampliar.caipora.service;

import br.dev.ampliar.caipora.domain.Deploy;
import br.dev.ampliar.caipora.domain.Role;
import br.dev.ampliar.caipora.domain.User;
import br.dev.ampliar.caipora.model.UserDTO;
import br.dev.ampliar.caipora.model.UserSearchDTO;
import br.dev.ampliar.caipora.repos.DeployRepository;
import br.dev.ampliar.caipora.repos.RoleRepository;
import br.dev.ampliar.caipora.repos.UserRepository;
import br.dev.ampliar.caipora.util.UserRoles;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private DeployRepository deployRepository;

    @InjectMocks
    private UserServiceImpl userService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testFindAll() {
        var filter = new UserSearchDTO();
        var pageable = PageRequest.of(0, 10);
        var user = new UserDTO();
        user.setName("John Doe");
        var page = new PageImpl<>(Collections.singletonList(user));

        when(userRepository.findAllBySearchCriteria(any(), any(), any(), any(), any())).thenReturn(page);

        var result = userService.findAll(filter, pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(userRepository, times(1)).findAllBySearchCriteria(any(), any(), any(), any(), any());
    }

    @Test
    void testGet() {
        var role = new Role();
        role.setName(UserRoles.ROLE_USER_VIEW);
        var user = new User();
        user.setId(1);
        user.setName("John Doe");
        user.setRoles(new HashSet<>(Collections.singletonList(role)));

        when(roleRepository.findAllById(any())).thenReturn(Collections.singletonList(role));
        when(userRepository.findById(1)).thenReturn(Optional.of(user));

        var result = userService.get(1);

        assertNotNull(result);
        assertEquals("John Doe", result.getName());
        verify(userRepository, times(1)).findById(1);
    }

    @Test
    void testCreate() {
        var role = new Role();
        role.setId(1);
        role.setName(UserRoles.ROLE_USER_VIEW);

        var userDTO = new UserDTO();
        userDTO.setName("John Doe");
        userDTO.setRoles(Collections.singletonList(role.getId()));

        var user = new User();
        user.setId(1);
        user.setRoles(new HashSet<>(Collections.singletonList(role)));

        when(roleRepository.findAllById(any())).thenReturn(Collections.singletonList(role));
        when(userRepository.save(any(User.class))).thenReturn(user);

        var id = userService.create(userDTO);

        assertNotNull(id);
        assertEquals(1, id);
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void testUpdate() {
        var userDTO = new UserDTO();
        userDTO.setName("John Doe");
        var user = new User();
        user.setId(1);

        when(userRepository.findById(1)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(user);

        userService.update(1, userDTO);

        verify(userRepository, times(1)).findById(1);
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void testDelete() {
        userService.delete(1);

        verify(userRepository, times(1)).deleteById(1);
    }

    @Test
    void testUsernameExists() {
        when(userRepository.existsByUsernameIgnoreCase("johndoe")).thenReturn(true);

        boolean exists = userService.usernameExists("johndoe");

        assertTrue(exists);
        verify(userRepository, times(1)).existsByUsernameIgnoreCase("johndoe");
    }

    @Test
    void testGetReferencedWarning_ReferencedByDeploy() {
        var user = new User();
        user.setId(1);
        var deploy = new Deploy();
        deploy.setId(1);

        when(userRepository.findById(1)).thenReturn(Optional.of(user));
        when(deployRepository.findFirstByOperator(user)).thenReturn(deploy);

        var warning = userService.getReferencedWarning(1);

        assertNotNull(warning);
        assertEquals("user.deploy.operator.referenced", warning.getKey());
    }
}