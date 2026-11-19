package br.dev.ampliar.caipora.service;

import br.dev.ampliar.caipora.domain.Department;
import br.dev.ampliar.caipora.domain.Deploy;
import br.dev.ampliar.caipora.domain.Role;
import br.dev.ampliar.caipora.domain.User;
import br.dev.ampliar.caipora.model.UserDTO;
import br.dev.ampliar.caipora.model.UserSearchDTO;
import br.dev.ampliar.caipora.repos.DepartmentRepository;
import br.dev.ampliar.caipora.repos.DeployRepository;
import br.dev.ampliar.caipora.repos.RoleRepository;
import br.dev.ampliar.caipora.repos.UserRepository;
import br.dev.ampliar.caipora.util.NotFoundException;
import br.dev.ampliar.caipora.util.ReferencedWarning;
import br.dev.ampliar.caipora.util.SecurityUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;
import java.util.List;


@Service
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final DepartmentRepository departmentRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final DeployRepository deployRepository;

    public UserServiceImpl(final UserRepository userRepository,
                           final DepartmentRepository departmentRepository, final RoleRepository roleRepository,
                           final PasswordEncoder passwordEncoder, final DeployRepository deployRepository) {
        this.userRepository = userRepository;
        this.departmentRepository = departmentRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.deployRepository = deployRepository;
    }

    @Override
    public Page<UserDTO> findAll(UserSearchDTO searchDTO, Pageable pageable) {
        return userRepository.findAllBySearchCriteria(
                searchDTO.getUsername(),
                searchDTO.getName(),
                searchDTO.getDepartment(),
                searchDTO.getEnabled(),
                pageable
        );
    }

    @Override
    public UserDTO get(final Integer id) {
        return userRepository.findById(id)
                .map(this::mapToDTO)
                .orElseThrow(NotFoundException::new);
    }

    @Override
    @Transactional
    public Integer create(final UserDTO userDTO) {
        final var user = mapToEntity(userDTO);
        // the password was chosen by an administrator: the user must replace it at the first sign-in
        user.setPasswordChangeRequired(true);
        return userRepository.save(user).getId();
    }

    @Override
    @Transactional
    public void update(final Integer id, final UserDTO userDTO) {
        final User user = userRepository.findById(id)
                .orElseThrow(NotFoundException::new);
        mapToEntity(userDTO, user);
        final boolean passwordReset = userDTO.getPassword() != null && !userDTO.getPassword().isBlank();
        if (passwordReset && !id.equals(SecurityUtils.currentUserId())) {
            // password reset by an administrator: it is temporary
            user.setPasswordChangeRequired(true);
        }
        userRepository.save(user);
    }

    @Override
    public Map<Integer, Set<Integer>> getRoleIds(final Collection<Integer> userIds) {
        final Map<Integer, Set<Integer>> result = new HashMap<>();
        if (userIds == null || userIds.isEmpty()) {
            return result;
        }
        userIds.forEach(id -> result.put(id, new HashSet<>()));
        for (final Object[] row : userRepository.findRoleIdsByUserIds(userIds)) {
            result.computeIfAbsent((Integer) row[0], id -> new HashSet<>()).add((Integer) row[1]);
        }
        return result;
    }

    @Override
    public boolean passwordMatches(final Integer id, final String rawPassword) {
        final User user = userRepository.findById(id).orElseThrow(NotFoundException::new);
        return rawPassword != null && passwordEncoder.matches(rawPassword, user.getPassword());
    }

    @Override
    @Transactional
    public void changeOwnPassword(final Integer id, final String newPassword) {
        final User user = userRepository.findById(id).orElseThrow(NotFoundException::new);
        user.setPassword(passwordEncoder.encode(newPassword));
        user.setPasswordChangeRequired(false);
        userRepository.save(user);
    }

    @Override
    @Transactional
    public void delete(final Integer id) {
        userRepository.deleteById(id);
    }

    private UserDTO mapToDTO(final User user) {
        UserDTO userDTO = new UserDTO();
        userDTO.setId(user.getId());
        userDTO.setName(user.getName());
        userDTO.setEmail(user.getEmail());
        userDTO.setGender(user.getGender());
        userDTO.setUsername(user.getUsername());
        userDTO.setEnabled(user.getEnabled());
        if (user.getDepartment() != null) {
            userDTO.setDepartmentId(user.getDepartment().getId());
            userDTO.setDepartmentName(user.getDepartment().getName());
        }
        userDTO.setCreatedAt(user.getCreatedAt());
        userDTO.setUpdatedAt(user.getUpdatedAt());
        userDTO.setRoles(user.getRoles() == null ? List.of() : user.getRoles().stream()
                .map(Role::getId)
                .toList());
        return userDTO;
    }

    private User mapToEntity(final UserDTO userDTO) {
        return mapToEntity(userDTO, new User());
    }

    private User mapToEntity(final UserDTO userDTO, final User user) {
        user.setName(userDTO.getName());
        user.setEmail(userDTO.getEmail());
        user.setGender(userDTO.getGender());
        user.setUsername(userDTO.getUsername());
        // a blank password on update keeps the current one
        if (userDTO.getPassword() != null && !userDTO.getPassword().isBlank()) {
            user.setPassword(passwordEncoder.encode(userDTO.getPassword()));
        }
        user.setEnabled(userDTO.getEnabled());
        final Department department = userDTO.getDepartmentId() == null ? null : departmentRepository.findById(userDTO.getDepartmentId())
                .orElseThrow(() -> new NotFoundException("department not found"));
        user.setDepartment(department);
        final List<Role> roles = roleRepository.findAllById(
                userDTO.getRoles() == null ? Collections.emptyList() : userDTO.getRoles());
        if (roles.size() != (userDTO.getRoles() == null ? 0 : userDTO.getRoles().size())) {
            throw new NotFoundException("one of roles not found");
        }
        user.setRoles(new HashSet<>(roles));
        return user;
    }

    @Override
    public boolean usernameExists(final String username) {
        return userRepository.existsByUsernameIgnoreCase(username);
    }

    @Override
    public ReferencedWarning getReferencedWarning(final Integer id) {
        final ReferencedWarning referencedWarning = new ReferencedWarning();
        final User user = userRepository.findById(id)
                .orElseThrow(NotFoundException::new);
        final Deploy operatorDeploy = deployRepository.findFirstByOperator(user);
        if (operatorDeploy != null) {
            referencedWarning.setKey("user.deploy.operator.referenced");
            referencedWarning.addParam(operatorDeploy.getId());
            return referencedWarning;
        }
        return null;
    }

}