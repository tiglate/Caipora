package br.dev.ampliar.caipora.repos;

import br.dev.ampliar.caipora.domain.Department;
import br.dev.ampliar.caipora.model.DepartmentDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;


public interface DepartmentRepository extends JpaRepository<Department, Integer> {

    @Query("SELECT new br.dev.ampliar.caipora.model.DepartmentDTO(d.id, d.name, d.email, d.createdAt, d.updatedAt) " +
            "FROM Department d " +
            "WHERE (:name IS NULL OR d.name LIKE %:name%) " +
            "AND (:email IS NULL OR d.email LIKE %:email%)")
    Page<DepartmentDTO> findAllBySearchCriteria(
            @Param("name") String name,
            @Param("email") String email,
            Pageable pageable
    );

    boolean existsByNameIgnoreCase(String name);

    Optional<Department> findByNameIgnoreCase(String name);
}
