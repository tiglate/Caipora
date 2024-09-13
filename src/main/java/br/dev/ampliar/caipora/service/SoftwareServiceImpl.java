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
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;


@Service
public class SoftwareServiceImpl implements SoftwareService {

    private final SoftwareRepository softwareRepository;
    private final StakeholderRepository stakeholderRepository;
    private final VersionRepository versionRepository;

    public SoftwareServiceImpl(final SoftwareRepository softwareRepository,
                               final StakeholderRepository stakeholderRepository,
                               final VersionRepository versionRepository) {
        this.softwareRepository = softwareRepository;
        this.stakeholderRepository = stakeholderRepository;
        this.versionRepository = versionRepository;
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
    public Integer create(final SoftwareDTO softwareDTO) {
        final Software software = mapToEntity(softwareDTO);
        return softwareRepository.save(software).getId();
    }

    @Override
    public void update(final Integer id, final SoftwareDTO softwareDTO) {
        final Software software = softwareRepository.findById(id)
                .orElseThrow(NotFoundException::new);
        mapToEntity(softwareDTO, software);
        softwareRepository.save(software);
    }

    @Override
    public void delete(final Integer id) {
        softwareRepository.deleteById(id);
    }

    private SoftwareDTO mapToDTO(final Software software, final SoftwareDTO softwareDTO) {
        softwareDTO.setId(software.getId());
        softwareDTO.setCode(software.getCode());
        softwareDTO.setName(software.getName());
        softwareDTO.setOwnerId(software.getOwner() == null ? null : software.getOwner().getId());
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
