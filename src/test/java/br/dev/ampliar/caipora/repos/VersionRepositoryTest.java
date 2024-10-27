package br.dev.ampliar.caipora.repos;

import br.dev.ampliar.caipora.CaiporaApplication;
import br.dev.ampliar.caipora.config.DomainConfig;
import br.dev.ampliar.caipora.domain.Software;
import br.dev.ampliar.caipora.domain.Version;
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

import java.time.LocalDate;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@ContextConfiguration(classes = {CaiporaApplication.class, DomainConfig.class})
class VersionRepositoryTest {

    @Autowired
    private VersionRepository repository;

    @Autowired
    private SoftwareRepository softwareRepository;

    @Test
    void testVersionCreationAndRetrievalById() {
        var software = new Software();
        software.setCode("SW001");
        software.setName("Test Software");
        var savedSoftware = softwareRepository.save(software);

        var version = new Version();
        version.setName("1.0");
        version.setFileName("v1.0.zip");
        version.setReleaseDate(LocalDate.now());
        version.setSoftware(savedSoftware);

        var savedVersion = repository.save(version);
        assertNotNull(savedVersion.getId());
        assertNotNull(savedVersion.getCreatedAt());
        assertNotNull(savedVersion.getUpdatedAt());

        var foundVersion = repository.findById(savedVersion.getId()).orElse(null);
        assertNotNull(foundVersion);
        assertEquals(version.getName(), foundVersion.getName());
        assertEquals(version.getFileName(), foundVersion.getFileName());
    }

    @Test
    void testVersionUpdate() {
        var software = new Software();
        software.setCode("SW002");
        software.setName("Another Software");
        var savedSoftware = softwareRepository.save(software);

        var version = new Version();
        version.setName("1.0");
        version.setFileName("v1.0.zip");
        version.setReleaseDate(LocalDate.now());
        version.setSoftware(savedSoftware);

        var savedVersion = repository.save(version);

        savedVersion.setName("1.1");
        savedVersion.setFileName("v1.1.zip");
        repository.save(savedVersion);

        var updatedVersion = repository.findById(savedVersion.getId()).orElse(null);
        assertNotNull(updatedVersion);

        assertEquals(savedVersion.getName(), updatedVersion.getName());
        assertEquals(savedVersion.getFileName(), updatedVersion.getFileName());
    }

    @Test
    void testVersionDeletion() {
        var software = new Software();
        software.setCode("SW003");
        software.setName("Software to Delete");
        var savedSoftware = softwareRepository.save(software);

        var version = new Version();
        version.setName("1.0");
        version.setFileName("v1.0.zip");
        version.setReleaseDate(LocalDate.now());
        version.setSoftware(savedSoftware);

        var savedVersion = repository.save(version);
        assertNotNull(savedVersion.getId());

        repository.delete(savedVersion);

        var deletedVersion = repository.findById(savedVersion.getId()).orElse(null);
        assertNull(deletedVersion);
    }

    static Stream<Arguments> provideSearchCriteria() {
        return Stream.of(
                Arguments.of("1.0", null, 1, "1.0"),
                Arguments.of(null, "SW001", 1, "1.0"),
                Arguments.of("1.1", null, 1, "1.1"),
                Arguments.of(null, null, 2, null)
        );
    }

    @ParameterizedTest
    @MethodSource("provideSearchCriteria")
    void testFindAllBySearchCriteria(String name, String softwareCode, int expectedCount, String expectedName) {
        var software1 = new Software();
        software1.setCode("SW001");
        software1.setName("Test Software");
        softwareRepository.save(software1);

        var software2 = new Software();
        software2.setCode("SW002");
        software2.setName("Another Software");
        softwareRepository.save(software2);

        var version1 = new Version();
        version1.setName("1.0");
        version1.setFileName("v1.0.zip");
        version1.setSoftware(software1);
        version1.setReleaseDate(LocalDate.now());
        repository.save(version1);

        var version2 = new Version();
        version2.setName("1.1");
        version2.setFileName("v1.1.zip");
        version2.setSoftware(software2);
        version2.setReleaseDate(LocalDate.now());
        repository.save(version2);

        Integer softwareId = null;

        if (softwareCode != null) {
            var foundSoftware = softwareRepository.findByCodeIgnoreCase(softwareCode);
            if (foundSoftware.isPresent()) {
                softwareId = foundSoftware.get().getId();
            }
        }

        var pageable = PageRequest.of(0, 10);
        var result = repository.findAllBySearchCriteria(name, softwareId, pageable);

        assertEquals(expectedCount, result.getTotalElements());
        if (expectedName != null) {
            assertEquals(expectedName, result.getContent().get(0).getName());
        }
    }
}