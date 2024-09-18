package br.dev.ampliar.caipora.service;

import br.dev.ampliar.caipora.domain.Department;
import br.dev.ampliar.caipora.domain.Stakeholder;
import br.dev.ampliar.caipora.domain.User;
import br.dev.ampliar.caipora.model.DepartmentDTO;
import br.dev.ampliar.caipora.repos.DepartmentRepository;
import br.dev.ampliar.caipora.repos.StakeholderRepository;
import br.dev.ampliar.caipora.repos.UserRepository;
import br.dev.ampliar.caipora.util.NotFoundException;
import br.dev.ampliar.caipora.util.ReferencedWarning;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;


@Service
public class DepartmentServiceImpl implements DepartmentService {

    private final DepartmentRepository departmentRepository;
    private final StakeholderRepository stakeholderRepository;
    private final UserRepository userRepository;

    public DepartmentServiceImpl(final DepartmentRepository departmentRepository,
                                 final StakeholderRepository stakeholderRepository,
                                 final UserRepository userRepository) {
        this.departmentRepository = departmentRepository;
        this.stakeholderRepository = stakeholderRepository;
        this.userRepository = userRepository;
    }

    @Override
    public Page<DepartmentDTO> findAll(DepartmentDTO searchDTO, Pageable pageable) {
        return departmentRepository.findAllBySearchCriteria(
                searchDTO.getName(),
                searchDTO.getEmail(),
                pageable
        );
    }

    @Override
    public DepartmentDTO get(final Integer id) {
        return departmentRepository.findById(id)
                .map(department -> mapToDTO(department, new DepartmentDTO()))
                .orElseThrow(NotFoundException::new);
    }

    @Override
    public Integer create(final DepartmentDTO departmentDTO) {
        var department = mapToEntity(departmentDTO);
        return departmentRepository.save(department).getId();
    }

    @Override
    public void update(final Integer id, final DepartmentDTO departmentDTO) {
        final Department department = departmentRepository.findById(id)
                .orElseThrow(NotFoundException::new);
        mapToEntity(departmentDTO, department);
        departmentRepository.save(department);
    }

    @Override
    public void delete(final Integer id) {
        departmentRepository.deleteById(id);
    }

    private DepartmentDTO mapToDTO(final Department department, final DepartmentDTO departmentDTO) {
        departmentDTO.setId(department.getId());
        departmentDTO.setName(department.getName());
        departmentDTO.setEmail(department.getEmail());
        departmentDTO.setCreatedAt(department.getCreatedAt());
        departmentDTO.setUpdatedAt(department.getUpdatedAt());
        return departmentDTO;
    }

    private Department mapToEntity(final DepartmentDTO departmentDTO) {
        return mapToEntity(departmentDTO, new Department());
    }

    private Department mapToEntity(final DepartmentDTO departmentDTO, final Department department) {
        department.setName(departmentDTO.getName());
        department.setEmail(departmentDTO.getEmail());
        return department;
    }

    @Override
    public boolean nameExists(final String name) {
        return departmentRepository.existsByNameIgnoreCase(name);
    }

    @Override
    public ReferencedWarning getReferencedWarning(final Integer id) {
        final ReferencedWarning referencedWarning = new ReferencedWarning();
        final Department department = departmentRepository.findById(id)
                .orElseThrow(NotFoundException::new);
        final Stakeholder departmentStakeholder = stakeholderRepository.findFirstByDepartment(department);
        if (departmentStakeholder != null) {
            referencedWarning.setKey("department.person.department.referenced");
            referencedWarning.addParam(departmentStakeholder.getId());
            return referencedWarning;
        }
        final User departmentUser = userRepository.findFirstByDepartment(department);
        if (departmentUser != null) {
            referencedWarning.setKey("department.person.department.referenced");
            referencedWarning.addParam(departmentUser.getId());
            return referencedWarning;
        }
        return null;
    }

}
