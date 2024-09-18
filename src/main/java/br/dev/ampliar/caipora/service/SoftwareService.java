package br.dev.ampliar.caipora.service;

import br.dev.ampliar.caipora.model.SoftwareDTO;
import br.dev.ampliar.caipora.util.ReferencedWarning;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;


public interface SoftwareService {

    Page<SoftwareDTO> findAll(SoftwareDTO searchDTO, Pageable pageable);

    SoftwareDTO get(Integer id);

    @SuppressWarnings("UnusedReturnValue")
    Integer create(SoftwareDTO softwareDTO);

    void update(Integer id, SoftwareDTO softwareDTO);

    void delete(Integer id);

    boolean codeExists(String code);

    boolean nameExists(String name);

    ReferencedWarning getReferencedWarning(Integer id);

}
