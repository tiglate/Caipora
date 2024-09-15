package br.dev.ampliar.caipora.service;

import br.dev.ampliar.caipora.model.DeployDTO;


public interface DeployService {

    DeployDTO get(Integer id);

    Integer create(DeployDTO deployDTO);

    boolean rfcExists(String rfc);

}
