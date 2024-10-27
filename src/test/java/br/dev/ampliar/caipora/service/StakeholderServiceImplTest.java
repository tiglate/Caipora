package br.dev.ampliar.caipora.service;

import br.dev.ampliar.caipora.domain.Deploy;
import br.dev.ampliar.caipora.domain.Software;
import br.dev.ampliar.caipora.domain.Stakeholder;
import br.dev.ampliar.caipora.model.StakeholderDTO;
import br.dev.ampliar.caipora.model.StakeholderSearchDTO;
import br.dev.ampliar.caipora.repos.DepartmentRepository;
import br.dev.ampliar.caipora.repos.DeployRepository;
import br.dev.ampliar.caipora.repos.SoftwareRepository;
import br.dev.ampliar.caipora.repos.StakeholderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class StakeholderServiceImplTest {

    @Mock
    private StakeholderRepository stakeholderRepository;

    @Mock
    private DepartmentRepository departmentRepository;

    @Mock
    private SoftwareRepository softwareRepository;

    @Mock
    private DeployRepository deployRepository;

    @InjectMocks
    private StakeholderServiceImpl stakeholderService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testFindAll() {
        var filter = new StakeholderSearchDTO();
        var pageable = PageRequest.of(0, 10);
        var stakeholder = new StakeholderDTO();
        stakeholder.setName("Jane Doe");
        var page = new PageImpl<>(Collections.singletonList(stakeholder));

        when(stakeholderRepository.findAllBySearchCriteria(any(), any(), any(), any(), any())).thenReturn(page);

        Page<StakeholderDTO> result = stakeholderService.findAll(filter, pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(stakeholderRepository, times(1)).findAllBySearchCriteria(any(), any(), any(), any(), any());
    }

    @Test
    void testGet() {
        var stakeholder = new Stakeholder();
        stakeholder.setId(1);
        stakeholder.setName("Jane Doe");

        when(stakeholderRepository.findById(1)).thenReturn(Optional.of(stakeholder));

        var result = stakeholderService.get(1);

        assertNotNull(result);
        assertEquals("Jane Doe", result.getName());
        verify(stakeholderRepository, times(1)).findById(1);
    }

    @Test
    void testCreate() {
        var stakeholderDTO = new StakeholderDTO();
        stakeholderDTO.setName("Jane Doe");
        var stakeholder = new Stakeholder();
        stakeholder.setId(1);

        when(stakeholderRepository.save(any(Stakeholder.class))).thenReturn(stakeholder);

        var id = stakeholderService.create(stakeholderDTO);

        assertNotNull(id);
        assertEquals(1, id);
        verify(stakeholderRepository, times(1)).save(any(Stakeholder.class));
    }

    @Test
    void testUpdate() {
        var stakeholderDTO = new StakeholderDTO();
        stakeholderDTO.setName("Jane Doe");
        var stakeholder = new Stakeholder();
        stakeholder.setId(1);

        when(stakeholderRepository.findById(1)).thenReturn(Optional.of(stakeholder));
        when(stakeholderRepository.save(any(Stakeholder.class))).thenReturn(stakeholder);

        stakeholderService.update(1, stakeholderDTO);

        verify(stakeholderRepository, times(1)).findById(1);
        verify(stakeholderRepository, times(1)).save(any(Stakeholder.class));
    }

    @Test
    void testDelete() {
        stakeholderService.delete(1);

        verify(stakeholderRepository, times(1)).deleteById(1);
    }

    @Test
    void testGetReferencedWarning_ReferencedBySoftware() {
        var stakeholder = new Stakeholder();
        stakeholder.setId(1);
        var software = new Software();
        software.setId(1);

        when(stakeholderRepository.findById(1)).thenReturn(Optional.of(stakeholder));
        when(softwareRepository.findFirstByOwner(stakeholder)).thenReturn(software);

        var warning = stakeholderService.getReferencedWarning(1);

        assertNotNull(warning);
        assertEquals("stakeholder.software.owner.referenced", warning.getKey());
    }

    @Test
    void testGetReferencedWarning_ReferencedByDeploy() {
        var stakeholder = new Stakeholder();
        stakeholder.setId(1);
        var deploy = new Deploy();
        deploy.setId(1);

        when(stakeholderRepository.findById(1)).thenReturn(Optional.of(stakeholder));
        when(softwareRepository.findFirstByOwner(stakeholder)).thenReturn(null);
        when(deployRepository.findFirstByAuthorizer(stakeholder)).thenReturn(deploy);

        var warning = stakeholderService.getReferencedWarning(1);

        assertNotNull(warning);
        assertEquals("stakeholder.deploy.authorizer.referenced", warning.getKey());
    }
}