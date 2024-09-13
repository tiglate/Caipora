package br.dev.ampliar.caipora.repos;

import br.dev.ampliar.caipora.domain.Deploy;
import br.dev.ampliar.caipora.domain.Stakeholder;
import br.dev.ampliar.caipora.domain.User;
import br.dev.ampliar.caipora.domain.Version;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;


public interface DeployRepository extends JpaRepository<Deploy, Integer> {

    Page<Deploy> findAllById(Integer id, Pageable pageable);

    Deploy findFirstByVersion(Version version);

    Deploy findFirstByOperator(User user);

    Deploy findFirstByAuthorizer(Stakeholder stakeholder);

    boolean existsByRfcIgnoreCase(String rfc);

}
