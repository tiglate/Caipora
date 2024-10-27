package br.dev.ampliar.caipora.repos;

import br.dev.ampliar.caipora.CaiporaApplication;
import br.dev.ampliar.caipora.config.DomainConfig;
import br.dev.ampliar.caipora.domain.Department;
import br.dev.ampliar.caipora.domain.Stakeholder;
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
class StakeholderRepositoryTest {

    @Autowired
    private StakeholderRepository repository;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Test
    void testStakeholderCreationAndRetrievalById() {
        var department = new Department();
        department.setName("IT");
        department.setEmail("it@test.com");
        var savedDepartment = departmentRepository.save(department);

        var stakeholder = new Stakeholder();
        stakeholder.setName("John Doe");
        stakeholder.setEmail("johndoe@test.com");
        stakeholder.setGender(Gender.MALE);
        stakeholder.setDepartment(savedDepartment);

        var savedStakeholder = repository.save(stakeholder);
        assertNotNull(savedStakeholder.getId());
        assertNotNull(savedStakeholder.getCreatedAt());
        assertNotNull(savedStakeholder.getUpdatedAt());

        var foundStakeholder = repository.findById(savedStakeholder.getId()).orElse(null);
        assertNotNull(foundStakeholder);
        assertEquals(stakeholder.getName(), foundStakeholder.getName());
        assertEquals(stakeholder.getEmail(), foundStakeholder.getEmail());
    }

    @Test
    void testStakeholderUpdate() {
        var department = new Department();
        department.setName("HR");
        department.setEmail("hr@test.com");
        var savedDepartment = departmentRepository.save(department);

        var stakeholder = new Stakeholder();
        stakeholder.setName("Jane Doe");
        stakeholder.setEmail("janedoe@test.com");
        stakeholder.setGender(Gender.FEMALE);
        stakeholder.setDepartment(savedDepartment);

        var savedStakeholder = repository.save(stakeholder);

        savedStakeholder.setName("Jane Smith");
        savedStakeholder.setEmail("janesmith@test.com");
        repository.save(savedStakeholder);

        var updatedStakeholder = repository.findById(savedStakeholder.getId()).orElse(null);
        assertNotNull(updatedStakeholder);

        assertEquals(savedStakeholder.getName(), updatedStakeholder.getName());
        assertEquals(savedStakeholder.getEmail(), updatedStakeholder.getEmail());
    }

    @Test
    void testStakeholderDeletion() {
        var department = new Department();
        department.setName("Finance");
        department.setEmail("finance@test.com");
        var savedDepartment = departmentRepository.save(department);

        var stakeholder = new Stakeholder();
        stakeholder.setName("Mark Doe");
        stakeholder.setEmail("markdoe@test.com");
        stakeholder.setGender(Gender.MALE);
        stakeholder.setDepartment(savedDepartment);

        var savedStakeholder = repository.save(stakeholder);
        assertNotNull(savedStakeholder.getId());

        repository.delete(savedStakeholder);

        var deletedStakeholder = repository.findById(savedStakeholder.getId()).orElse(null);
        assertNull(deletedStakeholder);
    }

    static Stream<Arguments> provideSearchCriteria() {
        return Stream.of(
                Arguments.of("John Doe", null, null, null, 1, "John Doe"),
                Arguments.of(null, "janesmith@test.com", null, null, 1, "Jane Smith"),
                Arguments.of(null, null, "Legal", null, 1, "John Doe"),
                Arguments.of(null, null, null, Gender.FEMALE, 1, "Jane Smith"),
                Arguments.of(null, null, null, null, 2, null)
        );
    }

    @ParameterizedTest
    @MethodSource("provideSearchCriteria")
    void testFindAllBySearchCriteria(String name, String email, String departmentName, Gender gender, int expectedCount, String expectedName) {
        var department1 = new Department();
        department1.setName("Legal");
        department1.setEmail("legal@test.com");
        departmentRepository.save(department1);

        var department2 = new Department();
        department2.setName("FLDS");
        department2.setEmail("flds@test.com");
        departmentRepository.save(department2);

        var stakeholder1 = new Stakeholder();
        stakeholder1.setName("John Doe");
        stakeholder1.setEmail("johndoe@test.com");
        stakeholder1.setGender(Gender.MALE);
        stakeholder1.setDepartment(department1);
        repository.save(stakeholder1);

        var stakeholder2 = new Stakeholder();
        stakeholder2.setName("Jane Smith");
        stakeholder2.setEmail("janesmith@test.com");
        stakeholder2.setGender(Gender.FEMALE);
        stakeholder2.setDepartment(department2);
        repository.save(stakeholder2);

        Integer departmentId = null;
        var department = departmentRepository.findByNameIgnoreCase(departmentName);

        if (department.isPresent()) {
            departmentId = department.get().getId();
        }

        var pageable = PageRequest.of(0, 10);
        var result = repository.findAllBySearchCriteria(name, email, departmentId, gender, pageable);

        assertEquals(expectedCount, result.getTotalElements());
        if (expectedName != null) {
            assertEquals(expectedName, result.getContent().get(0).getName());
        }
    }
}