package br.dev.ampliar.caipora.repos;

import br.dev.ampliar.caipora.domain.Department;
import br.dev.ampliar.caipora.domain.User;
import br.dev.ampliar.caipora.model.UserDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;


public interface UserRepository extends JpaRepository<User, Integer> {

    @EntityGraph(attributePaths = "roles")
    User findByNameIgnoreCase(String name);

    User findFirstByDepartment(Department department);

    boolean existsByUsernameIgnoreCase(String username);

    @Query("SELECT new br.dev.ampliar.caipora.model.UserDTO(u.id, u.name, u.email, u.gender, u.username, u.password, u.enabled, d.id, d.name) " +
            "FROM User u " +
            "LEFT JOIN u.department d " +
            "WHERE (:username IS NULL OR u.username LIKE %:username%) " +
            "AND (:name IS NULL OR u.name LIKE %:name%) " +
            "AND (:department IS NULL OR d.id = :department) " +
            "AND (:enabled IS NULL OR u.enabled = :enabled) ")
    Page<UserDTO> findAllBySearchCriteria(
            @Param("username") String username,
            @Param("name") String name,
            @Param("department") Integer department,
            @Param("enabled") Boolean enabled,
            Pageable pageable
    );
}
