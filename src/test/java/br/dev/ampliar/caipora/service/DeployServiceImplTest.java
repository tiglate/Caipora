package br.dev.ampliar.caipora.service;

import br.dev.ampliar.caipora.domain.Deploy;
import br.dev.ampliar.caipora.model.DeployDTO;
import br.dev.ampliar.caipora.model.Environment;
import br.dev.ampliar.caipora.repos.DeployRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class DeployServiceImplTest {

    @Mock
    private DeployRepository deployRepository;

    @InjectMocks
    private DeployServiceImpl deployService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGet() {
        var deploy = new Deploy();
        deploy.setId(1);
        deploy.setEnvironment(Environment.PRODUCTION);

        when(deployRepository.findById(1)).thenReturn(Optional.of(deploy));

        var result = deployService.get(1);

        assertNotNull(result);
        assertEquals(Environment.PRODUCTION, result.getEnvironment());
        verify(deployRepository, times(1)).findById(1);
    }

    @Test
    void testCreate() {
        var deployDTO = new DeployDTO();
        deployDTO.setEnvironment(Environment.PRODUCTION);
        var deploy = new Deploy();
        deploy.setId(1);

        when(deployRepository.save(any(Deploy.class))).thenReturn(deploy);

        var id = deployService.create(deployDTO);

        assertNotNull(id);
        assertEquals(1, id);
        verify(deployRepository, times(1)).save(any(Deploy.class));
    }

    @Test
    void testRfcExists() {
        when(deployRepository.existsByRfcIgnoreCase("RFC123")).thenReturn(true);

        boolean exists = deployService.rfcExists("RFC123");

        assertTrue(exists);
        verify(deployRepository, times(1)).existsByRfcIgnoreCase("RFC123");
    }
}