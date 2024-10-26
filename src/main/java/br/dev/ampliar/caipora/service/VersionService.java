package br.dev.ampliar.caipora.service;

import br.dev.ampliar.caipora.model.VersionDTO;
import br.dev.ampliar.caipora.util.ReferencedWarning;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.io.IOException;


public interface VersionService {

    Page<VersionDTO> findAll(VersionDTO searchDTO, Pageable pageable);

    VersionDTO get(Integer id);

    @SuppressWarnings("UnusedReturnValue")
    Integer create(VersionDTO versionDTO) throws IOException;

    void update(Integer id, VersionDTO versionDTO);

    void delete(Integer id) throws IOException;

    ReferencedWarning getReferencedWarning(Integer id);

}
