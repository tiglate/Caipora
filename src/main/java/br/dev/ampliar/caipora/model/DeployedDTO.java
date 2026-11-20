package br.dev.ampliar.caipora.model;

import java.time.LocalDate;

/**
 * State of one environment of an application: the active deploy (if any) and the deploy in progress (if any).
 */
@SuppressWarnings("unused")
public class DeployedDTO {
    private Environment environment;
    private Integer deployId;
    private LocalDate executionDate;
    private String rfc;
    private String version;
    private String operator;
    private String authorizer;
    private Integer inProgressDeployId;
    private DeployStatus inProgressStatus;
    private String inProgressVersion;

    public DeployedDTO() {
    }

    public DeployedDTO(Integer deployId, LocalDate executionDate, String rfc, String version, String operator, String authorizer) {
        this.deployId = deployId;
        this.executionDate = executionDate;
        this.rfc = rfc;
        this.version = version;
        this.operator = operator;
        this.authorizer = authorizer;
    }

    public Environment getEnvironment() {
        return environment;
    }

    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }

    public Integer getDeployId() {
        return deployId;
    }

    public void setDeployId(Integer deployId) {
        this.deployId = deployId;
    }

    public LocalDate getExecutionDate() {
        return executionDate;
    }

    public void setExecutionDate(LocalDate executionDate) {
        this.executionDate = executionDate;
    }

    public String getRfc() {
        return rfc;
    }

    public void setRfc(String rfc) {
        this.rfc = rfc;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getOperator() {
        return operator;
    }

    public void setOperator(String operator) {
        this.operator = operator;
    }

    public String getAuthorizer() {
        return authorizer;
    }

    public void setAuthorizer(String authorizer) {
        this.authorizer = authorizer;
    }

    public Integer getInProgressDeployId() {
        return inProgressDeployId;
    }

    public void setInProgressDeployId(Integer inProgressDeployId) {
        this.inProgressDeployId = inProgressDeployId;
    }

    public DeployStatus getInProgressStatus() {
        return inProgressStatus;
    }

    public void setInProgressStatus(DeployStatus inProgressStatus) {
        this.inProgressStatus = inProgressStatus;
    }

    public String getInProgressVersion() {
        return inProgressVersion;
    }

    public void setInProgressVersion(String inProgressVersion) {
        this.inProgressVersion = inProgressVersion;
    }

    public boolean isDeployed() {
        return deployId != null;
    }

    public boolean isInProgress() {
        return inProgressDeployId != null;
    }

    /**
     * @return the environment the active version can be promoted to, or null
     */
    public Environment getPromoteTo() {
        return deployId == null || environment == null ? null : environment.next();
    }
}
