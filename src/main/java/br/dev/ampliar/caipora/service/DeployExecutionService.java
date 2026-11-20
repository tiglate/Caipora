package br.dev.ampliar.caipora.service;

import br.dev.ampliar.caipora.domain.Deploy;
import br.dev.ampliar.caipora.domain.Software;
import br.dev.ampliar.caipora.domain.Version;
import br.dev.ampliar.caipora.model.DeployStatus;
import br.dev.ampliar.caipora.repos.DeployRepository;
import br.dev.ampliar.caipora.util.NotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.task.TaskExecutor;
import org.springframework.core.task.TaskRejectedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.transaction.support.TransactionTemplate;

import java.nio.file.Path;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Runs deploys in the background, after the transaction that requested them commits:
 * PENDING → RUNNING → SUCCEEDED / FAILED (or CANCELLED by a user). A successful deploy becomes the active one of its
 * application and environment. Every step is a short transaction of its own; the script runs outside any.
 */
@Service
public class DeployExecutionService {

    private static final Logger log = LoggerFactory.getLogger(DeployExecutionService.class);
    static final List<DeployStatus> IN_PROGRESS = List.of(DeployStatus.PENDING, DeployStatus.RUNNING);

    private final DeployRepository deployRepository;
    private final PackageStorage packageStorage;
    private final DeployScriptRunner runner;
    private final TaskExecutor executor;
    private final TransactionTemplate transactionTemplate;
    private final String nodeId;
    private final Duration timeout;

    public DeployExecutionService(final DeployRepository deployRepository,
                                  final PackageStorage packageStorage,
                                  final DeployScriptRunner runner,
                                  @Qualifier("deployExecutor") final TaskExecutor executor,
                                  final PlatformTransactionManager transactionManager,
                                  @Qualifier("deployNodeId") final String nodeId,
                                  @Value("${caipora.deploy.timeout:PT30M}") final Duration timeout) {
        this.deployRepository = deployRepository;
        this.packageStorage = packageStorage;
        this.runner = runner;
        this.executor = executor;
        this.transactionTemplate = new TransactionTemplate(transactionManager);
        this.nodeId = nodeId;
        this.timeout = timeout;
    }

    /**
     * Published (inside the transaction) when a deploy is queued; handled once it commits.
     */
    public record DeployRequestedEvent(Integer deployId) {
    }

