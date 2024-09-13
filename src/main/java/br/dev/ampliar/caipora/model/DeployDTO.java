package br.dev.ampliar.caipora.model;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;


public class DeployDTO {

    private Integer id;

    @NotNull
    private Environment environment;

    @NotNull
    private Integer sequence;

    @NotNull
    @Size(max = 30)
    @DeployRfcUnique
    private String rfc;

    @NotNull
    private LocalDateTime executionDate;

    @NotNull
    private Integer version;

    @NotNull
    private Integer operator;

    @NotNull
    private Integer authorizer;

    public Integer getId() {
        return id;
    }

    public void setId(final Integer id) {
        this.id = id;
    }

    public Environment getEnvironment() {
        return environment;
    }

    public void setEnvironment(final Environment environment) {
        this.environment = environment;
    }

    public Integer getSequence() {
        return sequence;
    }

    public void setSequence(final Integer sequence) {
        this.sequence = sequence;
    }

    public String getRfc() {
        return rfc;
    }

    public void setRfc(final String rfc) {
        this.rfc = rfc;
    }

    public LocalDateTime getExecutionDate() {
        return executionDate;
    }

    public void setExecutionDate(final LocalDateTime executionDate) {
        this.executionDate = executionDate;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(final Integer version) {
        this.version = version;
    }

    public Integer getOperator() {
        return operator;
    }

    public void setOperator(final Integer operator) {
        this.operator = operator;
    }

    public Integer getAuthorizer() {
        return authorizer;
    }

    public void setAuthorizer(final Integer authorizer) {
        this.authorizer = authorizer;
    }

}