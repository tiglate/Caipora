package br.dev.ampliar.caipora.service;

import br.dev.ampliar.caipora.domain.Deploy;
import br.dev.ampliar.caipora.domain.Software;
import br.dev.ampliar.caipora.domain.Version;
import br.dev.ampliar.caipora.model.VersionDTO;
import br.dev.ampliar.caipora.repos.DeployRepository;
import br.dev.ampliar.caipora.repos.SoftwareRepository;
import br.dev.ampliar.caipora.repos.VersionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class VersionServiceImplTest {

    @Mock
    private VersionRepository versionRepository;

    @Mock
    private SoftwareRepository softwareRepository;

    @Mock
    private DeployRepository deployRepository;

    @TempDir
    Path packagesDir;

    private VersionServiceImpl versionService;
    private Software software;

    @BeforeEach
    void setUp() throws IOException {
        MockitoAnnotations.openMocks(this);
        final PackageStorage packageStorage = new PackageStorage(packagesDir.toString(), List.of("zip", "msi", "tar.gz"));
        versionService = new VersionServiceImpl(versionRepository, softwareRepository, deployRepository, packageStorage);
        software = new Software();
        software.setId(7);
        software.setCode("CRM");
        software.setName("CRM");
        when(softwareRepository.findById(7)).thenReturn(Optional.of(software));
        Files.createDirectories(packagesDir.resolve("CRM"));
        Files.writeString(packagesDir.resolve("CRM/crm-1.0.zip"), "zip");
        Files.writeString(packagesDir.resolve("CRM/crm-1.1.tar.gz"), "tgz");
        Files.writeString(packagesDir.resolve("CRM/notes.txt"), "not a package");
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
        version.setSoftware(software);
        version.setFileName("crm-1.0.zip");

        when(versionRepository.findById(1)).thenReturn(Optional.of(version));

        var result = versionService.get(1);

        assertEquals("1.0.0", result.getName());
        assertTrue(result.isPackageAvailable());
        assertEquals("3 B", result.getFileSize());
    }

    @Test
    void testGet_PackageMissing() {
        var version = new Version();
        version.setId(1);
        version.setSoftware(software);
        version.setFileName("gone.zip");
        when(versionRepository.findById(1)).thenReturn(Optional.of(version));

        assertFalse(versionService.get(1).isPackageAvailable());
    }

    @Test
    void testCreate_RegistersAPackageOfTheApplicationFolder() {
        var versionDTO = new VersionDTO();
        versionDTO.setName("1.0.0");
        versionDTO.setSoftwareId(7);
        versionDTO.setFileName("crm-1.0.zip");
        var saved = new Version();
        saved.setId(1);
        when(versionRepository.save(any(Version.class))).thenReturn(saved);

        assertEquals(1, versionService.create(versionDTO));

        final ArgumentCaptor<Version> captor = ArgumentCaptor.forClass(Version.class);
        verify(versionRepository).save(captor.capture());
        assertEquals("crm-1.0.zip", captor.getValue().getFileName());
        assertSame(software, captor.getValue().getSoftware());
    }

    @Test
    void testCreate_RejectsFilesOutsideTheApplicationFolder() {
        for (String name : List.of("../other.zip", "missing.zip", "notes.txt", "sub/crm.zip", "")) {
            var versionDTO = new VersionDTO();
            versionDTO.setName("1.0.0");
            versionDTO.setSoftwareId(7);
            versionDTO.setFileName(name);
            assertThrows(IllegalArgumentException.class, () -> versionService.create(versionDTO), name);
        }
        verify(versionRepository, never()).save(any());
    }

    @Test
    void testCreate_RejectsAPackageAlreadyRegistered() {
        var versionDTO = new VersionDTO();
        versionDTO.setName("1.0.0");
        versionDTO.setSoftwareId(7);
        versionDTO.setFileName("crm-1.0.zip");
        when(versionRepository.existsBySoftwareAndFileNameIgnoreCase(software, "crm-1.0.zip")).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> versionService.create(versionDTO));
    }

    @Test
    void testGetAvailablePackages_ListsOnlyUnregisteredPackages() {
        when(versionRepository.findFileNamesBySoftware(software)).thenReturn(List.of("CRM-1.0.ZIP"));

        var packages = versionService.getAvailablePackages(7);

        assertEquals(List.of("crm-1.1.tar.gz"), packages.stream().map(PackageStorage.PackageFile::name).toList());
    }

    @Test
    void testUpdate_KeepsPackageAndApplication() {
        var versionDTO = new VersionDTO();
        versionDTO.setName("1.0.1");
        versionDTO.setFileName("other.zip");
        var version = new Version();
        version.setId(1);
        version.setSoftware(software);
        version.setFileName("crm-1.0.zip");

        when(versionRepository.findById(1)).thenReturn(Optional.of(version));

        versionService.update(1, versionDTO);

        assertEquals("1.0.1", version.getName());
        assertEquals("crm-1.0.zip", version.getFileName());
        assertSame(software, version.getSoftware());
        verify(versionRepository).save(version);
    }

    @Test
    void testDelete_KeepsThePackage() {
        var version = new Version();
        version.setId(1);
        version.setSoftware(software);
        version.setFileName("crm-1.0.zip");
        when(versionRepository.findById(1)).thenReturn(Optional.of(version));

        versionService.delete(1);

        verify(versionRepository).delete(version);
        assertTrue(Files.exists(packagesDir.resolve("CRM/crm-1.0.zip")));
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

    @Test
    void testGetPackageFile_RejectsPathTraversal() {
        var version = new Version();
        version.setId(1);
        version.setSoftware(software);
        version.setFileName("../../etc/passwd.zip");
        when(versionRepository.findById(1)).thenReturn(Optional.of(version));

        assertThrows(IllegalArgumentException.class, () -> versionService.getPackageFile(1));
    }
}
