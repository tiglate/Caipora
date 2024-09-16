package br.dev.ampliar.caipora.repos;

import br.dev.ampliar.caipora.domain.Software;
import br.dev.ampliar.caipora.domain.Version;
import br.dev.ampliar.caipora.model.VersionDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;


public interface VersionRepository extends JpaRepository<Version, Integer> {

    @Query("SELECT new br.dev.ampliar.caipora.model.VersionDTO(v.id, v.name, v.fileName, v.releaseDate, s.id, s.name) " +
            "FROM Version v " +
            "LEFT JOIN v.software s " +
            "WHERE (:name IS NULL OR v.name LIKE %:name%) " +
            "AND (:software IS NULL OR v.software.id = :software)")
    Page<VersionDTO> findAllBySearchCriteria(
            @Param("name") String name,
            @Param("software") Integer software,
            Pageable pageable
    );

    List<Version> findAllBySoftware(Software software, Sort sort);

    Version findFirstBySoftware(Software software);

}
