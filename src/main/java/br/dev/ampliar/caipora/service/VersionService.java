package br.dev.ampliar.caipora.service;

import br.dev.ampliar.caipora.model.VersionDTO;
import br.dev.ampliar.caipora.util.ReferencedWarning;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;


public interface VersionService {

    Page<VersionDTO> findAll(VersionDTO searchDTO, Pageable pageable);

    VersionDTO get(Integer id);

    @SuppressWarnings("UnusedReturnValue")
    Integer create(VersionDTO versionDTO);

    void update(Integer id, VersionDTO versionDTO);

    void delete(Integer id);

    ReferencedWarning getReferencedWarning(Integer id);

}
