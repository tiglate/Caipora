package br.dev.ampliar.caipora.repos;

import br.dev.ampliar.caipora.domain.Department;
import br.dev.ampliar.caipora.domain.Stakeholder;
import br.dev.ampliar.caipora.model.Gender;
import br.dev.ampliar.caipora.model.StakeholderDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;


public interface StakeholderRepository extends JpaRepository<Stakeholder, Integer> {

    @Query("SELECT new br.dev.ampliar.caipora.model.StakeholderDTO(s.id, s.name, s.email, s.gender, d.id, d.name, s.createdAt, s.updatedAt) " +
            "FROM Stakeholder s " +
            "LEFT JOIN s.department d " +
            "WHERE (:name IS NULL OR s.name LIKE %:name%) " +
            "AND (:department IS NULL OR d.id = :department) " +
            "AND (:gender IS NULL OR s.gender = :gender)")
    Page<StakeholderDTO> findAllBySearchCriteria(
            @Param("name") String name,
            @Param("department") Integer department,
            @Param("gender") Gender gender,
            Pageable pageable
    );

    Stakeholder findFirstByDepartment(Department department);

}
