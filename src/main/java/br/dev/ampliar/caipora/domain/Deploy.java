package br.dev.ampliar.caipora.domain;

import br.dev.ampliar.caipora.model.Environment;
import jakarta.persistence.*;
import org.hibernate.envers.Audited;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;


@Audited
@Entity
@Table(name = "tb_deploy")
@EntityListeners(AuditingEntityListener.class)
public class Deploy {

    @Id
    @Column(name = "id_deploy", nullable = false, updatable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Environment environment;

    @Column(nullable = false)
    private Boolean isActive;

    @Column(nullable = false, unique = true, length = 30)
    private String rfc;

    @Column(nullable = false)
    private LocalDateTime executionDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_version", nullable = false)
    private Version version;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_operator", nullable = false)
    private User operator;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_authorizer", nullable = false)
    private Stakeholder authorizer;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false, columnDefinition = "datetime2")
    private OffsetDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false, columnDefinition = "datetime2")
    private OffsetDateTime updatedAt;

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

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(final Boolean isActive) {
        this.isActive = isActive;
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

    public Version getVersion() {
        return version;
    }

    public void setVersion(final Version version) {
        this.version = version;
    }

    public User getOperator() {
        return operator;
    }

    public void setOperator(final User operator) {
        this.operator = operator;
    }

    public Stakeholder getAuthorizer() {
        return authorizer;
    }

    public void setAuthorizer(final Stakeholder authorizer) {
        this.authorizer = authorizer;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(OffsetDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(OffsetDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

}