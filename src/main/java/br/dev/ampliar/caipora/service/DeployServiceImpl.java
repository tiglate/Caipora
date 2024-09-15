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
import org.springframework.stereotype.Service;


@Service
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
        final Deploy deploy = new Deploy();
        mapToEntity(deployDTO, deploy);
        return deployRepository.save(deploy).getId();
    }

    private DeployDTO mapToDTO(final Deploy deploy, final DeployDTO deployDTO) {
        deployDTO.setId(deploy.getId());
        deployDTO.setEnvironment(deploy.getEnvironment());
        deployDTO.setIsActive(deploy.getIsActive());
        deployDTO.setRfc(deploy.getRfc());
        deployDTO.setExecutionDate(deploy.getExecutionDate());
        deployDTO.setVersion(deploy.getVersion() == null ? null : deploy.getVersion().getId());
        deployDTO.setOperator(deploy.getOperator() == null ? null : deploy.getOperator().getId());
        deployDTO.setAuthorizer(deploy.getAuthorizer() == null ? null : deploy.getAuthorizer().getId());
        return deployDTO;
    }

    private Deploy mapToEntity(final DeployDTO deployDTO, final Deploy deploy) {
        deploy.setEnvironment(deployDTO.getEnvironment());
        deploy.setIsActive(deployDTO.getIsActive());
        deploy.setRfc(deployDTO.getRfc());
        deploy.setExecutionDate(deployDTO.getExecutionDate());
        final Version version = deployDTO.getVersion() == null ? null : versionRepository.findById(deployDTO.getVersion())
                .orElseThrow(() -> new NotFoundException("version not found"));
        deploy.setVersion(version);
        final User operator = deployDTO.getOperator() == null ? null : userRepository.findById(deployDTO.getOperator())
                .orElseThrow(() -> new NotFoundException("operator not found"));
        deploy.setOperator(operator);
        final Stakeholder authorizer = deployDTO.getAuthorizer() == null ? null : stakeholderRepository.findById(deployDTO.getAuthorizer())
                .orElseThrow(() -> new NotFoundException("authorizer not found"));
        deploy.setAuthorizer(authorizer);
        return deploy;
    }

    @Override
    public boolean rfcExists(final String rfc) {
        return deployRepository.existsByRfcIgnoreCase(rfc);
    }

}
