package br.dev.ampliar.caipora.service;

import br.dev.ampliar.caipora.model.StakeholderDTO;
import br.dev.ampliar.caipora.model.StakeholderSearchDTO;
import br.dev.ampliar.caipora.util.ReferencedWarning;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;


public interface StakeholderService {

    Page<StakeholderDTO> findAll(StakeholderSearchDTO filter, Pageable pageable);

    StakeholderDTO get(Integer id);

    @SuppressWarnings("UnusedReturnValue")
    Integer create(StakeholderDTO stakeholderDTO);

    void update(Integer id, StakeholderDTO stakeholderDTO);

    void delete(Integer id);

    ReferencedWarning getReferencedWarning(Integer id);

}
