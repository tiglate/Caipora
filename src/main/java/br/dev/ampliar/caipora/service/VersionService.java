package br.dev.ampliar.caipora.service;

import br.dev.ampliar.caipora.model.VersionDTO;
import br.dev.ampliar.caipora.util.ReferencedWarning;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.io.IOException;
import java.nio.file.Path;


public interface VersionService {

    Page<VersionDTO> findAll(VersionDTO searchDTO, Pageable pageable);

    VersionDTO get(Integer id);

    @SuppressWarnings("UnusedReturnValue")
    Integer create(VersionDTO versionDTO) throws IOException;

    void update(Integer id, VersionDTO versionDTO);

    void delete(Integer id) throws IOException;

    /**
     * @return the package file of the given version, already validated to be inside the upload directory
     */
    StoredFile getStoredFile(Integer id);

    ReferencedWarning getReferencedWarning(Integer id);

    /**
     * @param path         absolute path of the stored file
     * @param downloadName name suggested to the browser (the original upload name, without the internal UUID prefix)
     */
    record StoredFile(Path path, String downloadName) {
    }

}
