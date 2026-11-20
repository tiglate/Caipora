package br.dev.ampliar.caipora.service;

import br.dev.ampliar.caipora.model.SoftwareDTO;
import br.dev.ampliar.caipora.util.ReferencedWarning;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;


public interface SoftwareService {

    Page<SoftwareDTO> findAll(SoftwareDTO searchDTO, Pageable pageable);

    SoftwareDTO get(Integer id);

    /**
     * @param canEditScript whether the current user may set the deploy script (otherwise it is left empty)
     * @throws IllegalArgumentException when the code can't name a packages folder
     */
    @SuppressWarnings("UnusedReturnValue")
    Integer create(SoftwareDTO softwareDTO, boolean canEditScript);

    /**
     * @param canEditScript whether the current user may change the deploy script (otherwise it is kept)
     * @throws IllegalArgumentException when the code changes while the application has versions, or the new code
     *                                  can't name a packages folder
     */
    void update(Integer id, SoftwareDTO softwareDTO, boolean canEditScript);

    /**
     * @return the packages folder of the application (it may not exist yet), or null when the code can't name one
     */
    String getPackagesFolder(Integer id);

    void delete(Integer id);

    boolean codeExists(String code);

    boolean nameExists(String name);

    ReferencedWarning getReferencedWarning(Integer id);

}
