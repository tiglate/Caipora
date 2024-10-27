package br.dev.ampliar.caipora.repos;

import br.dev.ampliar.caipora.CaiporaApplication;
import br.dev.ampliar.caipora.config.DomainConfig;
import br.dev.ampliar.caipora.domain.*;
import br.dev.ampliar.caipora.model.Environment;
import br.dev.ampliar.caipora.model.Gender;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

import java.time.LocalDate;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@ContextConfiguration(classes = {CaiporaApplication.class, DomainConfig.class})
class DeployRepositoryTest {

    @Autowired
    private DeployRepository repository;

    @Autowired
    private VersionRepository versionRepository;

    @Autowired
    private SoftwareRepository softwareRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private StakeholderRepository stakeholderRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    private Department createDepartment(String name) {
        var department = new Department();
        department.setName(name);
        return departmentRepository.save(department);
    }

    private Software createSoftware(String code, String name) {
        var software = new Software();
        software.setCode(code);
        software.setName(name);
        return softwareRepository.save(software);
    }

    private Version createVersion(String name, String fileName, Software software) {
        var version = new Version();
        version.setName(name);
        version.setFileName(fileName);
        version.setReleaseDate(LocalDate.now());
        version.setSoftware(software);
        return versionRepository.save(version);
    }

    private User createUser(String name, String email, Gender gender, Department department) {
        var user = new User();
        user.setName(name);
        user.setUsername(generateUsername(name, email));
        user.setEmail(email);
        user.setDepartment(department);
        user.setEnabled(true);
        user.setGender(gender);
        user.setPassword("password123");
        return userRepository.save(user);
    }

    private static String generateUsername(String fullName, String email) {
        if (fullName == null || email == null) {
            throw new IllegalArgumentException("Full name and email must not be null");
        }

        String[] nameParts = fullName.split(" ");
        if (nameParts.length < 2) {
            throw new IllegalArgumentException("Full name must contain at least a first name and a last name");
        }

        String firstName = nameParts[0];
        String lastName = nameParts[nameParts.length - 1];
        String uniqueIdentifier = UUID.nameUUIDFromBytes(email.getBytes()).toString().substring(0, 8);

        return (firstName.charAt(0) + lastName + uniqueIdentifier).toLowerCase();
    }

    private Stakeholder createStakeholder(String name, String email, Gender gender, Department department) {
        var stakeholder = new Stakeholder();
        stakeholder.setName(name);
        stakeholder.setEmail(email);
        stakeholder.setGender(gender);
        stakeholder.setDepartment(department);
        return stakeholderRepository.save(stakeholder);
    }

    @Test
    void testDeployCreationAndRetrievalById() {
        var department = createDepartment("IT Department");
        var software = createSoftware("SW001", "Test Software");
        var version = createVersion("1.0", "v1.0.zip", software);
        var authorizer = createStakeholder("Jane Doe", "janedoe@test.com", Gender.MALE, department);
        var operator = createUser("John Doe", "johndoe@test.com", Gender.FEMALE, department);

        var deploy = new Deploy();
        deploy.setVersion(version);
        deploy.setRfc("RFC001");
        deploy.setExecutionDate(LocalDate.now());
        deploy.setAuthorizer(authorizer);
        deploy.setOperator(operator);
        deploy.setIsActive(true);
        deploy.setEnvironment(Environment.PRODUCTION);

        var savedDeploy = repository.save(deploy);
        assertNotNull(savedDeploy.getId());
        assertNotNull(savedDeploy.getCreatedAt());
        assertNotNull(savedDeploy.getUpdatedAt());

        var foundDeploy = repository.findById(savedDeploy.getId()).orElse(null);
        assertNotNull(foundDeploy);
        assertEquals(deploy.getRfc(), foundDeploy.getRfc());
        assertEquals(deploy.getExecutionDate(), foundDeploy.getExecutionDate());
        assertEquals(deploy.getAuthorizer().getId(), foundDeploy.getAuthorizer().getId());
    }

    @Test
    void testDeployUpdate() {
        var department = createDepartment("IT Department");
        var software = createSoftware("SW002", "Another Software");
        var version = createVersion("1.0", "v1.0.zip", software);
        var authorizer = createStakeholder("Jane Doe", "janedoe@test.com", Gender.FEMALE, department);
        var operator = createUser("John Doe", "johndoe@test.com", Gender.MALE, department);

        var deploy = new Deploy();
        deploy.setVersion(version);
        deploy.setRfc("RFC002");
        deploy.setExecutionDate(LocalDate.now());
        deploy.setAuthorizer(authorizer);
        deploy.setOperator(operator);
        deploy.setIsActive(true);
        deploy.setEnvironment(Environment.PRODUCTION);

        var savedDeploy = repository.save(deploy);

        savedDeploy.setRfc("RFC003");
        savedDeploy.setExecutionDate(LocalDate.now().plusDays(1));
        repository.save(savedDeploy);

        var updatedDeploy = repository.findById(savedDeploy.getId()).orElse(null);
        assertNotNull(updatedDeploy);

        assertEquals(savedDeploy.getRfc(), updatedDeploy.getRfc());
        assertEquals(savedDeploy.getExecutionDate(), updatedDeploy.getExecutionDate());
        assertEquals(savedDeploy.getAuthorizer().getId(), updatedDeploy.getAuthorizer().getId());
    }

