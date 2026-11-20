package br.dev.ampliar.caipora.service;

import br.dev.ampliar.caipora.domain.Deploy;
import br.dev.ampliar.caipora.domain.Software;
import br.dev.ampliar.caipora.domain.Version;
import br.dev.ampliar.caipora.model.DeployStatus;
import br.dev.ampliar.caipora.model.Environment;
import br.dev.ampliar.caipora.repos.DeployRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.ArgumentCaptor;
import org.springframework.core.task.SyncTaskExecutor;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.SimpleTransactionStatus;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class DeployExecutionServiceTest {

    @TempDir
    Path packagesDir;

    private DeployRepository deployRepository;
    private DeployScriptRunner runner;
    private DeployExecutionService service;
    private Software software;
    private Deploy deploy;

    @BeforeEach
    void setUp() throws IOException {
        deployRepository = mock(DeployRepository.class);
        runner = mock(DeployScriptRunner.class);
        final PlatformTransactionManager transactionManager = mock(PlatformTransactionManager.class);
        when(transactionManager.getTransaction(any())).thenReturn(new SimpleTransactionStatus());
        final PackageStorage packageStorage = new PackageStorage(packagesDir.toString(), List.of("zip"));
        service = new DeployExecutionService(deployRepository, packageStorage, runner, new SyncTaskExecutor(),
                transactionManager, "node-1", Duration.ofMinutes(5));

        Files.createDirectories(packagesDir.resolve("CRM"));
        Files.writeString(packagesDir.resolve("CRM/crm-2.0.zip"), "zip");
        software = new Software();
        software.setId(1);
        software.setCode("CRM");
        software.setName("CRM");
        software.setDeployScript("deploy.sh");
        final Version version = new Version();
        version.setId(2);
        version.setName("v2.0");
        version.setFileName("crm-2.0.zip");
        version.setSoftware(software);
        deploy = new Deploy();
        deploy.setId(10);
        deploy.setVersion(version);
        deploy.setEnvironment(Environment.QA);
        deploy.setRfc("CHG1");
        deploy.setStatus(DeployStatus.PENDING);
        deploy.setIsActive(false);
        when(deployRepository.findById(10)).thenReturn(Optional.of(deploy));
    }

    @Test
    void successfulScriptActivatesTheDeployAndDeactivatesThePreviousOne() {
        final Deploy previous = new Deploy();
        previous.setId(9);
        previous.setIsActive(true);
        when(deployRepository.findAllByVersionSoftwareAndEnvironmentAndIsActiveTrue(software, Environment.QA))
                .thenReturn(List.of(previous));
        when(runner.run(eq("deploy.sh"), anyMap(), any(), any()))
                .thenReturn(new DeployScriptRunner.Result(0, "ok", false, false));

        service.onDeployRequested(new DeployExecutionService.DeployRequestedEvent(10));

        assertEquals(DeployStatus.SUCCEEDED, deploy.getStatus());
        assertTrue(deploy.getIsActive());
        assertFalse(previous.getIsActive());
        assertEquals("node-1", deploy.getExecutorNode());
        assertNotNull(deploy.getFinishedAt());
        verify(deployRepository).saveAllAndFlush(List.of(previous));
    }

    @Test
    void scriptReceivesTheDeployInEnvironmentVariables() {
        when(runner.run(any(), anyMap(), any(), any())).thenReturn(new DeployScriptRunner.Result(0, "", false, false));

        service.onDeployRequested(new DeployExecutionService.DeployRequestedEvent(10));

        @SuppressWarnings("unchecked") final ArgumentCaptor<Map<String, String>> captor = ArgumentCaptor.forClass(Map.class);
        verify(runner).run(any(), captor.capture(), any(), any());
        assertEquals(packagesDir.resolve("CRM/crm-2.0.zip").toAbsolutePath().normalize().toString(),
                captor.getValue().get("CAIPORA_PACKAGE"));
        assertEquals("QA", captor.getValue().get("CAIPORA_ENVIRONMENT"));
        assertEquals("v2.0", captor.getValue().get("CAIPORA_VERSION"));
        assertEquals("CHG1", captor.getValue().get("CAIPORA_RFC"));
    }

    @Test
    void failedScriptKeepsTheDeployInactive() {
        when(runner.run(any(), anyMap(), any(), any())).thenReturn(new DeployScriptRunner.Result(2, "boom", false, false));

        service.onDeployRequested(new DeployExecutionService.DeployRequestedEvent(10));

        assertEquals(DeployStatus.FAILED, deploy.getStatus());
        assertEquals(2, deploy.getExitCode());
        assertFalse(deploy.getIsActive());
        verify(deployRepository, never()).saveAllAndFlush(any());
    }

    @Test
    void withoutScriptTheDeployIsOnlyRecorded() {
        software.setDeployScript(null);

        service.onDeployRequested(new DeployExecutionService.DeployRequestedEvent(10));

        assertEquals(DeployStatus.SUCCEEDED, deploy.getStatus());
        assertTrue(deploy.getIsActive());
        verifyNoInteractions(runner);
    }

    @Test
    void missingPackageFailsWithoutRunningTheScript() throws IOException {
        Files.delete(packagesDir.resolve("CRM/crm-2.0.zip"));

        service.onDeployRequested(new DeployExecutionService.DeployRequestedEvent(10));

        assertEquals(DeployStatus.FAILED, deploy.getStatus());
        assertTrue(deploy.getScriptOutput().contains("not found"), deploy.getScriptOutput());
        verifyNoInteractions(runner);
    }

    @Test
    void deployCancelledWhileRunningStaysCancelled() {
        when(runner.run(any(), anyMap(), any(), any())).thenAnswer(invocation -> {
            deploy.setStatus(DeployStatus.CANCELLED); // e.g. by a user, from another request
            return new DeployScriptRunner.Result(0, "done", false, false);
        });

        service.onDeployRequested(new DeployExecutionService.DeployRequestedEvent(10));

        assertEquals(DeployStatus.CANCELLED, deploy.getStatus());
        assertFalse(deploy.getIsActive());
    }

    @Test
    void cancelledBeforeStartingNeverRuns() {
        deploy.setStatus(DeployStatus.CANCELLED);

        service.onDeployRequested(new DeployExecutionService.DeployRequestedEvent(10));

        verifyNoInteractions(runner);
        assertEquals(DeployStatus.CANCELLED, deploy.getStatus());
    }

    @Test
    void restartFailsOnlyTheDeploysOfThisNodeOrStaleOnes() {
        final Deploy mine = inProgress(1, "node-1", OffsetDateTime.now());
        final Deploy otherNode = inProgress(2, "node-2", OffsetDateTime.now());
        final Deploy stale = inProgress(3, "node-2", OffsetDateTime.now().minusHours(2));
        when(deployRepository.findAllByStatusIn(anyCollection())).thenReturn(List.of(mine, otherNode, stale));

        service.failInterruptedDeploys();

        assertEquals(DeployStatus.FAILED, mine.getStatus());
        assertEquals(DeployStatus.RUNNING, otherNode.getStatus());
        assertEquals(DeployStatus.FAILED, stale.getStatus());
    }

    @Test
    void anOldDeployRerunRecentlyOnAnotherNodeIsLeftAlone() {
        final Deploy rerun = inProgress(4, "node-2", OffsetDateTime.now().minusDays(30));
        rerun.setStartedAt(OffsetDateTime.now().minusMinutes(1));
        when(deployRepository.findAllByStatusIn(anyCollection())).thenReturn(List.of(rerun));

        service.failInterruptedDeploys();

        assertEquals(DeployStatus.RUNNING, rerun.getStatus());
    }

    private static Deploy inProgress(final int id, final String node, final OffsetDateTime createdAt) {
        final Deploy deploy = new Deploy();
        deploy.setId(id);
        deploy.setStatus(DeployStatus.RUNNING);
        deploy.setExecutorNode(node);
        deploy.setCreatedAt(createdAt);
        return deploy;
    }
}
