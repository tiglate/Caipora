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
import jakarta.transaction.Transactional;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;


@Service
@Transactional
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
    public Integer create(final UserDTO userDTO) {
        final var user = mapToEntity(userDTO);
        return userRepository.save(user).getId();
    }

    @Override
    public void update(final Integer id, final UserDTO userDTO) {
        final User user = userRepository.findById(id)
                .orElseThrow(NotFoundException::new);
        mapToEntity(userDTO, user);
        userRepository.save(user);
    }

    @Override
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
        userDTO.setDepartmentId(user.getDepartment() == null ? null : user.getDepartment().getId());
        userDTO.setCreatedAt(user.getCreatedAt());
        userDTO.setUpdatedAt(user.getUpdatedAt());
        userDTO.setRoles(user.getRoles().stream()
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
        user.setPassword(userDTO.getPassword() == null || userDTO.getPassword().trim().isEmpty() ? user.getPassword() : passwordEncoder.encode(userDTO.getPassword()));
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