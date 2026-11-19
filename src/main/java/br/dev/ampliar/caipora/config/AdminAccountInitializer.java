package br.dev.ampliar.caipora.config;

import br.dev.ampliar.caipora.domain.Department;
import br.dev.ampliar.caipora.domain.User;
import br.dev.ampliar.caipora.model.Gender;
import br.dev.ampliar.caipora.repos.DepartmentRepository;
import br.dev.ampliar.caipora.repos.RoleRepository;
import br.dev.ampliar.caipora.repos.UserRepository;
import br.dev.ampliar.caipora.util.UserRoles;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;

/**
 * Creates the administrator account on the first run, in every environment.
 * <p>
 * When no enabled account can manage users (e.g. a fresh database), an account named {@code caipora.admin.username}
 * is created with every role and the password given by the {@code ADMIN_INITIAL_PASSWORD} environment variable.
 * That password is temporary: it must be changed at the first sign-in. If the variable is missing in that situation
 * the application refuses to start, so a deployment is never left without an administrator or with a guessable one.
 * Afterwards the variable is ignored and can be removed.
 * <p>
 * Runs before the development data loaders ({@code @Order(1)}).
 */
@Component
@Order(1)
public class AdminAccountInitializer implements ApplicationRunner {

    static final int MIN_PASSWORD_LENGTH = 8;
    public static final String ADMIN_DEPARTMENT = "IT";

    private static final Logger log = LoggerFactory.getLogger(AdminAccountInitializer.class);

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final DepartmentRepository departmentRepository;
    private final PasswordEncoder passwordEncoder;
    private final String adminUsername;
    private final String initialPassword;

    public AdminAccountInitializer(final UserRepository userRepository,
                                   final RoleRepository roleRepository,
                                   final DepartmentRepository departmentRepository,
                                   final PasswordEncoder passwordEncoder,
                                   @Value("${caipora.admin.username:admin}") final String adminUsername,
                                   @Value("${caipora.admin.initial-password:}") final String initialPassword) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.departmentRepository = departmentRepository;
        this.passwordEncoder = passwordEncoder;
        this.adminUsername = adminUsername;
        this.initialPassword = initialPassword;
    }

    @Override
    @Transactional
    public void run(final ApplicationArguments args) {
        if (userRepository.existsByEnabledTrueAndRolesName(UserRoles.ROLE_USER_MANAGE)) {
            return; // an administrator already exists
        }
        if (initialPassword == null || initialPassword.isBlank()) {
            throw new IllegalStateException("No administrator account exists. Set the ADMIN_INITIAL_PASSWORD " +
                    "environment variable (at least " + MIN_PASSWORD_LENGTH + " characters) to create the '" +
                    adminUsername + "' account; the password will have to be changed at the first sign-in.");
        }
        if (initialPassword.length() < MIN_PASSWORD_LENGTH) {
            throw new IllegalStateException("ADMIN_INITIAL_PASSWORD must have at least " + MIN_PASSWORD_LENGTH +
                    " characters.");
        }
        if (userRepository.existsByUsernameIgnoreCase(adminUsername)) {
            // never silently take over an existing account
            throw new IllegalStateException("No enabled account can manage users, but an account named '" +
                    adminUsername + "' already exists. Enable it (and grant " + UserRoles.ROLE_USER_MANAGE +
                    ") directly in the database, or set caipora.admin.username to create a different administrator.");
        }

        final User admin = new User();
        admin.setName("Administrator");
        admin.setUsername(adminUsername);
        admin.setGender(Gender.MALE); // mandatory field; can be edited later
        admin.setDepartment(adminDepartment());
        admin.setEnabled(true);
        admin.setRoles(new HashSet<>(roleRepository.findAll()));
        admin.setPassword(passwordEncoder.encode(initialPassword));
        admin.setPasswordChangeRequired(true);
        userRepository.save(admin);
        log.info("Administrator account '{}' created with the password from ADMIN_INITIAL_PASSWORD; " +
                "it must be changed at the first sign-in.", adminUsername);
    }

    private Department adminDepartment() {
        return departmentRepository.findByNameIgnoreCase(ADMIN_DEPARTMENT).orElseGet(() -> {
            final Department department = new Department();
            department.setName(ADMIN_DEPARTMENT);
            return departmentRepository.save(department);
        });
    }
}
