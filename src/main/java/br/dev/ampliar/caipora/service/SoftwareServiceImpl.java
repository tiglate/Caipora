package br.dev.ampliar.caipora.service;

import br.dev.ampliar.caipora.domain.Software;
import br.dev.ampliar.caipora.domain.Stakeholder;
import br.dev.ampliar.caipora.domain.Version;
import br.dev.ampliar.caipora.model.SoftwareDTO;
import br.dev.ampliar.caipora.repos.SoftwareRepository;
import br.dev.ampliar.caipora.repos.StakeholderRepository;
import br.dev.ampliar.caipora.repos.VersionRepository;
import br.dev.ampliar.caipora.util.NotFoundException;
import br.dev.ampliar.caipora.util.ReferencedWarning;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@Transactional(readOnly = true)
public class SoftwareServiceImpl implements SoftwareService {

    private static final java.util.regex.Pattern CODE_PATTERN = java.util.regex.Pattern.compile("[A-Za-z0-9][A-Za-z0-9._-]*");

    private final SoftwareRepository softwareRepository;
    private final StakeholderRepository stakeholderRepository;
    private final VersionRepository versionRepository;
    private final PackageStorage packageStorage;

    public SoftwareServiceImpl(final SoftwareRepository softwareRepository,
                               final StakeholderRepository stakeholderRepository,
                               final VersionRepository versionRepository,
                               final PackageStorage packageStorage) {
        this.softwareRepository = softwareRepository;
        this.stakeholderRepository = stakeholderRepository;
        this.versionRepository = versionRepository;
        this.packageStorage = packageStorage;
    }

    @Override
    public Page<SoftwareDTO> findAll(SoftwareDTO searchDTO, Pageable pageable) {
        return softwareRepository.findAllBySearchCriteria(
                searchDTO.getCode(),
                searchDTO.getName(),
                searchDTO.getOwnerId(),
                pageable
        );
    }

    @Override
    public SoftwareDTO get(final Integer id) {
        return softwareRepository.findById(id)
                .map(software -> mapToDTO(software, new SoftwareDTO()))
                .orElseThrow(NotFoundException::new);
    }

    @Override
    @Transactional
    public Integer create(final SoftwareDTO softwareDTO, final boolean canEditScript) {
        checkCode(softwareDTO.getCode());
        final Software software = mapToEntity(softwareDTO);
        software.setDeployScript(canEditScript ? normalizeScript(softwareDTO.getDeployScript()) : null);
        final Integer id = softwareRepository.save(software).getId();
        packageStorage.createApplicationFolder(software.getCode());
        return id;
    }

    @Override
    @Transactional
    public void update(final Integer id, final SoftwareDTO softwareDTO, final boolean canEditScript) {
        final Software software = softwareRepository.findById(id)
                .orElseThrow(NotFoundException::new);
        if (softwareDTO.getCode() != null && !softwareDTO.getCode().equals(software.getCode())
                && versionRepository.findFirstBySoftware(software) != null) {
            throw new IllegalArgumentException(
                    "The code cannot be changed while the application has versions: it names the packages folder.");
        }
        if (softwareDTO.getCode() != null && !softwareDTO.getCode().equals(software.getCode())) {
            checkCode(softwareDTO.getCode());
        }
        final String script = canEditScript ? normalizeScript(softwareDTO.getDeployScript()) : software.getDeployScript();
        mapToEntity(softwareDTO, software);
        software.setDeployScript(script);
        softwareRepository.save(software);
        packageStorage.createApplicationFolder(software.getCode());
    }

    @Override
    public String getPackagesFolder(final Integer id) {
        final Software software = softwareRepository.findById(id)
                .orElseThrow(NotFoundException::new);
        try {
            return packageStorage.applicationFolder(software.getCode()).toString();
        } catch (IllegalArgumentException e) {
            return null; // code recorded before codes were restricted: it can't name a folder
        }
    }

    /**
     * The code names the packages folder of the application. Checked only for new or changed codes, so
     * applications recorded before the restriction can still be edited.
     */
    static void checkCode(final String code) {
        if (code != null && !CODE_PATTERN.matcher(code).matches()) {
            throw new IllegalArgumentException(
                    "Use only letters, digits, dots, hyphens and underscores (the code names the packages folder).");
        }
    }

    /**
     * Scripts are stored as typed (no HTML sanitizing: it would break {@code <}, {@code >} and {@code &&}), only
     * with Unix line endings (browsers submit CRLF, which breaks bash) and blank meaning "no script".
     */
    static String normalizeScript(final String script) {
        if (script == null || script.isBlank()) {
            return null;
        }
        return script.replace("\r\n", "\n").replace('\r', '\n');
    }

    @Override
    @Transactional
    public void delete(final Integer id) {
        softwareRepository.deleteById(id);
    }

    private SoftwareDTO mapToDTO(final Software software, final SoftwareDTO softwareDTO) {
        softwareDTO.setId(software.getId());
        softwareDTO.setCode(software.getCode());
        softwareDTO.setName(software.getName());
        softwareDTO.setOwnerId(software.getOwner() == null ? null : software.getOwner().getId());
        softwareDTO.setOwnerName(software.getOwner() == null ? null : software.getOwner().getName());
        softwareDTO.setDeployScript(software.getDeployScript());
        softwareDTO.setCreatedAt(software.getCreatedAt());
        softwareDTO.setUpdatedAt(software.getUpdatedAt());
        return softwareDTO;
    }

    private Software mapToEntity(final SoftwareDTO softwareDTO) {
        return mapToEntity(softwareDTO, new Software());
    }

    private Software mapToEntity(final SoftwareDTO softwareDTO, final Software software) {
        software.setCode(softwareDTO.getCode());
        software.setName(softwareDTO.getName());
        final Stakeholder owner = softwareDTO.getOwnerId() == null ? null : stakeholderRepository.findById(softwareDTO.getOwnerId())
                .orElseThrow(() -> new NotFoundException("owner not found"));
        software.setOwner(owner);
        return software;
    }

    @Override
    public boolean codeExists(final String code) {
        return softwareRepository.existsByCodeIgnoreCase(code);
    }

    @Override
    public boolean nameExists(final String name) {
        return softwareRepository.existsByNameIgnoreCase(name);
    }

    @Override
    public ReferencedWarning getReferencedWarning(final Integer id) {
        final ReferencedWarning referencedWarning = new ReferencedWarning();
        final Software software = softwareRepository.findById(id)
                .orElseThrow(NotFoundException::new);
        final Version softwareVersion = versionRepository.findFirstBySoftware(software);
        if (softwareVersion != null) {
            referencedWarning.setKey("software.version.software.referenced");
            referencedWarning.addParam(softwareVersion.getId());
            return referencedWarning;
        }
        return null;
    }

}
