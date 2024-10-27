package br.dev.ampliar.caipora.repos;

import br.dev.ampliar.caipora.domain.Software;
import br.dev.ampliar.caipora.domain.Stakeholder;
import br.dev.ampliar.caipora.model.SoftwareDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;


public interface SoftwareRepository extends JpaRepository<Software, Integer> {

    @Query("SELECT new br.dev.ampliar.caipora.model.SoftwareDTO(d.id, d.code, d.name, o.id, o.name, d.createdAt, d.updatedAt) " +
            "FROM Software d " +
            "LEFT JOIN d.owner o " +
            "WHERE (:code IS NULL OR d.code LIKE %:code%) " +
            "AND (:name IS NULL OR d.name LIKE %:name%) " +
            "AND (:owner IS NULL OR o.id = :owner)")
    Page<SoftwareDTO> findAllBySearchCriteria(
            @Param("code") String code,
            @Param("name") String name,
            @Param("owner") Integer owner,
            Pageable pageable
    );

    Optional<Software> findByCodeIgnoreCase(String code);

    Software findFirstByOwner(Stakeholder stakeholder);

    boolean existsByCodeIgnoreCase(String code);

    boolean existsByNameIgnoreCase(String name);

}
