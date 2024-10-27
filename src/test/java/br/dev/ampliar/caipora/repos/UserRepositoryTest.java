package br.dev.ampliar.caipora.repos;

import br.dev.ampliar.caipora.CaiporaApplication;
import br.dev.ampliar.caipora.config.DomainConfig;
import br.dev.ampliar.caipora.domain.Department;
import br.dev.ampliar.caipora.domain.User;
import br.dev.ampliar.caipora.model.Gender;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@ContextConfiguration(classes = {CaiporaApplication.class, DomainConfig.class})
class UserRepositoryTest {

    @Autowired
    private UserRepository repository;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Test
    void testUserCreationAndRetrievalById() {
        var department = new Department();
        department.setName("IT");
        department.setEmail("it@test.com");
        var savedDepartment = departmentRepository.save(department);

        var user = new User();
        user.setEnabled(true);
        user.setName("John Doe");
        user.setGender(Gender.MALE);
        user.setUsername("johndoe");
        user.setPassword("{noop}123456");
        user.setEmail("johndoe@test.com");
        user.setDepartment(savedDepartment);

        var savedUser = repository.save(user);
        assertNotNull(savedUser.getId());
        assertNotNull(savedUser.getCreatedAt());
        assertNotNull(savedUser.getUpdatedAt());

        var foundUser = repository.findById(savedUser.getId()).orElse(null);
        assertNotNull(foundUser);
        assertEquals(user.getName(), foundUser.getName());
        assertEquals(user.getUsername(), foundUser.getUsername());
        assertEquals(user.getEmail(), foundUser.getEmail());
    }

    @Test
    void testUserUpdate() {
        var department = new Department();
        department.setName("HR");
        department.setEmail("hr@test.com");
        var savedDepartment = departmentRepository.save(department);

        var user = new User();
        user.setEnabled(true);
        user.setName("Jane Doe");
        user.setGender(Gender.MALE);
        user.setUsername("janedoe");
        user.setPassword("{noop}123456");
        user.setEmail("janedoe@test.com");
        user.setDepartment(savedDepartment);

        var savedUser = repository.save(user);

        savedUser.setName("Jane Smith");
        savedUser.setEmail("janesmith@test.com");
        repository.save(savedUser);

        var updatedUser = repository.findById(savedUser.getId()).orElse(null);
        assertNotNull(updatedUser);

        assertEquals(savedUser.getName(), updatedUser.getName());
        assertEquals(savedUser.getEmail(), updatedUser.getEmail());
    }

    @Test
    void testUserDeletion() {
        var department = new Department();
        department.setName("Finance");
        department.setEmail("finance@test.com");
        var savedDepartment = departmentRepository.save(department);

        var user = new User();
        user.setEnabled(true);
        user.setName("Mark Doe");
        user.setGender(Gender.MALE);
        user.setUsername("markdoe");
        user.setPassword("{noop}123456");
        user.setEmail("markdoe@test.com");
        user.setDepartment(savedDepartment);

        var savedUser = repository.save(user);
        assertNotNull(savedUser.getId());

        repository.delete(savedUser);

        var deletedUser = repository.findById(savedUser.getId()).orElse(null);
        assertNull(deletedUser);
    }

    static Stream<Arguments> provideSearchCriteria() {
        return Stream.of(
                Arguments.of("johndoe", null, null, null, 1, "John Doe"),
                Arguments.of(null, "Jane Smith", null, null, 1, "Jane Smith"),
                Arguments.of(null, null, "Legal", null, 1, "John Doe"),
                Arguments.of(null, null, null, true, 2, null)
        );
    }

    @ParameterizedTest
    @MethodSource("provideSearchCriteria")
    void testFindAllBySearchCriteria(String username, String name, String departmentName, Boolean enabled, int expectedCount, String expectedName) {
        var department1 = new Department();
        department1.setName("Legal");
        department1.setEmail("legal@test.com");
        departmentRepository.save(department1);

        var department2 = new Department();
        department2.setName("FLDS");
        department2.setEmail("flds@test.com");
        departmentRepository.save(department2);

        var user1 = new User();
        user1.setEnabled(true);
        user1.setName("John Doe");
        user1.setGender(Gender.MALE);
        user1.setUsername("johndoe");
        user1.setPassword("{noop}123456");
        user1.setEmail("johndoe@test.com");
        user1.setDepartment(department1);
        user1.setEnabled(true);
        repository.save(user1);

        var user2 = new User();
        user2.setEnabled(false);
        user2.setName("Jane Smith");
        user2.setGender(Gender.FEMALE);
        user2.setUsername("janesmith");
        user2.setPassword("{noop}123456");
        user2.setEmail("janesmith@test.com");
        user2.setDepartment(department2);
        user2.setEnabled(true);
        repository.save(user2);

        Integer departmentId = null;
        var department = departmentRepository.findByNameIgnoreCase(departmentName);

        if (department.isPresent()) {
            departmentId = department.get().getId();
        }

        var pageable = PageRequest.of(0, 10);
        var result = repository.findAllBySearchCriteria(username, name, departmentId, enabled, pageable);

        assertEquals(expectedCount, result.getTotalElements());
        if (expectedName != null) {
            assertEquals(expectedName, result.getContent().get(0).getName());
        }
    }
}