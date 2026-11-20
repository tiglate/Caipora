package br.dev.ampliar.caipora.service;

import br.dev.ampliar.caipora.domain.Deploy;
import br.dev.ampliar.caipora.domain.Software;
import br.dev.ampliar.caipora.domain.Version;
import br.dev.ampliar.caipora.model.DeployDTO;
import br.dev.ampliar.caipora.model.DeployStatus;
import br.dev.ampliar.caipora.model.Environment;
import br.dev.ampliar.caipora.repos.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.context.ApplicationEventPublisher;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class DeployServiceImplTest {

    @Mock
    private DeployRepository deployRepository;
    @Mock
    private VersionRepository versionRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private StakeholderRepository stakeholderRepository;
    @Mock
    private SoftwareRepository softwareRepository;
    @Mock
    private DeployExecutionService executionService;
    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private DeployServiceImpl deployService;

    private Software software;
    private Version version;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        software = new Software();
        software.setId(3);
        software.setName("CRM");
        version = new Version();
        version.setId(5);
        version.setName("v1.0");
        version.setSoftware(software);
        when(versionRepository.findById(5)).thenReturn(Optional.of(version));
        when(softwareRepository.findById(3)).thenReturn(Optional.of(software));
        when(softwareRepository.findByIdForUpdate(3)).thenReturn(Optional.of(software));
    }

    @Test
    void testGet() {
        var deploy = new Deploy();
        deploy.setId(1);
        deploy.setEnvironment(Environment.PRODUCTION);
        deploy.setVersion(version);

        when(deployRepository.findById(1)).thenReturn(Optional.of(deploy));

        var result = deployService.get(1);

        assertEquals(Environment.PRODUCTION, result.getEnvironment());
        assertEquals("v1.0", result.getVersionName());
        assertEquals(3, result.getSoftwareId());
    }

    @Test
    void testCreate_QueuesAnInactivePendingDeployAndPublishesTheEvent() {
        var deployDTO = new DeployDTO();
        deployDTO.setEnvironment(Environment.QA);
        deployDTO.setVersionId(5);
        deployDTO.setIsActive(true);
        when(deployRepository.save(any(Deploy.class))).thenAnswer(invocation -> {
            Deploy deploy = invocation.getArgument(0);
            deploy.setId(1);
            return deploy;
        });

        assertEquals(1, deployService.create(deployDTO));

        final ArgumentCaptor<Deploy> captor = ArgumentCaptor.forClass(Deploy.class);
        verify(deployRepository).save(captor.capture());
        assertEquals(DeployStatus.PENDING, captor.getValue().getStatus());
        assertFalse(captor.getValue().getIsActive());
        verify(softwareRepository).findByIdForUpdate(3);
        verify(eventPublisher).publishEvent(new DeployExecutionService.DeployRequestedEvent(1));
    }

    @Test
    void testCreate_RejectedWhileTheEnvironmentIsBeingDeployed() {
        var deployDTO = new DeployDTO();
        deployDTO.setEnvironment(Environment.QA);
        deployDTO.setVersionId(5);
        when(deployRepository.existsByVersionSoftwareAndEnvironmentAndStatusIn(eq(software), eq(Environment.QA), anyCollection()))
                .thenReturn(true);

        assertThrows(DeployService.DeployInProgressException.class, () -> deployService.create(deployDTO));
        verify(deployRepository, never()).save(any());
        verify(eventPublisher, never()).publishEvent(any());
    }

    @Test
    void testRerun_OnlyFailedDeploys() {
        var deploy = new Deploy();
        deploy.setId(1);
        deploy.setVersion(version);
        deploy.setEnvironment(Environment.QA);
        deploy.setStatus(DeployStatus.SUCCEEDED);
        when(deployRepository.findById(1)).thenReturn(Optional.of(deploy));

        assertThrows(IllegalStateException.class, () -> deployService.rerun(1, null));

        deploy.setStatus(DeployStatus.FAILED);
        deploy.setScriptOutput("boom");
        deploy.setExitCode(1);
        deployService.rerun(1, null);

        assertEquals(DeployStatus.PENDING, deploy.getStatus());
        assertNull(deploy.getScriptOutput());
        assertNull(deploy.getExitCode());
        verify(eventPublisher).publishEvent(new DeployExecutionService.DeployRequestedEvent(1));
    }

    @Test
    void testFindRollbackVersion_SkipsTheActiveVersion() {
        var active = deployOf(version, true);
        var older = new Version();
        older.setId(4);
        when(deployRepository.findAllByVersionSoftwareAndEnvironmentAndIsActiveTrue(software, Environment.QA))
                .thenReturn(List.of(active));
        when(deployRepository.findAllByVersionSoftwareAndEnvironmentAndStatusOrderByIdDesc(software, Environment.QA,
                DeployStatus.SUCCEEDED)).thenReturn(List.of(active, deployOf(version, false), deployOf(older, false)));

        assertEquals(Optional.of(4), deployService.findRollbackVersion(3, Environment.QA));
    }

    @Test
    void testRfcExists() {
        when(deployRepository.existsByRfcIgnoreCase("RFC123")).thenReturn(true);

        assertTrue(deployService.rfcExists("RFC123"));
    }

    private static Deploy deployOf(final Version version, final boolean active) {
        var deploy = new Deploy();
        deploy.setVersion(version);
        deploy.setIsActive(active);
        deploy.setStatus(DeployStatus.SUCCEEDED);
        return deploy;
    }
}
