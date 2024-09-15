package br.dev.ampliar.caipora.repos;

import br.dev.ampliar.caipora.domain.LastVersionDeployed;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LastVersionDeployedRepository extends JpaRepository<LastVersionDeployed, Integer> {

}