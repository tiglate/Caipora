package br.dev.ampliar.caipora.repos;

import br.dev.ampliar.caipora.domain.Software;
import br.dev.ampliar.caipora.domain.Stakeholder;
import br.dev.ampliar.caipora.model.SoftwareDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;


public interface SoftwareRepository extends JpaRepository<Software, Integer> {

    @Query("SELECT new br.dev.ampliar.caipora.model.SoftwareDTO(d.id, d.code, d.name, o.id, o.name, d.createdAt, d.updatedAt) " +
            "FROM Software d " +
            "LEFT JOIN d.owner o " +
            "WHERE (:code IS NULL OR LOWER(d.code) LIKE CONCAT('%', LOWER(CAST(:code AS String)), '%')) " +
            "AND (:name IS NULL OR LOWER(d.name) LIKE CONCAT('%', LOWER(CAST(:name AS String)), '%')) " +
            "AND (:owner IS NULL OR o.id = :owner)")
    Page<SoftwareDTO> findAllBySearchCriteria(
            @Param("code") String code,
            @Param("name") String name,
            @Param("owner") Integer owner,
            Pageable pageable
    );

    Optional<Software> findByCodeIgnoreCase(String code);

    /**
     * Locks the application row until the end of the transaction: serializes the start of deploys of an
     * application across every instance, so two deploys never run at the same time for one environment.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT s FROM Software s WHERE s.id = :id")
    Optional<Software> findByIdForUpdate(@Param("id") Integer id);

    Software findFirstByOwner(Stakeholder stakeholder);

    boolean existsByCodeIgnoreCase(String code);

    boolean existsByNameIgnoreCase(String name);

}
