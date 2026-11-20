package br.dev.ampliar.caipora.service;

import br.dev.ampliar.caipora.domain.Deploy;
import br.dev.ampliar.caipora.domain.Software;
import br.dev.ampliar.caipora.domain.Stakeholder;
import br.dev.ampliar.caipora.domain.User;
import br.dev.ampliar.caipora.domain.Version;
import br.dev.ampliar.caipora.model.DeployDTO;
import br.dev.ampliar.caipora.model.DeployStatus;
import br.dev.ampliar.caipora.model.Environment;
import br.dev.ampliar.caipora.repos.DeployRepository;
import br.dev.ampliar.caipora.repos.SoftwareRepository;
import br.dev.ampliar.caipora.repos.StakeholderRepository;
import br.dev.ampliar.caipora.repos.UserRepository;
import br.dev.ampliar.caipora.repos.VersionRepository;
import br.dev.ampliar.caipora.util.HtmlSanitizer;
import br.dev.ampliar.caipora.util.NotFoundException;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.Optional;


@Service
@Transactional(readOnly = true)
public class DeployServiceImpl implements DeployService {

    private final DeployRepository deployRepository;
    private final VersionRepository versionRepository;
    private final UserRepository userRepository;
    private final StakeholderRepository stakeholderRepository;
    private final SoftwareRepository softwareRepository;
    private final DeployExecutionService executionService;
    private final ApplicationEventPublisher eventPublisher;

