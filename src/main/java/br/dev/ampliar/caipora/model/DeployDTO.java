package br.dev.ampliar.caipora.model;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.time.OffsetDateTime;


@SuppressWarnings("unused")
public class DeployDTO {

    private Integer id;

    private Integer softwareId;

    private String softwareCode;

    private String softwareName;

    @NotNull
    private Environment environment;

    private Boolean isActive;

    @NotNull
    @Size(max = 30)
    @DeployRfcUnique
    private String rfc;

    @NotNull
    private LocalDate executionDate;

    private String notes;

    @NotNull
    private Integer versionId;

    @NotNull
    private Integer authorizerId;

    private Integer operatorId;

    private String operatorName;

    private OffsetDateTime createdAt;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public @NotNull Environment getEnvironment() {
        return environment;
    }

    public void setEnvironment(@NotNull Environment environment) {
        this.environment = environment;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean active) {
        isActive = active;
    }

    public @NotNull @Size(max = 30) String getRfc() {
        return rfc;
    }

    public void setRfc(@NotNull @Size(max = 30) String rfc) {
        this.rfc = rfc;
    }

    public @NotNull LocalDate getExecutionDate() {
        return executionDate;
    }

    public void setExecutionDate(@NotNull LocalDate executionDate) {
        this.executionDate = executionDate;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public @NotNull Integer getVersionId() {
        return versionId;
    }

    public void setVersionId(@NotNull Integer versionId) {
        this.versionId = versionId;
    }

    public Integer getOperatorId() {
        return operatorId;
    }

    public void setOperatorId(Integer operatorId) {
        this.operatorId = operatorId;
    }

    public String getOperatorName() {
        return operatorName;
    }

    public void setOperatorName(String operatorName) {
        this.operatorName = operatorName;
    }

    public @NotNull Integer getAuthorizerId() {
        return authorizerId;
    }

    public void setAuthorizerId(@NotNull Integer authorizerId) {
        this.authorizerId = authorizerId;
    }

    public Integer getSoftwareId() {
        return softwareId;
    }

    public void setSoftwareId(Integer softwareId) {
        this.softwareId = softwareId;
    }

    public String getSoftwareCode() {
        return softwareCode;
    }

    public void setSoftwareCode(String softwareCode) {
        this.softwareCode = softwareCode;
    }

    public String getSoftwareName() {
        return softwareName;
    }

    public void setSoftwareName(String softwareName) {
        this.softwareName = softwareName;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(OffsetDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
