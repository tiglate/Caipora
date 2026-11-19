package br.dev.ampliar.caipora.dev;

import br.dev.ampliar.caipora.domain.Department;
import br.dev.ampliar.caipora.domain.Role;
import br.dev.ampliar.caipora.domain.User;
import br.dev.ampliar.caipora.model.Gender;
import br.dev.ampliar.caipora.repos.DepartmentRepository;
import br.dev.ampliar.caipora.repos.RoleRepository;
import br.dev.ampliar.caipora.repos.UserRepository;
import br.dev.ampliar.caipora.util.RandomUtils;
import br.dev.ampliar.caipora.util.UserRoles;
import net.datafaker.Faker;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Creates a few users per department with random (unknown) passwords and random read-only permissions. To sign in
 * as one of them, reset the password with the administrator account.
 */
@Component
public class UsersLoader implements DataLoaderCommand {

    private static final int USERS_PER_DEPARTMENT = 3;
    private static final Set<String> EXCLUDED_ROLES = Set.of(
            UserRoles.ROLE_USER_MANAGE, UserRoles.ROLE_SWAGGER_ACCESS, UserRoles.ROLE_ACTUATOR_ACCESS);

    private final RandomUtils randomUtils;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final DepartmentRepository departmentRepository;
    private final PasswordEncoder passwordEncoder;

    public UsersLoader(final RandomUtils randomUtils,
                       final UserRepository userRepository,
                       final RoleRepository roleRepository,
                       final DepartmentRepository departmentRepository,
                       final PasswordEncoder passwordEncoder) {
        this.randomUtils = randomUtils;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.departmentRepository = departmentRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public int getOrder() {
        return 4;
    }

    @Override
    public String getName() {
        return "Users";
    }

    @Override
    public boolean canItRun() {
        // only the administrator created at startup exists
        return userRepository.count() <= 1;
    }

    @Override
    @Transactional
    public int run() {
        final Faker faker = new Faker();
        final List<Role> roles = roleRepository.findAll().stream()
                .filter(role -> !EXCLUDED_ROLES.contains(role.getName()))
                .toList();
        final Set<String> usernames = new HashSet<>();
        int count = 0;
        for (final Department department : departmentRepository.findAll()) {
            for (int i = 0; i < USERS_PER_DEPARTMENT; i++) {
                final String first = faker.name().firstName();
                final String last = faker.name().lastName();
                final String username = uniqueUsername(first, last, usernames);

                final User user = new User();
                user.setName(first + " " + last);
                user.setEmail(DevNames.email(first, last, faker));
                user.setGender(randomUtils.pickRandomEnumValue(Gender.class));
                user.setUsername(username);
                // random password nobody knows: reset it as administrator to sign in as this user
                user.setPassword(passwordEncoder.encode(UUID.randomUUID().toString()));
                user.setEnabled(randomUtils.chance(0.85));
                user.setPasswordChangeRequired(false);
                user.setDepartment(department);
                user.setRoles(new HashSet<>(randomUtils.createRandomSublist(roles, randomUtils.nextInt(2, 7))));
                userRepository.save(user);
                count++;
            }
        }
        return count;
    }

    private String uniqueUsername(final String first, final String last, final Set<String> taken) {
        final String base = (DevNames.slug(first) + "." + DevNames.slug(last));
        final String trimmed = base.length() > 45 ? base.substring(0, 45) : base;
        String username = trimmed;
        for (int i = 2; !taken.add(username) || userRepository.existsByUsernameIgnoreCase(username); i++) {
            username = trimmed + i;
        }
        return username;
    }
}
