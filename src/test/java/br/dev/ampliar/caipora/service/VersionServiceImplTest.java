package br.dev.ampliar.caipora.service;

import br.dev.ampliar.caipora.domain.Deploy;
import br.dev.ampliar.caipora.domain.Version;
import br.dev.ampliar.caipora.model.VersionDTO;
import br.dev.ampliar.caipora.repos.DeployRepository;
import br.dev.ampliar.caipora.repos.SoftwareRepository;
import br.dev.ampliar.caipora.repos.VersionRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class VersionServiceImplTest {

    @Mock
    private VersionRepository versionRepository;

    @Mock
    private SoftwareRepository softwareRepository;

    @Mock
    private DeployRepository deployRepository;

    @InjectMocks
    private VersionServiceImpl versionService;

    private Path tempDir;

    @BeforeEach
    void setUp() throws IOException {
        MockitoAnnotations.openMocks(this);
        tempDir = Files.createTempDirectory("test-uploads");
        ReflectionTestUtils.setField(versionService, "uploadDirectory", tempDir.toString());
    }

    @AfterEach
    void tearDown() throws IOException {
        Files.walk(tempDir)
            .map(Path::toFile)
            .forEach(file -> {
                if (!file.delete()) {
                    file.deleteOnExit();
                }
            });
    }

    @Test
    void testFindAll() {
        var filter = new VersionDTO();
        var pageable = PageRequest.of(0, 10);
        var version = new VersionDTO();
        version.setName("1.0.0");
        var page = new PageImpl<>(Collections.singletonList(version));

        when(versionRepository.findAllBySearchCriteria(any(), any(), any())).thenReturn(page);

        var result = versionService.findAll(filter, pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(versionRepository, times(1)).findAllBySearchCriteria(any(), any(), any());
    }

    @Test
    void testGet() {
        var version = new Version();
        version.setId(1);
        version.setName("1.0.0");

        when(versionRepository.findById(1)).thenReturn(Optional.of(version));

        var result = versionService.get(1);

        assertNotNull(result);
        assertEquals("1.0.0", result.getName());
        verify(versionRepository, times(1)).findById(1);
    }

    @Test
    void testCreate() throws IOException {
        var versionDTO = new VersionDTO();
        versionDTO.setName("1.0.0");
        var fileContent = "test content".getBytes();
        var file = new MockMultipartFile("file", "test.zip", "application/zip", fileContent);
        versionDTO.setFile(file);
        var version = new Version();
        version.setId(1);

        when(versionRepository.save(any(Version.class))).thenReturn(version);

        var id = versionService.create(versionDTO);

        assertNotNull(id);
        assertEquals(1, id);
        verify(versionRepository, times(1)).save(any(Version.class));
    }

    @Test
    void testUpdate() {
        var versionDTO = new VersionDTO();
        versionDTO.setName("1.0.0");
        var version = new Version();
        version.setId(1);

        when(versionRepository.findById(1)).thenReturn(Optional.of(version));
        when(versionRepository.save(any(Version.class))).thenReturn(version);

        versionService.update(1, versionDTO);

        verify(versionRepository, times(1)).findById(1);
        verify(versionRepository, times(1)).save(any(Version.class));
    }

    @Test
    void testDelete() throws IOException {
        var version = new Version();
        version.setId(1);
        version.setFileName("test.zip");

        when(versionRepository.findById(1)).thenReturn(Optional.of(version));

        versionService.delete(1);

        verify(versionRepository, times(1)).deleteById(1);
    }

    @Test
    void testGetReferencedWarning_ReferencedByDeploy() {
        var version = new Version();
        version.setId(1);
        var deploy = new Deploy();
        deploy.setId(1);

        when(versionRepository.findById(1)).thenReturn(Optional.of(version));
        when(deployRepository.findFirstByVersion(version)).thenReturn(deploy);

        var warning = versionService.getReferencedWarning(1);

        assertNotNull(warning);
        assertEquals("version.deploy.version.referenced", warning.getKey());
    }
}