package br.dev.ampliar.caipora.model;

import br.dev.ampliar.caipora.domain.LastVersionDeployed;
import br.dev.ampliar.caipora.repos.LastVersionDeployedRepository;
import br.dev.ampliar.caipora.service.SoftwareDeployedService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class SoftwareDeployedServiceImpl implements SoftwareDeployedService {

    private final LastVersionDeployedRepository lastVersionDeployedRepository;

    public SoftwareDeployedServiceImpl(LastVersionDeployedRepository lastVersionDeployedRepository) {
        this.lastVersionDeployedRepository = lastVersionDeployedRepository;
    }

    @Override
    public List<SoftwareDeployedDTO> getSoftwareDeployedDTOs() {
        List<LastVersionDeployed> deployedVersions = lastVersionDeployedRepository.findAll();
        return mapToSoftwareDeployedDTOs(deployedVersions);
    }

    private List<SoftwareDeployedDTO> mapToSoftwareDeployedDTOs(List<LastVersionDeployed> deployedVersions) {
        Map<Integer, SoftwareDeployedDTO> softwareDTOMap = new HashMap<>();

        for (var item : deployedVersions) {
            Integer softwareId = item.getSoftwareId();

            var softwareDTO = softwareDTOMap.computeIfAbsent(softwareId,
                    id -> {
                        SoftwareDeployedDTO dto = new SoftwareDeployedDTO(
                                softwareId,
                                item.getSoftwareCode(),
                                item.getSoftwareName()
                        );
                        dto.setDev(new DeployedDTO());
                        dto.setQa(new DeployedDTO());
                        dto.setUat(new DeployedDTO());
                        dto.setProd(new DeployedDTO());
                        dto.setDr(new DeployedDTO());
                        return dto;
                    });

            var deployedDTO = new DeployedDTO(
                    item.getDeployId(),
                    item.getExecutionDate(),
                    item.getRfc(),
                    item.getVersion(),
                    item.getOperator(),
                    item.getAuthorizer()
            );

            // Set the DeployedDTO to the correct environment field in the SoftwareDeployedDTO
            switch (item.getEnvironment().toUpperCase()) {
                case "DEVELOPMENT" -> softwareDTO.setDev(deployedDTO);
                case "QA" -> softwareDTO.setQa(deployedDTO);
                case "UAT" -> softwareDTO.setUat(deployedDTO);
                case "PRODUCTION" -> softwareDTO.setProd(deployedDTO);
                case "DR" -> softwareDTO.setDr(deployedDTO);
                default -> throw new IllegalArgumentException("Invalid environment: " + item.getEnvironment());
            }
        }

        return new ArrayList<>(softwareDTOMap.values());
    }
}
