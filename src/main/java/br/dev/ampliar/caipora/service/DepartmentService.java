package br.dev.ampliar.caipora.service;

import br.dev.ampliar.caipora.model.DepartmentDTO;
import br.dev.ampliar.caipora.util.ReferencedWarning;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;


public interface DepartmentService {

    Page<DepartmentDTO> findAll(DepartmentDTO filter, Pageable pageable);

    DepartmentDTO get(Integer id);

    @SuppressWarnings("UnusedReturnValue")
    Integer create(DepartmentDTO departmentDTO);

    void update(Integer id, DepartmentDTO departmentDTO);

    void delete(Integer id);

    boolean nameExists(String name);

    ReferencedWarning getReferencedWarning(Integer id);

}
