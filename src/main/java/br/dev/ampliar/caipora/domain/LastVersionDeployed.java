package br.dev.ampliar.caipora.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.springframework.data.annotation.Immutable;

import java.time.LocalDate;

@Entity
@Immutable
@Table(name = "vw_last_version_deployed") // Map to the view
public class LastVersionDeployed {

    @Column(name = "id_software")
    private Integer softwareId;

    @Column(name = "software_code")
    private String softwareCode;

    @Column(name = "software_name")
    private String softwareName;

    @Id
    @Column(name = "id_deploy")
    private Integer deployId;

    @Column(name = "environment")
    private String environment;

    @Column(name = "execution_date")
    private LocalDate executionDate;

    @Column(name = "rfc")
    private String rfc;

    @Column(name = "version")
    private String version;

    @Column(name = "operator")
    private String operator;

    @Column(name = "authorizer")
    private String authorizer;

    public Integer getSoftwareId() {
        return softwareId;
    }

    public String getSoftwareCode() {
        return softwareCode;
    }

    public String getSoftwareName() {
        return softwareName;
    }

    public Integer getDeployId() {
        return deployId;
    }

    public String getEnvironment() {
        return environment;
    }

    public LocalDate getExecutionDate() {
        return executionDate;
    }

    public String getRfc() {
        return rfc;
    }

    public String getVersion() {
        return version;
    }

    public String getOperator() {
        return operator;
    }

    public String getAuthorizer() {
        return authorizer;
    }
}