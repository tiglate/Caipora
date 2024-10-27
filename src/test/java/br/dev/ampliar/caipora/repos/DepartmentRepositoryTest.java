package br.dev.ampliar.caipora.repos;

import br.dev.ampliar.caipora.CaiporaApplication;
import br.dev.ampliar.caipora.config.DomainConfig;
import br.dev.ampliar.caipora.domain.Department;
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
class DepartmentRepositoryTest {

    @Autowired
    private DepartmentRepository repository;

    @Test
    void testDepartmentCreationAndRetrievalById() {
        var department = new Department();
        department.setName("Finance");
        department.setEmail("finance@test.com");

        var savedDepartment = repository.save(department);
        assertNotNull(savedDepartment.getId());
        assertNotNull(savedDepartment.getCreatedAt());
        assertNotNull(savedDepartment.getUpdatedAt());

        var foundDepartment = repository.findById(savedDepartment.getId()).orElse(null);
        assertNotNull(foundDepartment);
        assertEquals(department.getName(), foundDepartment.getName());
        assertEquals(department.getEmail(), foundDepartment.getEmail());
    }

    @Test
    void testDepartmentUpdate() {
        var department = new Department();
        department.setName("Compliance");
        department.setEmail("compliance@test.com");

        var savedDepartment = repository.save(department);

        savedDepartment.setName("IT");
        savedDepartment.setEmail("it@test.com");
        repository.save(savedDepartment);

        var updatedDepartment = repository.findById(savedDepartment.getId()).orElse(null);
        assertNotNull(updatedDepartment);

        assertEquals(savedDepartment.getName(), updatedDepartment.getName());
        assertEquals(savedDepartment.getEmail(), updatedDepartment.getEmail());
    }

    @Test
    void testDepartmentDeletion() {
        var department = new Department();
        department.setName("HR");
        department.setEmail("hr@test.com");

        var savedDepartment = repository.save(department);
        assertNotNull(savedDepartment.getId());

        repository.delete(savedDepartment);

        var deletedDepartment = repository.findById(savedDepartment.getId()).orElse(null);
        assertNull(deletedDepartment);
    }

    static Stream<Arguments> provideSearchCriteria() {
        return Stream.of(
                Arguments.of("Finance", null, 1, "Finance"),
                Arguments.of(null, "it@test.com", 1, "IT"),
                Arguments.of("IT", "it@test.com", 1, "IT"),
                Arguments.of(null, null, 2, null)
        );
    }

    @ParameterizedTest
    @MethodSource("provideSearchCriteria")
    void testFindAllBySearchCriteria(String name, String email, int expectedCount, String expectedName) {
        var department1 = new Department();
        department1.setName("Finance");
        department1.setEmail("finance@test.com");
        repository.save(department1);

        var department2 = new Department();
        department2.setName("IT");
        department2.setEmail("it@test.com");
        repository.save(department2);

        var pageable = PageRequest.of(0, 10);
        var result = repository.findAllBySearchCriteria(name, email, pageable);

        assertEquals(expectedCount, result.getTotalElements());
        if (expectedName != null) {
            assertEquals(expectedName, result.getContent().get(0).getName());
        }
    }
}
