package br.dev.ampliar.caipora.model;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * An application and the state of each of its environments (deploy overview card).
 */
@SuppressWarnings("unused")
public class SoftwareDeployedDTO {
    private Integer softwareId;
    private String code;
    private String name;
    private boolean hasScript;
    private final Map<Environment, DeployedDTO> environments = new EnumMap<>(Environment.class);

    public SoftwareDeployedDTO(Integer softwareId, String softwareCode, String softwareName) {
        this.softwareId = softwareId;
        this.code = softwareCode;
        this.name = softwareName;
        for (final Environment environment : Environment.values()) {
            final DeployedDTO deployed = new DeployedDTO();
            deployed.setEnvironment(environment);
            environments.put(environment, deployed);
        }
    }

    public Integer getSoftwareId() {
        return softwareId;
    }

    public void setSoftwareId(Integer softwareId) {
        this.softwareId = softwareId;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isHasScript() {
        return hasScript;
    }

    public void setHasScript(boolean hasScript) {
        this.hasScript = hasScript;
    }

    public DeployedDTO get(final Environment environment) {
        return environments.get(environment);
    }

    /**
     * @return the environments in pipeline order
     */
    public List<DeployedDTO> getEnvironments() {
        return List.copyOf(environments.values());
    }

    public boolean isInProgress() {
        return environments.values().stream().anyMatch(DeployedDTO::isInProgress);
    }

    public boolean isNeverDeployed() {
        return environments.values().stream().noneMatch(DeployedDTO::isDeployed);
    }
}
