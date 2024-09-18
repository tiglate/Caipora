package br.dev.ampliar.caipora.domain;

import jakarta.persistence.*;
import org.hibernate.envers.Audited;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.OffsetDateTime;


@Audited
@Entity
@Table(name = "tb_role")
@EntityListeners(AuditingEntityListener.class)
public class Role {

    @Id
    @Column(name = "id_role", nullable = false, updatable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, unique = true)
    private String name;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false, columnDefinition = "datetime2")
    private OffsetDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false, columnDefinition = "datetime2")
    private OffsetDateTime updatedAt;

    public Integer getId() {
        return id;
    }

    @SuppressWarnings("unused")
    public void setId(final Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    @SuppressWarnings("unused")
    public void setName(final String name) {
        this.name = name;
    }

    @SuppressWarnings("unused")
    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    @SuppressWarnings("unused")
    public void setCreatedAt(OffsetDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @SuppressWarnings("unused")
    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }

    @SuppressWarnings("unused")
    public void setUpdatedAt(OffsetDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

}
