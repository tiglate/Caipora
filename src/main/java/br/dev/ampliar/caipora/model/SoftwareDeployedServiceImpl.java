package br.dev.ampliar.caipora.model;

import br.dev.ampliar.caipora.domain.Deploy;
import br.dev.ampliar.caipora.domain.LastVersionDeployed;
import br.dev.ampliar.caipora.domain.Software;
import br.dev.ampliar.caipora.repos.DeployRepository;
import br.dev.ampliar.caipora.repos.LastVersionDeployedRepository;
import br.dev.ampliar.caipora.repos.SoftwareRepository;
import br.dev.ampliar.caipora.service.SoftwareDeployedService;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * Builds the deploy overview: every application (also the ones never deployed), with the active deploy and the
 * deploy in progress of each environment.
 */
@Service
@Transactional(readOnly = true)
public class SoftwareDeployedServiceImpl implements SoftwareDeployedService {

    private static final List<DeployStatus> IN_PROGRESS = List.of(DeployStatus.PENDING, DeployStatus.RUNNING);

    private final LastVersionDeployedRepository lastVersionDeployedRepository;
    private final SoftwareRepository softwareRepository;
    private final DeployRepository deployRepository;

    public SoftwareDeployedServiceImpl(final LastVersionDeployedRepository lastVersionDeployedRepository,
                                       final SoftwareRepository softwareRepository,
                                       final DeployRepository deployRepository) {
        this.lastVersionDeployedRepository = lastVersionDeployedRepository;
        this.softwareRepository = softwareRepository;
        this.deployRepository = deployRepository;
    }

    @Override
    public List<SoftwareDeployedDTO> getSoftwareDeployedDTOs() {
        final Map<Integer, SoftwareDeployedDTO> cards = new LinkedHashMap<>();
        for (final Software software : softwareRepository.findAll(Sort.by(Sort.Order.asc("code").ignoreCase()))) {
            final SoftwareDeployedDTO card = new SoftwareDeployedDTO(software.getId(), software.getCode(), software.getName());
            card.setHasScript(software.getDeployScript() != null && !software.getDeployScript().isBlank());
            cards.put(software.getId(), card);
        }

        // several active rows for one environment can exist in data recorded before deploys were serialized:
        // the most recent one wins
        final List<LastVersionDeployed> active = new ArrayList<>(lastVersionDeployedRepository.findAll());
        active.sort(Comparator.comparing(LastVersionDeployed::getExecutionDate, Comparator.nullsFirst(Comparator.naturalOrder()))
                .thenComparing(LastVersionDeployed::getDeployId));
        for (final LastVersionDeployed item : active) {
            final SoftwareDeployedDTO card = cards.get(item.getSoftwareId());
            final Environment environment = parseEnvironment(item.getEnvironment());
            if (card == null || environment == null) {
                continue;
            }
            final DeployedDTO deployed = card.get(environment);
            deployed.setDeployId(item.getDeployId());
            deployed.setExecutionDate(item.getExecutionDate());
            deployed.setRfc(item.getRfc());
            deployed.setVersion(item.getVersion());
            deployed.setOperator(item.getOperator());
            deployed.setAuthorizer(item.getAuthorizer());
        }

        for (final Deploy deploy : deployRepository.findAllByStatusIn(IN_PROGRESS)) {
            final SoftwareDeployedDTO card = cards.get(deploy.getVersion().getSoftware().getId());
            if (card == null) {
                continue;
            }
            final DeployedDTO deployed = card.get(deploy.getEnvironment());
            deployed.setInProgressDeployId(deploy.getId());
            deployed.setInProgressStatus(deploy.getStatus());
            deployed.setInProgressVersion(deploy.getVersion().getName());
        }
        return List.copyOf(cards.values());
    }

    private static Environment parseEnvironment(final String value) {
        try {
            return value == null ? null : Environment.valueOf(value.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
