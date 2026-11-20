package br.dev.ampliar.caipora.service;

import br.dev.ampliar.caipora.domain.Deploy;
import br.dev.ampliar.caipora.domain.Software;
import br.dev.ampliar.caipora.domain.Version;
import br.dev.ampliar.caipora.model.VersionDTO;
import br.dev.ampliar.caipora.repos.DeployRepository;
import br.dev.ampliar.caipora.repos.SoftwareRepository;
import br.dev.ampliar.caipora.repos.VersionRepository;
import br.dev.ampliar.caipora.util.HtmlSanitizer;
import br.dev.ampliar.caipora.util.NotFoundException;
import br.dev.ampliar.caipora.util.ReferencedWarning;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.file.Path;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;


@Service
@Transactional(readOnly = true)
public class VersionServiceImpl implements VersionService {

    private final VersionRepository versionRepository;
    private final SoftwareRepository softwareRepository;
    private final DeployRepository deployRepository;
    private final PackageStorage packageStorage;

    public VersionServiceImpl(final VersionRepository versionRepository,
                              final SoftwareRepository softwareRepository,
                              final DeployRepository deployRepository,
                              final PackageStorage packageStorage) {
        this.versionRepository = versionRepository;
        this.softwareRepository = softwareRepository;
        this.deployRepository = deployRepository;
        this.packageStorage = packageStorage;
    }

    @Override
    public Page<VersionDTO> findAll(VersionDTO searchDTO, Pageable pageable) {
        return versionRepository.findAllBySearchCriteria(
                searchDTO.getName(),
                searchDTO.getSoftwareId(),
                pageable
        );
    }

    @Override
    public VersionDTO get(final Integer id) {
        return versionRepository.findById(id)
                .map(version -> mapToDTO(version, new VersionDTO()))
                .orElseThrow(NotFoundException::new);
    }

    @Override
    @Transactional
    public Integer create(final VersionDTO versionDTO) {
        final Version version = mapToEntity(versionDTO, new Version());
        final Software software = version.getSoftware();
        if (software == null) {
            throw new IllegalArgumentException("Please select the application.");
        }
        // the form only offers files of the application folder, but the request could be forged
        try {
            packageStorage.resolvePackage(software.getCode(), versionDTO.getFileName());
        } catch (IllegalArgumentException | NotFoundException e) {
            throw new IllegalArgumentException("Please select a package of the application folder.", e);
        }
        if (versionRepository.existsBySoftwareAndFileNameIgnoreCase(software, versionDTO.getFileName())) {
            throw new IllegalArgumentException("This package is already registered as a version.");
        }
        version.setFileName(versionDTO.getFileName());
        return versionRepository.save(version).getId();
    }

    @Override
    @Transactional
    public void update(final Integer id, final VersionDTO versionDTO) {
        final Version version = versionRepository.findById(id)
                .orElseThrow(NotFoundException::new);
        // neither the package nor the application can be changed through an update
        final String fileName = version.getFileName();
        final Software software = version.getSoftware();
        mapToEntity(versionDTO, version);
        version.setFileName(fileName);
        version.setSoftware(software);
        versionRepository.save(version);
    }

    @Override
    @Transactional
    public void delete(final Integer id) {
        final Version version = versionRepository.findById(id)
                .orElseThrow(NotFoundException::new);
        versionRepository.delete(version);
    }

    @Override
    public List<PackageStorage.PackageFile> getAvailablePackages(final Integer softwareId) {
        if (softwareId == null) {
            return List.of();
        }
        final Software software = softwareRepository.findById(softwareId)
                .orElseThrow(() -> new NotFoundException("software not found"));
        final Set<String> registered = versionRepository.findFileNamesBySoftware(software).stream()
                .map(name -> name.toLowerCase(Locale.ROOT))
                .collect(Collectors.toSet());
        final List<PackageStorage.PackageFile> files;
        try {
            files = packageStorage.listPackages(software.getCode());
        } catch (IllegalArgumentException e) {
            return List.of(); // code that can't name a folder
        }
        return files.stream()
                .filter(file -> !registered.contains(file.name().toLowerCase(Locale.ROOT)))
                .toList();
    }

    @Override
    public Path getPackagesFolder(final Integer softwareId) {
        final Software software = softwareRepository.findById(softwareId)
                .orElseThrow(() -> new NotFoundException("software not found"));
        try {
            return packageStorage.applicationFolder(software.getCode());
        } catch (IllegalArgumentException e) {
            return null; // code that can't name a folder
        }
    }

    @Override
    public Path getPackageFile(final Integer id) {
        final Version version = versionRepository.findById(id)
                .orElseThrow(NotFoundException::new);
        return packageStorage.resolvePackage(version.getSoftware().getCode(), version.getFileName());
    }

    private VersionDTO mapToDTO(final Version version, final VersionDTO versionDTO) {
        versionDTO.setId(version.getId());
        versionDTO.setName(version.getName());
        versionDTO.setFileName(version.getFileName());
        versionDTO.setReleaseDate(version.getReleaseDate());
        // sanitize on read as well, so content stored before sanitization was introduced is also safe
        versionDTO.setChangelog(HtmlSanitizer.sanitize(version.getChangelog()));
        versionDTO.setComments(HtmlSanitizer.sanitize(version.getComments()));
        if (version.getSoftware() != null) {
            final Software software = version.getSoftware();
            versionDTO.setSoftwareId(software.getId());
            versionDTO.setSoftwareName(software.getName());
            packageStorage.findPackage(software.getCode(), version.getFileName()).ifPresent(file -> {
                versionDTO.setPackageAvailable(true);
                versionDTO.setFileSize(file.displaySize());
            });
        }
        versionDTO.setCreatedAt(version.getCreatedAt());
        versionDTO.setUpdatedAt(version.getUpdatedAt());
        return versionDTO;
    }

    private Version mapToEntity(final VersionDTO versionDTO, final Version version) {
        version.setName(versionDTO.getName());
        version.setReleaseDate(versionDTO.getReleaseDate());
        version.setChangelog(HtmlSanitizer.sanitize(versionDTO.getChangelog()));
        version.setComments(HtmlSanitizer.sanitize(versionDTO.getComments()));
        final Software software = versionDTO.getSoftwareId() == null ? null : softwareRepository.findById(versionDTO.getSoftwareId())
                .orElseThrow(() -> new NotFoundException("software not found"));
        version.setSoftware(software);
        return version;
    }

    @Override
    public ReferencedWarning getReferencedWarning(final Integer id) {
        final ReferencedWarning referencedWarning = new ReferencedWarning();
        final Version version = versionRepository.findById(id)
                .orElseThrow(NotFoundException::new);
        final Deploy versionDeploy = deployRepository.findFirstByVersion(version);
        if (versionDeploy != null) {
            referencedWarning.setKey("version.deploy.version.referenced");
            referencedWarning.addParam(versionDeploy.getId());
            return referencedWarning;
        }
        return null;
    }

}
