package br.dev.ampliar.caipora.repos;

import br.dev.ampliar.caipora.CaiporaApplication;
import br.dev.ampliar.caipora.config.DomainConfig;
import br.dev.ampliar.caipora.domain.Software;
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
class SoftwareRepositoryTest {

    @Autowired
    private SoftwareRepository repository;

    @Test
    void testSoftwareCreationAndRetrievalById() {
        var software = new Software();
        software.setCode("SW001");
        software.setName("Test Software");

        var savedSoftware = repository.save(software);
        assertNotNull(savedSoftware.getId());
        assertNotNull(savedSoftware.getCreatedAt());
        assertNotNull(savedSoftware.getUpdatedAt());

        var foundSoftware = repository.findById(savedSoftware.getId()).orElse(null);
        assertNotNull(foundSoftware);
        assertEquals(software.getCode(), foundSoftware.getCode());
        assertEquals(software.getName(), foundSoftware.getName());
    }

    @Test
    void testSoftwareUpdate() {
        var software = new Software();
        software.setCode("SW002");
        software.setName("Initial Name");

        var savedSoftware = repository.save(software);

        savedSoftware.setName("Updated Name");
        repository.save(savedSoftware);

        var updatedSoftware = repository.findById(savedSoftware.getId()).orElse(null);
        assertNotNull(updatedSoftware);

        assertEquals(savedSoftware.getName(), updatedSoftware.getName());
    }

    @Test
    void testSoftwareDeletion() {
        var software = new Software();
        software.setCode("SW003");
        software.setName("To Be Deleted");

        var savedSoftware = repository.save(software);
        assertNotNull(savedSoftware.getId());

        repository.delete(savedSoftware);

        var deletedSoftware = repository.findById(savedSoftware.getId()).orElse(null);
        assertNull(deletedSoftware);
    }

    static Stream<Arguments> provideSearchCriteria() {
        return Stream.of(
                Arguments.of("SW001", null, null, 1, "Test Software"),
                Arguments.of(null, "Updated Name", null, 1, "Updated Name"),
                Arguments.of("SW002", "Updated Name", null, 1, "Updated Name"),
                Arguments.of(null, null, null, 2, null)
        );
    }

    @ParameterizedTest
    @MethodSource("provideSearchCriteria")
    void testFindAllBySearchCriteria(String code, String name, Integer owner, int expectedCount, String expectedName) {
        var software1 = new Software();
        software1.setCode("SW001");
        software1.setName("Test Software");
        repository.save(software1);

        var software2 = new Software();
        software2.setCode("SW002");
        software2.setName("Updated Name");
        repository.save(software2);

        var pageable = PageRequest.of(0, 10);
        var result = repository.findAllBySearchCriteria(code, name, owner, pageable);

        assertEquals(expectedCount, result.getTotalElements());
        if (expectedName != null) {
            assertEquals(expectedName, result.getContent().get(0).getName());
        }
    }
}