    @Test
    void testDeployDeletion() {
        var department = createDepartment("IT Department");
        var software = createSoftware("SW003", "Software to Delete");
        var version = createVersion("1.0", "v1.0.zip", software);
        var authorizer = createStakeholder("Jane Doe", "janedoe@test.com", Gender.FEMALE, department);
        var operator = createUser("John Doe", "johndoe@test.com", Gender.MALE, department);

        var deploy = new Deploy();
        deploy.setVersion(version);
        deploy.setRfc("RFC004");
        deploy.setExecutionDate(LocalDate.now());
        deploy.setAuthorizer(authorizer);
        deploy.setOperator(operator);
        deploy.setIsActive(true);
        deploy.setEnvironment(Environment.PRODUCTION);

        var savedDeploy = repository.save(deploy);
        assertNotNull(savedDeploy.getId());

        repository.delete(savedDeploy);

        var deletedDeploy = repository.findById(savedDeploy.getId()).orElse(null);
        assertNull(deletedDeploy);
    }

    @Test
    void testFindFirstByVersion() {
        var department = createDepartment("IT Department");
        var software = createSoftware("SW004", "Software for Version Test");
        var version = createVersion("1.0", "v1.0.zip", software);
        var authorizer = createStakeholder("Jane Doe", "janedoe@test.com", Gender.FEMALE, department);
        var operator = createUser("John Doe", "johndoe@test.com", Gender.MALE, department);

        var deploy = new Deploy();
        deploy.setVersion(version);
        deploy.setRfc("RFC005");
        deploy.setExecutionDate(LocalDate.now());
        deploy.setAuthorizer(authorizer);
        deploy.setOperator(operator);
        deploy.setIsActive(true);
        deploy.setEnvironment(Environment.PRODUCTION);
        repository.save(deploy);

        var foundDeploy = repository.findFirstByVersion(version);
        assertNotNull(foundDeploy);
        assertEquals(deploy.getRfc(), foundDeploy.getRfc());
        assertEquals(deploy.getExecutionDate(), foundDeploy.getExecutionDate());
        assertEquals(deploy.getAuthorizer().getId(), foundDeploy.getAuthorizer().getId());
    }

    @Test
    void testFindFirstByOperator() {
        var department = createDepartment("IT Department");
        var software = createSoftware("SW005", "Software for Operator Test");
        var version = createVersion("1.0", "v1.0.zip", software);
        var authorizer = createStakeholder("Jane Doe", "janedoe@test.com", Gender.FEMALE, department);
        var operator = createUser("John Doe", "johndoe@test.com", Gender.MALE, department);

        var deploy = new Deploy();
        deploy.setOperator(operator);
        deploy.setVersion(version);
        deploy.setRfc("RFC006");
        deploy.setExecutionDate(LocalDate.now());
        deploy.setAuthorizer(authorizer);
        deploy.setIsActive(true);
        deploy.setEnvironment(Environment.PRODUCTION);
        repository.save(deploy);

        var foundDeploy = repository.findFirstByOperator(operator);
        assertNotNull(foundDeploy);
        assertEquals(deploy.getRfc(), foundDeploy.getRfc());
        assertEquals(deploy.getExecutionDate(), foundDeploy.getExecutionDate());
        assertEquals(deploy.getAuthorizer().getId(), foundDeploy.getAuthorizer().getId());
    }

    @Test
    void testFindFirstByAuthorizer() {
        var department = createDepartment("IT Department");
        var software = createSoftware("SW006", "Software for Authorizer Test");
        var version = createVersion("1.0", "v1.0.zip", software);
        var authorizer = createStakeholder("Jane Doe", "janedoe@test.com", Gender.FEMALE, department);
        var operator = createUser("John Doe", "johndoe@test.com", Gender.MALE, department);

        var deploy = new Deploy();
        deploy.setAuthorizer(authorizer);
        deploy.setVersion(version);
        deploy.setRfc("RFC007");
        deploy.setExecutionDate(LocalDate.now());
        deploy.setOperator(operator);
        deploy.setIsActive(true);
        deploy.setEnvironment(Environment.PRODUCTION);
        repository.save(deploy);

        var foundDeploy = repository.findFirstByAuthorizer(authorizer);
        assertNotNull(foundDeploy);
        assertEquals(deploy.getRfc(), foundDeploy.getRfc());
        assertEquals(deploy.getExecutionDate(), foundDeploy.getExecutionDate());
        assertEquals(deploy.getAuthorizer().getId(), foundDeploy.getAuthorizer().getId());
    }

    @Test
    void testExistsByRfcIgnoreCase() {
        var department = createDepartment("IT Department");
        var software = createSoftware("SW007", "Software for RFC Test");
        var version = createVersion("1.0", "v1.0.zip", software);
        var authorizer = createStakeholder("Jane Doe", "janedoe@test.com", Gender.FEMALE, department);
        var operator = createUser("John Doe", "johndoe@test.com", Gender.MALE, department);

        var deploy = new Deploy();
        deploy.setVersion(version);
        deploy.setRfc("RFC008");
        deploy.setExecutionDate(LocalDate.now());
        deploy.setAuthorizer(authorizer);
        deploy.setOperator(operator);
        deploy.setIsActive(true);
        deploy.setEnvironment(Environment.PRODUCTION);
        repository.save(deploy);

        assertTrue(repository.existsByRfcIgnoreCase("rfc008"));
        assertFalse(repository.existsByRfcIgnoreCase("rfc009"));
    }
}