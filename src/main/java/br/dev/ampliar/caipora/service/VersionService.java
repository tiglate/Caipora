package br.dev.ampliar.caipora.service;

import br.dev.ampliar.caipora.model.VersionDTO;
import br.dev.ampliar.caipora.util.ReferencedWarning;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.nio.file.Path;
import java.util.List;


public interface VersionService {

    Page<VersionDTO> findAll(VersionDTO searchDTO, Pageable pageable);

    VersionDTO get(Integer id);

    /**
     * @throws IllegalArgumentException when the package is not a valid, unregistered file of the application folder
     */
    @SuppressWarnings("UnusedReturnValue")
    Integer create(VersionDTO versionDTO);

    void update(Integer id, VersionDTO versionDTO);

    /**
     * Deletes the version record. The package stays in the packages directory, which is managed outside Caipora.
     */
    void delete(Integer id);

    /**
     * @return the packages of the application folder that are not registered as a version yet, most recent first
     */
    List<PackageStorage.PackageFile> getAvailablePackages(Integer softwareId);

    /**
     * @return the packages folder of the application (it may not exist yet), or null when its code can't name one
     */
    Path getPackagesFolder(Integer softwareId);

    /**
     * @return the package file of the given version, already validated to be inside the application folder
     */
    Path getPackageFile(Integer id);

    ReferencedWarning getReferencedWarning(Integer id);

}
