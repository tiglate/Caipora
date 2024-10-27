package br.dev.ampliar.caipora.service;

import br.dev.ampliar.caipora.domain.Department;
import br.dev.ampliar.caipora.domain.Stakeholder;
import br.dev.ampliar.caipora.domain.User;
import br.dev.ampliar.caipora.model.DepartmentDTO;
import br.dev.ampliar.caipora.repos.DepartmentRepository;
import br.dev.ampliar.caipora.repos.StakeholderRepository;
import br.dev.ampliar.caipora.repos.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class DepartmentServiceImplTest {

    @Mock
    private DepartmentRepository departmentRepository;

    @Mock
    private StakeholderRepository stakeholderRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private DepartmentServiceImpl departmentService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testFindAll() {
        var filter = new DepartmentDTO();
        var pageable = PageRequest.of(0, 10);
        var department = new DepartmentDTO();
        department.setName("IT Department");
        var pageDTO = new PageImpl<>(Collections.singletonList(department));

        when(departmentRepository.findAllBySearchCriteria(any(), any(), any())).thenReturn(pageDTO);

        var result = departmentService.findAll(filter, pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(departmentRepository, times(1)).findAllBySearchCriteria(any(), any(), any());
    }

    @Test
    void testGet() {
        var department = new Department();
        department.setId(1);
        department.setName("IT Department");

        when(departmentRepository.findById(1)).thenReturn(Optional.of(department));

        var result = departmentService.get(1);

        assertNotNull(result);
        assertEquals("IT Department", result.getName());
        verify(departmentRepository, times(1)).findById(1);
    }

    @Test
    void testCreate() {
        var departmentDTO = new DepartmentDTO();
        departmentDTO.setName("IT Department");
        var department = new Department();
        department.setId(1);

        when(departmentRepository.save(any(Department.class))).thenReturn(department);

        var id = departmentService.create(departmentDTO);

        assertNotNull(id);
        assertEquals(1, id);
        verify(departmentRepository, times(1)).save(any(Department.class));
    }

    @Test
    void testUpdate() {
        var departmentDTO = new DepartmentDTO();
        departmentDTO.setName("IT Department");
        var department = new Department();
        department.setId(1);

        when(departmentRepository.findById(1)).thenReturn(Optional.of(department));
        when(departmentRepository.save(any(Department.class))).thenReturn(department);

        departmentService.update(1, departmentDTO);

        verify(departmentRepository, times(1)).findById(1);
        verify(departmentRepository, times(1)).save(any(Department.class));
    }

    @Test
    void testDelete() {
        departmentService.delete(1);

        verify(departmentRepository, times(1)).deleteById(1);
    }

    @Test
    void testNameExists() {
        when(departmentRepository.existsByNameIgnoreCase("IT Department")).thenReturn(true);

        var exists = departmentService.nameExists("IT Department");

        assertTrue(exists);
        verify(departmentRepository, times(1)).existsByNameIgnoreCase("IT Department");
    }

    @Test
    void testGetReferencedWarning_ReferencedByStakeholder() {
        var department = new Department();
        department.setId(1);
        var stakeholder = new Stakeholder();
        stakeholder.setId(1);

        when(departmentRepository.findById(1)).thenReturn(Optional.of(department));
        when(stakeholderRepository.findFirstByDepartment(department)).thenReturn(stakeholder);

        var warning = departmentService.getReferencedWarning(1);

        assertNotNull(warning);
        assertEquals("department.person.department.referenced", warning.getKey());
    }

    @Test
    void testGetReferencedWarning_ReferencedByUser() {
        var department = new Department();
        department.setId(1);
        var user = new User();
        user.setId(1);

        when(departmentRepository.findById(1)).thenReturn(Optional.of(department));
        when(stakeholderRepository.findFirstByDepartment(department)).thenReturn(null);
        when(userRepository.findFirstByDepartment(department)).thenReturn(user);

        var warning = departmentService.getReferencedWarning(1);

        assertNotNull(warning);
        assertEquals("department.person.department.referenced", warning.getKey());
    }
}