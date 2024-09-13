package br.dev.ampliar.caipora.repos;

import br.dev.ampliar.caipora.domain.Role;
import org.springframework.data.jpa.repository.JpaRepository;


public interface RoleRepository extends JpaRepository<Role, Integer> {

    Role findByName(String name);

}
