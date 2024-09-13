package br.dev.ampliar.caipora.service;

import br.dev.ampliar.caipora.domain.Deploy;
import br.dev.ampliar.caipora.domain.Stakeholder;
import br.dev.ampliar.caipora.domain.User;
import br.dev.ampliar.caipora.domain.Version;
import br.dev.ampliar.caipora.model.DeployDTO;
import br.dev.ampliar.caipora.repos.StakeholderRepository;
import br.dev.ampliar.caipora.repos.UserRepository;
import br.dev.ampliar.caipora.repos.VersionRepository;
import br.dev.ampliar.caipora.util.NotFoundException;
import org.mapstruct.AfterMapping;
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;


@Mapper(
        componentModel = MappingConstants.ComponentModel.SPRING,
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface DeployMapper {

    @Mapping(target = "version", ignore = true)
    @Mapping(target = "operator", ignore = true)
    @Mapping(target = "authorizer", ignore = true)
    DeployDTO updateDeployDTO(Deploy deploy, @MappingTarget DeployDTO deployDTO);

    @AfterMapping
    default void afterUpdateDeployDTO(Deploy deploy, @MappingTarget DeployDTO deployDTO) {
        deployDTO.setVersion(deploy.getVersion() == null ? null : deploy.getVersion().getId());
        deployDTO.setOperator(deploy.getOperator() == null ? null : deploy.getOperator().getId());
        deployDTO.setAuthorizer(deploy.getAuthorizer() == null ? null : deploy.getAuthorizer().getId());
    }

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "operator", ignore = true)
    @Mapping(target = "authorizer", ignore = true)
    Deploy updateDeploy(DeployDTO deployDTO, @MappingTarget Deploy deploy,
            @Context VersionRepository versionRepository, @Context UserRepository userRepository,
            @Context StakeholderRepository stakeholderRepository);

    @AfterMapping
    default void afterUpdateDeploy(DeployDTO deployDTO, @MappingTarget Deploy deploy,
            @Context VersionRepository versionRepository, @Context UserRepository userRepository,
            @Context StakeholderRepository stakeholderRepository) {
        final Version version = deployDTO.getVersion() == null ? null : versionRepository.findById(deployDTO.getVersion())
                .orElseThrow(() -> new NotFoundException("version not found"));
        deploy.setVersion(version);
        final User operator = deployDTO.getOperator() == null ? null : userRepository.findById(deployDTO.getOperator())
                .orElseThrow(() -> new NotFoundException("operator not found"));
        deploy.setOperator(operator);
        final Stakeholder authorizer = deployDTO.getAuthorizer() == null ? null : stakeholderRepository.findById(deployDTO.getAuthorizer())
                .orElseThrow(() -> new NotFoundException("authorizer not found"));
        deploy.setAuthorizer(authorizer);
    }

}
