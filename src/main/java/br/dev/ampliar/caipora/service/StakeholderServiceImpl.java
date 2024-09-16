package br.dev.ampliar.caipora.service;

import br.dev.ampliar.caipora.domain.Department;
import br.dev.ampliar.caipora.domain.Deploy;
import br.dev.ampliar.caipora.domain.Software;
import br.dev.ampliar.caipora.domain.Stakeholder;
import br.dev.ampliar.caipora.model.StakeholderDTO;
import br.dev.ampliar.caipora.model.StakeholderSearchDTO;
import br.dev.ampliar.caipora.repos.DepartmentRepository;
import br.dev.ampliar.caipora.repos.DeployRepository;
import br.dev.ampliar.caipora.repos.SoftwareRepository;
import br.dev.ampliar.caipora.repos.StakeholderRepository;
import br.dev.ampliar.caipora.util.NotFoundException;
import br.dev.ampliar.caipora.util.ReferencedWarning;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;


@Service
public class StakeholderServiceImpl implements StakeholderService {

    private final StakeholderRepository stakeholderRepository;
    private final DepartmentRepository departmentRepository;
    private final SoftwareRepository softwareRepository;
    private final DeployRepository deployRepository;

    public StakeholderServiceImpl(final StakeholderRepository stakeholderRepository,
                                  final DepartmentRepository departmentRepository,
                                  final SoftwareRepository softwareRepository, final DeployRepository deployRepository) {
        this.stakeholderRepository = stakeholderRepository;
        this.departmentRepository = departmentRepository;
        this.softwareRepository = softwareRepository;
        this.deployRepository = deployRepository;
    }

    @Override
    public Page<StakeholderDTO> findAll(StakeholderSearchDTO searchDTO, Pageable pageable) {
        return stakeholderRepository.findAllBySearchCriteria(
                searchDTO.getName(),
                searchDTO.getDepartment(),
                searchDTO.getGender(),
                pageable
        );
    }

    @Override
    public StakeholderDTO get(final Integer id) {
        return stakeholderRepository.findById(id)
                .map(stakeholder -> mapToDTO(stakeholder, new StakeholderDTO()))
                .orElseThrow(NotFoundException::new);
    }

    @Override
    public Integer create(final StakeholderDTO stakeholderDTO) {
        final Stakeholder stakeholder = mapToEntity(stakeholderDTO);
        return stakeholderRepository.save(stakeholder).getId();
    }

    @Override
    public void update(final Integer id, final StakeholderDTO stakeholderDTO) {
        final Stakeholder stakeholder = stakeholderRepository.findById(id)
                .orElseThrow(NotFoundException::new);
        mapToEntity(stakeholderDTO, stakeholder);
        stakeholderRepository.save(stakeholder);
    }

    @Override
    public void delete(final Integer id) {
        stakeholderRepository.deleteById(id);
    }

    private StakeholderDTO mapToDTO(final Stakeholder stakeholder,
                                    final StakeholderDTO stakeholderDTO) {
        stakeholderDTO.setId(stakeholder.getId());
        stakeholderDTO.setName(stakeholder.getName());
        stakeholderDTO.setEmail(stakeholder.getEmail());
        stakeholderDTO.setGender(stakeholder.getGender());
        stakeholderDTO.setDepartmentId(stakeholder.getDepartment() == null ? null : stakeholder.getDepartment().getId());
        stakeholderDTO.setCreatedAt(stakeholder.getCreatedAt());
        stakeholderDTO.setUpdatedAt(stakeholder.getUpdatedAt());
        return stakeholderDTO;
    }

    private Stakeholder mapToEntity(final StakeholderDTO stakeholderDTO) {
        return mapToEntity(stakeholderDTO, new Stakeholder());
    }

    private Stakeholder mapToEntity(final StakeholderDTO stakeholderDTO,
                                    final Stakeholder stakeholder) {
        stakeholder.setName(stakeholderDTO.getName());
        stakeholder.setEmail(stakeholderDTO.getEmail());
        stakeholder.setGender(stakeholderDTO.getGender());
        final Department department = stakeholderDTO.getDepartmentId() == null ? null : departmentRepository.findById(stakeholderDTO.getDepartmentId())
                .orElseThrow(() -> new NotFoundException("department not found"));
        stakeholder.setDepartment(department);
        return stakeholder;
    }

    @Override
    public ReferencedWarning getReferencedWarning(final Integer id) {
        final ReferencedWarning referencedWarning = new ReferencedWarning();
        final Stakeholder stakeholder = stakeholderRepository.findById(id)
                .orElseThrow(NotFoundException::new);
        final Software ownerSoftware = softwareRepository.findFirstByOwner(stakeholder);
        if (ownerSoftware != null) {
            referencedWarning.setKey("stakeholder.software.owner.referenced");
            referencedWarning.addParam(ownerSoftware.getId());
            return referencedWarning;
        }
        final Deploy authorizerDeploy = deployRepository.findFirstByAuthorizer(stakeholder);
        if (authorizerDeploy != null) {
            referencedWarning.setKey("stakeholder.deploy.authorizer.referenced");
            referencedWarning.addParam(authorizerDeploy.getId());
            return referencedWarning;
        }
        return null;
    }

}
