package br.dev.ampliar.caipora.model;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.Duration;
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

    private String versionName;

    private String fileName;

    private String authorizerName;

    private DeployStatus status;

    private Integer exitCode;

    private OffsetDateTime startedAt;

    private OffsetDateTime finishedAt;

    private String scriptOutput;

    private String executorNode;

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

    public String getVersionName() {
        return versionName;
    }

    public void setVersionName(String versionName) {
        this.versionName = versionName;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getAuthorizerName() {
        return authorizerName;
    }

    public void setAuthorizerName(String authorizerName) {
        this.authorizerName = authorizerName;
    }

    public DeployStatus getStatus() {
        return status;
    }

    public void setStatus(DeployStatus status) {
        this.status = status;
    }

    public Integer getExitCode() {
        return exitCode;
    }

    public void setExitCode(Integer exitCode) {
        this.exitCode = exitCode;
    }

    public OffsetDateTime getStartedAt() {
        return startedAt;
    }

    public void setStartedAt(OffsetDateTime startedAt) {
        this.startedAt = startedAt;
    }

    public OffsetDateTime getFinishedAt() {
        return finishedAt;
    }

    public void setFinishedAt(OffsetDateTime finishedAt) {
        this.finishedAt = finishedAt;
    }

    public String getScriptOutput() {
        return scriptOutput;
    }

    public void setScriptOutput(String scriptOutput) {
        this.scriptOutput = scriptOutput;
    }

    public String getExecutorNode() {
        return executorNode;
    }

    public void setExecutorNode(String executorNode) {
        this.executorNode = executorNode;
    }

    public boolean isInProgress() {
        return status != null && status.isInProgress();
    }

    /**
     * @return how long the script ran (until now while running), e.g. "1 min 05 s"; null when it hasn't started
     */
    public String getDuration() {
        if (startedAt == null) {
            return null;
        }
        final OffsetDateTime end = finishedAt != null ? finishedAt : OffsetDateTime.now();
        final long seconds = Math.max(0, Duration.between(startedAt, end).toSeconds());
        return seconds < 60 ? seconds + " s" : "%d min %02d s".formatted(seconds / 60, seconds % 60);
    }
}
