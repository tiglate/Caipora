package br.dev.ampliar.caipora.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class DeployedDTO {
    private Integer deployId;
    private LocalDate executionDate;
    private String rfc;
    private String version;
    private String operator;
    private String authorizer;

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
}