    public DeployServiceImpl(final DeployRepository deployRepository,
                             final VersionRepository versionRepository, final UserRepository userRepository,
                             final StakeholderRepository stakeholderRepository,
                             final SoftwareRepository softwareRepository,
                             final DeployExecutionService executionService,
                             final ApplicationEventPublisher eventPublisher) {
        this.deployRepository = deployRepository;
        this.versionRepository = versionRepository;
        this.userRepository = userRepository;
        this.stakeholderRepository = stakeholderRepository;
        this.softwareRepository = softwareRepository;
        this.executionService = executionService;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public DeployDTO get(final Integer id) {
        return deployRepository.findById(id)
                .map(deploy -> mapToDTO(deploy, new DeployDTO()))
                .orElseThrow(NotFoundException::new);
    }

    @Override
    @Transactional
    public Integer create(final DeployDTO deployDTO) {
        final Version version = deployDTO.getVersionId() == null ? null : versionRepository.findById(deployDTO.getVersionId())
                .orElseThrow(() -> new NotFoundException("version not found"));
        if (version != null) {
            lockAndEnsureIdle(version.getSoftware(), deployDTO.getEnvironment());
        }
        final Deploy deploy = mapToEntity(deployDTO, new Deploy());
        deploy.setStatus(DeployStatus.PENDING);
        // it becomes the active deploy of the environment only when it succeeds
        deploy.setIsActive(false);
        final Integer id = deployRepository.save(deploy).getId();
        eventPublisher.publishEvent(new DeployExecutionService.DeployRequestedEvent(id));
        return id;
    }

    @Override
    @Transactional
    public void rerun(final Integer id, final Integer operatorId) {
        final Deploy deploy = deployRepository.findById(id)
                .orElseThrow(NotFoundException::new);
        if (deploy.getStatus() == null || !deploy.getStatus().canBeRerun()) {
            throw new IllegalStateException("Only a failed or cancelled deploy can be run again.");
        }
        lockAndEnsureIdle(deploy.getVersion().getSoftware(), deploy.getEnvironment());
        deploy.setStatus(DeployStatus.PENDING);
        deploy.setIsActive(false);
        deploy.setExitCode(null);
        deploy.setStartedAt(null);
        deploy.setFinishedAt(null);
        deploy.setScriptOutput(null);
        deploy.setExecutorNode(null);
        deploy.setExecutionDate(LocalDate.now());
        if (operatorId != null) {
            deploy.setOperator(userRepository.findById(operatorId)
                    .orElseThrow(() -> new NotFoundException("operator not found")));
        }
        deployRepository.save(deploy);
        eventPublisher.publishEvent(new DeployExecutionService.DeployRequestedEvent(id));
    }

    @Override
    @Transactional
    public boolean cancel(final Integer id, final String reason) {
        final Deploy deploy = deployRepository.findById(id)
                .orElseThrow(NotFoundException::new);
        return executionService.cancel(deploy, reason);
    }

    @Override
    @Transactional
    public int unlock(final Integer softwareId, final String reason) {
        final Software software = softwareRepository.findById(softwareId)
                .orElseThrow(NotFoundException::new);
        int count = 0;
        for (final Deploy deploy : deployRepository.findAllByVersionSoftwareAndStatusIn(software,
                DeployExecutionService.IN_PROGRESS)) {
            if (executionService.cancel(deploy, reason)) {
                count++;
            }
        }
        return count;
    }

    @Override
    public Page<DeployDTO> findHistory(final Integer softwareId, final Pageable pageable) {
        final Software software = softwareRepository.findById(softwareId)
                .orElseThrow(NotFoundException::new);
        return deployRepository.findAllByVersionSoftware(software, pageable)
                .map(deploy -> mapToDTO(deploy, new DeployDTO()));
    }

    @Override
    public Optional<Integer> findRollbackVersion(final Integer softwareId, final Environment environment) {
        final Software software = softwareRepository.findById(softwareId)
                .orElseThrow(NotFoundException::new);
        final List<Deploy> active = deployRepository.findAllByVersionSoftwareAndEnvironmentAndIsActiveTrue(software, environment);
        final Integer activeVersionId = active.isEmpty() ? null : active.getFirst().getVersion().getId();
        return deployRepository.findAllByVersionSoftwareAndEnvironmentAndStatusOrderByIdDesc(software, environment,
                        DeployStatus.SUCCEEDED).stream()
                .map(deploy -> deploy.getVersion().getId())
                .filter(versionId -> !Objects.equals(versionId, activeVersionId))
                .findFirst();
    }

    /**
     * Takes the lock of the application (until the end of the transaction, on every instance) and checks that it is
     * not being deployed to the environment already.
     */
    private void lockAndEnsureIdle(final Software software, final Environment environment) {
        final Software locked = softwareRepository.findByIdForUpdate(software.getId())
                .orElseThrow(() -> new NotFoundException("software not found"));
        if (environment != null && deployRepository.existsByVersionSoftwareAndEnvironmentAndStatusIn(locked,
                environment, DeployExecutionService.IN_PROGRESS)) {
            throw new DeployInProgressException("%s is already being deployed to %s. Wait for it to finish, or cancel it."
                    .formatted(locked.getName(), environment));
        }
    }

    private DeployDTO mapToDTO(final Deploy deploy, final DeployDTO deployDTO) {
        deployDTO.setId(deploy.getId());
        deployDTO.setEnvironment(deploy.getEnvironment());
        deployDTO.setIsActive(deploy.getIsActive());
        deployDTO.setRfc(deploy.getRfc());
        deployDTO.setExecutionDate(deploy.getExecutionDate());
        deployDTO.setCreatedAt(deploy.getCreatedAt());
        deployDTO.setNotes(HtmlSanitizer.sanitize(deploy.getNotes()));
        deployDTO.setStatus(deploy.getStatus());
        deployDTO.setExitCode(deploy.getExitCode());
        deployDTO.setStartedAt(deploy.getStartedAt());
        deployDTO.setFinishedAt(deploy.getFinishedAt());
        deployDTO.setScriptOutput(deploy.getScriptOutput());
        deployDTO.setExecutorNode(deploy.getExecutorNode());
        if (deploy.getAuthorizer() != null) {
            deployDTO.setAuthorizerId(deploy.getAuthorizer().getId());
            deployDTO.setAuthorizerName(deploy.getAuthorizer().getName());
        }
        if (deploy.getOperator() != null) {
            final var operator = deploy.getOperator();
            deployDTO.setOperatorId(operator.getId());
            deployDTO.setOperatorName(operator.getName());
        }
        if (deploy.getVersion() != null) {
            final var version = deploy.getVersion();
            deployDTO.setVersionId(version.getId());
            deployDTO.setVersionName(version.getName());
            deployDTO.setFileName(version.getFileName());
            if (version.getSoftware() != null) {
                final var software = version.getSoftware();
                deployDTO.setSoftwareId(software.getId());
                deployDTO.setSoftwareCode(software.getCode());
                deployDTO.setSoftwareName(software.getName());
            }
        }
        return deployDTO;
    }

    private Deploy mapToEntity(final DeployDTO deployDTO, final Deploy deploy) {
        deploy.setEnvironment(deployDTO.getEnvironment());
        deploy.setIsActive(deployDTO.getIsActive());
        deploy.setRfc(deployDTO.getRfc());
        deploy.setExecutionDate(deployDTO.getExecutionDate());
        deploy.setNotes(HtmlSanitizer.sanitize(deployDTO.getNotes()));
        final Version version = deployDTO.getVersionId() == null ? null : versionRepository.findById(deployDTO.getVersionId())
                .orElseThrow(() -> new NotFoundException("version not found"));
        deploy.setVersion(version);
        final User operator = deployDTO.getOperatorId() == null ? null : userRepository.findById(deployDTO.getOperatorId())
                .orElseThrow(() -> new NotFoundException("operator not found"));
        deploy.setOperator(operator);
        final Stakeholder authorizer = deployDTO.getAuthorizerId() == null ? null : stakeholderRepository.findById(deployDTO.getAuthorizerId())
                .orElseThrow(() -> new NotFoundException("authorizer not found"));
        deploy.setAuthorizer(authorizer);
        return deploy;
    }

    @Override
    public boolean rfcExists(final String rfc) {
        return deployRepository.existsByRfcIgnoreCase(rfc);
    }

}
