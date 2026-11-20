package br.dev.ampliar.caipora.repos;

import br.dev.ampliar.caipora.domain.Deploy;
import br.dev.ampliar.caipora.domain.Software;
import br.dev.ampliar.caipora.domain.Stakeholder;
import br.dev.ampliar.caipora.domain.User;
import br.dev.ampliar.caipora.domain.Version;
import br.dev.ampliar.caipora.model.DeployStatus;
import br.dev.ampliar.caipora.model.Environment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;


public interface DeployRepository extends JpaRepository<Deploy, Integer> {

    Deploy findFirstByVersion(Version version);

    Deploy findFirstByOperator(User user);

    Deploy findFirstByAuthorizer(Stakeholder stakeholder);

    boolean existsByRfcIgnoreCase(String rfc);

    boolean existsByVersionSoftwareAndEnvironmentAndStatusIn(Software software, Environment environment,
                                                             Collection<DeployStatus> statuses);

    List<Deploy> findAllByVersionSoftwareAndStatusIn(Software software, Collection<DeployStatus> statuses);

    List<Deploy> findAllByStatusIn(Collection<DeployStatus> statuses);

    List<Deploy> findAllByVersionSoftwareAndEnvironmentAndIsActiveTrue(Software software, Environment environment);

    @EntityGraph(attributePaths = {"version"})
    List<Deploy> findAllByVersionSoftwareAndEnvironmentAndStatusOrderByIdDesc(Software software,
                                                                             Environment environment,
                                                                             DeployStatus status);

    @EntityGraph(attributePaths = {"version", "operator", "authorizer"})
    Page<Deploy> findAllByVersionSoftware(Software software, Pageable pageable);

    @Query("SELECT d.status FROM Deploy d WHERE d.id = :id")
    DeployStatus findStatusById(@Param("id") Integer id);

    /**
     * Stores the output captured so far. A bulk update on purpose: it bypasses auditing (no revision, no
     * updated_at change) since it runs every second while a script is executing.
     */
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("UPDATE Deploy d SET d.scriptOutput = :output WHERE d.id = :id")
    void updateScriptOutput(@Param("id") Integer id, @Param("output") String output);

}
