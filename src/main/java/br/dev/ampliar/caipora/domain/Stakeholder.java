package br.dev.ampliar.caipora.domain;

import jakarta.persistence.*;
import org.hibernate.envers.Audited;


@Audited
@Entity
@Table(name = "tb_stakeholder")
public class Stakeholder extends Person {

    @Id
    @Column(name = "id_stakeholder", nullable = false, updatable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    public Integer getId() {
        return id;
    }

    @SuppressWarnings("unused")
    public void setId(final Integer id) {
        this.id = id;
    }

}
