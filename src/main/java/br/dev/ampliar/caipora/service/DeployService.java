package br.dev.ampliar.caipora.service;

import br.dev.ampliar.caipora.model.DeployDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;


public interface DeployService {

    Page<DeployDTO> findAll(String filter, Pageable pageable);

    DeployDTO get(Integer id);

    Integer create(DeployDTO deployDTO);

    void update(Integer id, DeployDTO deployDTO);

    void delete(Integer id);

    boolean rfcExists(String rfc);

}