    public String getNodeId() {
        return nodeId;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onDeployRequested(final DeployRequestedEvent event) {
        try {
            executor.execute(() -> execute(event.deployId()));
        } catch (TaskRejectedException e) {
            log.error("Deploy {} rejected by the executor", event.deployId(), e);
            finish(event.deployId(), DeployStatus.FAILED, null,
                    "[Caipora] Too many deploys are queued on this server: please try again later.\n");
        }
    }

    /**
     * Runs the deploy synchronously (called on a deploy thread).
     */
    void execute(final Integer deployId) {
        final Preparation preparation;
        try {
            preparation = transactionTemplate.execute(status -> start(deployId));
        } catch (RuntimeException e) {
            log.error("Could not start deploy {}", deployId, e);
            finish(deployId, DeployStatus.FAILED, null, "[Caipora] Could not start the deploy: " + e.getMessage() + "\n");
            return;
        }
        if (preparation == null) {
            return; // cancelled before it started, or already handled
        }
        if (preparation.error() != null) {
            finish(deployId, DeployStatus.FAILED, null, "[Caipora] " + preparation.error() + "\n");
            return;
        }
        if (preparation.script() == null) {
            finish(deployId, DeployStatus.SUCCEEDED, null,
                    "[Caipora] The application has no deploy script: the deploy was only recorded.\n");
            return;
        }
        log.info("Running the deploy script of deploy {}", deployId);
        final DeployScriptRunner.Result result = runner.run(preparation.script(), preparation.variables(), timeout,
                output -> progress(deployId, output));
        final DeployStatus status = result.succeeded() ? DeployStatus.SUCCEEDED
                : (result.cancelled() ? DeployStatus.CANCELLED : DeployStatus.FAILED);
        log.info("Deploy {} finished: {} (exit code {})", deployId, status, result.exitCode());
        finish(deployId, status, result.exitCode(), result.output());
    }

    private record Preparation(String script, Map<String, String> variables, String error) {
    }

    private Preparation start(final Integer deployId) {
        final Deploy deploy = deployRepository.findById(deployId).orElse(null);
        if (deploy == null || deploy.getStatus() != DeployStatus.PENDING) {
            return null;
        }
        deploy.setStatus(DeployStatus.RUNNING);
        deploy.setStartedAt(OffsetDateTime.now());
        deploy.setExecutorNode(nodeId);
        deployRepository.save(deploy);

        final Version version = deploy.getVersion();
        final Software software = version.getSoftware();
        final Path packageFile;
        try {
            packageFile = packageStorage.resolvePackage(software.getCode(), version.getFileName());
        } catch (IllegalArgumentException | NotFoundException e) {
            return new Preparation(null, null, "Package " + version.getFileName() + " not found in "
                    + packageStorage.applicationFolder(software.getCode()) + ".");
        }
        final Map<String, String> variables = new LinkedHashMap<>();
        variables.put("CAIPORA_DEPLOY_ID", String.valueOf(deploy.getId()));
        variables.put("CAIPORA_APP_CODE", software.getCode());
        variables.put("CAIPORA_APP_NAME", software.getName());
        variables.put("CAIPORA_VERSION", version.getName());
        variables.put("CAIPORA_ENVIRONMENT", deploy.getEnvironment().name());
        variables.put("CAIPORA_RFC", deploy.getRfc());
        variables.put("CAIPORA_PACKAGE", packageFile.toString());
        variables.put("CAIPORA_PACKAGE_NAME", packageFile.getFileName().toString());
        variables.put("CAIPORA_PACKAGES_FOLDER", packageFile.getParent().toString());
        variables.put("CAIPORA_OPERATOR", deploy.getOperator() == null ? "" : deploy.getOperator().getUsername());
        return new Preparation(software.getDeployScript(), variables, null);
    }

    /**
     * Saves the output so far and tells whether the deploy was cancelled meanwhile (possibly on another instance).
     */
    private boolean progress(final Integer deployId, final String output) {
        try {
            final DeployStatus status = transactionTemplate.execute(tx -> {
                deployRepository.updateScriptOutput(deployId, output);
                return deployRepository.findStatusById(deployId);
            });
            return status == DeployStatus.CANCELLED;
        } catch (RuntimeException e) {
            log.warn("Could not save the progress of deploy {}", deployId, e);
            return false;
        }
    }

    /**
     * Records the outcome; a successful deploy replaces the active deploy of its application and environment.
     * A deploy cancelled meanwhile stays cancelled.
     */
    void finish(final Integer deployId, final DeployStatus status, final Integer exitCode, final String output) {
        try {
            transactionTemplate.executeWithoutResult(tx -> {
                final Deploy deploy = deployRepository.findById(deployId).orElse(null);
                if (deploy == null) {
                    return;
                }
                final boolean cancelled = deploy.getStatus() == DeployStatus.CANCELLED;
                final DeployStatus finalStatus = cancelled ? DeployStatus.CANCELLED : status;
                deploy.setStatus(finalStatus);
                deploy.setExitCode(exitCode);
                deploy.setScriptOutput(output);
                deploy.setFinishedAt(OffsetDateTime.now());
                if (finalStatus == DeployStatus.SUCCEEDED) {
                    activate(deploy);
                }
                deployRepository.save(deploy);
            });
        } catch (RuntimeException e) {
            log.error("Could not record the outcome ({}) of deploy {}", status, deployId, e);
        }
    }

    /**
     * Deactivates the current deploy(s) of the application and environment first (flushed, because of the partial
     * unique index on active deploys), then activates the given one.
     */
    private void activate(final Deploy deploy) {
        final Software software = deploy.getVersion().getSoftware();
        final List<Deploy> active = deployRepository
                .findAllByVersionSoftwareAndEnvironmentAndIsActiveTrue(software, deploy.getEnvironment());
        for (final Deploy previous : active) {
            if (!Objects.equals(previous.getId(), deploy.getId())) {
                previous.setIsActive(false);
            }
        }
        deployRepository.saveAllAndFlush(active);
        deploy.setIsActive(true);
    }

    /**
     * Cancels a deploy that is queued or running. A running script notices it within a second, on whichever
     * instance runs it, and is stopped.
     *
     * @return whether the deploy was in progress
     */
    boolean cancel(final Deploy deploy, final String reason) {
        if (deploy.getStatus() == null || !deploy.getStatus().isInProgress()) {
            return false;
        }
        deploy.setStatus(DeployStatus.CANCELLED);
        // a running script updates it again when it actually stops
        deploy.setFinishedAt(OffsetDateTime.now());
        deploy.setScriptOutput((deploy.getScriptOutput() == null ? "" : deploy.getScriptOutput())
                + "\n[Caipora] " + reason + "\n");
        deployRepository.save(deploy);
        return true;
    }

    /**
     * A deploy left in progress by a previous run of this instance can't finish anymore: it is failed. Deploys of
     * other instances are failed only when they are clearly stale (much older than the timeout).
     */
    @EventListener(ApplicationReadyEvent.class)
    public void failInterruptedDeploys() {
        try {
            transactionTemplate.executeWithoutResult(tx -> {
                final OffsetDateTime staleBefore = OffsetDateTime.now().minus(timeout).minusMinutes(15);
                for (final Deploy deploy : deployRepository.findAllByStatusIn(IN_PROGRESS)) {
                    final boolean mine = nodeId.equals(deploy.getExecutorNode());
                    // a re-run keeps its original creation date: judge by when it (re)started
                    final OffsetDateTime since = deploy.getStartedAt() != null ? deploy.getStartedAt()
                            : (deploy.getUpdatedAt() != null ? deploy.getUpdatedAt() : deploy.getCreatedAt());
                    final boolean stale = since != null && since.isBefore(staleBefore);
                    if (mine || stale) {
                        deploy.setStatus(DeployStatus.FAILED);
                        deploy.setFinishedAt(OffsetDateTime.now());
                        deploy.setScriptOutput((deploy.getScriptOutput() == null ? "" : deploy.getScriptOutput())
                                + "\n[Caipora] Interrupted: the server was restarted while the deploy was in progress.\n");
                        deployRepository.save(deploy);
                        log.warn("Deploy {} was interrupted by a restart and marked as failed", deploy.getId());
                    }
                }
            });
        } catch (RuntimeException e) {
            log.error("Could not fail the deploys interrupted by a restart", e);
        }
    }
}
