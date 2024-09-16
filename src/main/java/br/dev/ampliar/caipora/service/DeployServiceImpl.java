package br.dev.ampliar.caipora.service;

import br.dev.ampliar.caipora.domain.Deploy;
import br.dev.ampliar.caipora.domain.Stakeholder;
import br.dev.ampliar.caipora.domain.User;
import br.dev.ampliar.caipora.domain.Version;
import br.dev.ampliar.caipora.model.DeployDTO;
import br.dev.ampliar.caipora.repos.DeployRepository;
import br.dev.ampliar.caipora.repos.StakeholderRepository;
import br.dev.ampliar.caipora.repos.UserRepository;
import br.dev.ampliar.caipora.repos.VersionRepository;
import br.dev.ampliar.caipora.util.NotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;


@Service
@Transactional
public class DeployServiceImpl implements DeployService {

    private final DeployRepository deployRepository;
    private final VersionRepository versionRepository;
    private final UserRepository userRepository;
    private final StakeholderRepository stakeholderRepository;

    public DeployServiceImpl(final DeployRepository deployRepository,
                             final VersionRepository versionRepository, final UserRepository userRepository,
                             final StakeholderRepository stakeholderRepository) {
        this.deployRepository = deployRepository;
        this.versionRepository = versionRepository;
        this.userRepository = userRepository;
        this.stakeholderRepository = stakeholderRepository;
    }

    @Override
    public DeployDTO get(final Integer id) {
        return deployRepository.findById(id)
                .map(deploy -> mapToDTO(deploy, new DeployDTO()))
                .orElseThrow(NotFoundException::new);
    }

    @Override
    public Integer create(final DeployDTO deployDTO) {
        final var deploy = mapToEntity(deployDTO);
        return deployRepository.save(deploy).getId();
    }

    private DeployDTO mapToDTO(final Deploy deploy, final DeployDTO deployDTO) {
        deployDTO.setId(deploy.getId());
        deployDTO.setEnvironment(deploy.getEnvironment());
        deployDTO.setIsActive(deploy.getIsActive());
        deployDTO.setRfc(deploy.getRfc());
        deployDTO.setExecutionDate(deploy.getExecutionDate());
        deployDTO.setCreatedAt(deploy.getCreatedAt());
        deployDTO.setNotes(deploy.getNotes());
        deployDTO.setAuthorizerId(deploy.getAuthorizer() == null ? null : deploy.getAuthorizer().getId());
        if (deploy.getOperator() != null) {
            final var operator = deploy.getOperator();
            deployDTO.setOperatorId(operator.getId());
            deployDTO.setOperatorName(operator.getName());
        } else {
            deployDTO.setOperatorId(null);
            deployDTO.setOperatorName(null);
        }
        if (deploy.getVersion() != null && deploy.getVersion().getSoftware() != null) {
            final var version = deploy.getVersion();
            final var software = version.getSoftware();
            deployDTO.setVersionId(version.getId());
            deployDTO.setSoftwareId(software.getId());
            deployDTO.setSoftwareCode(software.getCode());
            deployDTO.setSoftwareName(software.getName());
        } else {
            deployDTO.setVersionId(null);
            deployDTO.setSoftwareId(null);
            deployDTO.setSoftwareCode(null);
            deployDTO.setSoftwareName(null);
        }
        return deployDTO;
    }

    private Deploy mapToEntity(final DeployDTO deployDTO) {
        return mapToEntity(deployDTO, new Deploy());
    }

    private Deploy mapToEntity(final DeployDTO deployDTO, final Deploy deploy) {
        deploy.setEnvironment(deployDTO.getEnvironment());
        deploy.setIsActive(deployDTO.getIsActive());
        deploy.setRfc(deployDTO.getRfc());
        deploy.setExecutionDate(deployDTO.getExecutionDate());
        deploy.setNotes(deployDTO.getNotes());
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
