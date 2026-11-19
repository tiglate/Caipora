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

import java.util.Collection;
import java.util.List;


public interface UserRepository extends JpaRepository<User, Integer> {

    @EntityGraph(attributePaths = "roles")
    User findByUsernameIgnoreCase(String username);

    User findFirstByDepartment(Department department);

    boolean existsByUsernameIgnoreCase(String username);

    /**
     * @return whether an enabled account holding the given role exists
     */
    boolean existsByEnabledTrueAndRolesName(String roleName);

    /**
     * @return pairs of (user id, role id) for the given users
     */
    @Query("SELECT u.id, r.id FROM User u JOIN u.roles r WHERE u.id IN :ids")
    List<Object[]> findRoleIdsByUserIds(@Param("ids") Collection<Integer> ids);

    @Query("SELECT new br.dev.ampliar.caipora.model.UserDTO(u.id, u.name, u.email, u.gender, u.username, u.enabled, d.id, d.name, u.createdAt, u.updatedAt) " +
            "FROM User u " +
            "LEFT JOIN u.department d " +
            "WHERE (:username IS NULL OR LOWER(u.username) LIKE CONCAT('%', LOWER(CAST(:username AS String)), '%')) " +
            "AND (:name IS NULL OR LOWER(u.name) LIKE CONCAT('%', LOWER(CAST(:name AS String)), '%')) " +
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
