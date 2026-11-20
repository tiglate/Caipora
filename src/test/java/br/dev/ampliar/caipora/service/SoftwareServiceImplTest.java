package br.dev.ampliar.caipora.service;

import br.dev.ampliar.caipora.domain.Software;
import br.dev.ampliar.caipora.domain.Version;
import br.dev.ampliar.caipora.model.SoftwareDTO;
import br.dev.ampliar.caipora.repos.SoftwareRepository;
import br.dev.ampliar.caipora.repos.VersionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class SoftwareServiceImplTest {

    @Mock
    private SoftwareRepository softwareRepository;

    @Mock
    private VersionRepository versionRepository;

    @Mock
    private PackageStorage packageStorage;

    @InjectMocks
    private SoftwareServiceImpl softwareService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testFindAll() {
        var filter = new SoftwareDTO();
        var pageable = PageRequest.of(0, 10);
        var software = new SoftwareDTO();
        software.setCode("SW001");
        software.setName("Test Software");
        var page = new PageImpl<>(Collections.singletonList(software));

        when(softwareRepository.findAllBySearchCriteria(any(), any(), any(), any())).thenReturn(page);

        var result = softwareService.findAll(filter, pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(softwareRepository, times(1)).findAllBySearchCriteria(any(), any(), any(), any());
    }

    @Test
    void testGet() {
        var software = new Software();
        software.setId(1);
        software.setCode("SW001");
        software.setName("Test Software");

        when(softwareRepository.findById(1)).thenReturn(Optional.of(software));

        var result = softwareService.get(1);

        assertNotNull(result);
        assertEquals("Test Software", result.getName());
        verify(softwareRepository, times(1)).findById(1);
    }

    @Test
    void testCreate() {
        var softwareDTO = new SoftwareDTO();
        softwareDTO.setCode("SW001");
        softwareDTO.setName("Test Software");
        var software = new Software();
        software.setId(1);

        when(softwareRepository.save(any(Software.class))).thenReturn(software);

        var id = softwareService.create(softwareDTO, false);

        assertNotNull(id);
        assertEquals(1, id);
        verify(softwareRepository, times(1)).save(any(Software.class));
        verify(packageStorage).createApplicationFolder("SW001");
    }

    @Test
    void testCreate_ScriptOnlyWithPermission() {
        var softwareDTO = new SoftwareDTO();
        softwareDTO.setCode("SW001");
        softwareDTO.setName("Test Software");
        softwareDTO.setDeployScript("echo a\r\necho b && echo <c>\r\n");
        when(softwareRepository.save(any(Software.class))).thenAnswer(invocation -> invocation.getArgument(0));

        softwareService.create(softwareDTO, false);
        softwareService.create(softwareDTO, true);

        var captor = org.mockito.ArgumentCaptor.forClass(Software.class);
        verify(softwareRepository, times(2)).save(captor.capture());
        assertNull(captor.getAllValues().get(0).getDeployScript());
        // stored as typed (not HTML-sanitized), with Unix line endings
        assertEquals("echo a\necho b && echo <c>\n", captor.getAllValues().get(1).getDeployScript());
    }

    @Test
    void testCodeMustBeAbleToNameAFolder() {
        var softwareDTO = new SoftwareDTO();
        softwareDTO.setCode("../X");
        softwareDTO.setName("Test Software");

        assertThrows(IllegalArgumentException.class, () -> softwareService.create(softwareDTO, false));
        assertDoesNotThrow(() -> SoftwareServiceImpl.checkCode("CRM_2.v-1"));
    }

    @Test
    void testUpdate_LegacyCodeCanStillBeSaved() {
        var softwareDTO = new SoftwareDTO();
        softwareDTO.setCode("OLD CODE");
        softwareDTO.setName("Test Software");
        var software = new Software();
        software.setId(1);
        software.setCode("OLD CODE");
        when(softwareRepository.findById(1)).thenReturn(Optional.of(software));

        assertDoesNotThrow(() -> softwareService.update(1, softwareDTO, false));
    }

    @Test
    void testUpdate_KeepsScriptWithoutPermission() {
        var softwareDTO = new SoftwareDTO();
        softwareDTO.setCode("SW001");
        softwareDTO.setName("Test Software");
        softwareDTO.setDeployScript("rm -rf /");
        var software = new Software();
        software.setId(1);
        software.setCode("SW001");
        software.setDeployScript("echo deploy");
        when(softwareRepository.findById(1)).thenReturn(Optional.of(software));

        softwareService.update(1, softwareDTO, false);

        assertEquals("echo deploy", software.getDeployScript());
    }

    @Test
    void testUpdate_CodeLockedWhenTheApplicationHasVersions() {
        var softwareDTO = new SoftwareDTO();
        softwareDTO.setCode("NEW");
        softwareDTO.setName("Test Software");
        var software = new Software();
        software.setId(1);
        software.setCode("OLD");
        when(softwareRepository.findById(1)).thenReturn(Optional.of(software));
        when(versionRepository.findFirstBySoftware(software)).thenReturn(new br.dev.ampliar.caipora.domain.Version());

        assertThrows(IllegalArgumentException.class, () -> softwareService.update(1, softwareDTO, true));
        verify(softwareRepository, never()).save(any());
    }

    @Test
    void testUpdate() {
        var softwareDTO = new SoftwareDTO();
        softwareDTO.setCode("SW001");
        softwareDTO.setName("Test Software");
        var software = new Software();
        software.setId(1);

        when(softwareRepository.findById(1)).thenReturn(Optional.of(software));
        when(softwareRepository.save(any(Software.class))).thenReturn(software);

        softwareService.update(1, softwareDTO, true);

        verify(softwareRepository, times(1)).findById(1);
        verify(softwareRepository, times(1)).save(any(Software.class));
    }

    @Test
    void testDelete() {
        softwareService.delete(1);

        verify(softwareRepository, times(1)).deleteById(1);
    }

    @Test
    void testCodeExists() {
        when(softwareRepository.existsByCodeIgnoreCase("SW001")).thenReturn(true);

        var exists = softwareService.codeExists("SW001");

        assertTrue(exists);
        verify(softwareRepository, times(1)).existsByCodeIgnoreCase("SW001");
    }

    @Test
    void testNameExists() {
        when(softwareRepository.existsByNameIgnoreCase("Test Software")).thenReturn(true);

        var exists = softwareService.nameExists("Test Software");

        assertTrue(exists);
        verify(softwareRepository, times(1)).existsByNameIgnoreCase("Test Software");
    }

    @Test
    void testGetReferencedWarning_ReferencedByVersion() {
        var software = new Software();
        software.setId(1);
        var version = new Version();
        version.setId(1);

        when(softwareRepository.findById(1)).thenReturn(Optional.of(software));
        when(versionRepository.findFirstBySoftware(software)).thenReturn(version);

        var warning = softwareService.getReferencedWarning(1);

        assertNotNull(warning);
        assertEquals("software.version.software.referenced", warning.getKey());
    }
}