package br.dev.ampliar.caipora.service;

import br.dev.ampliar.caipora.model.UserDTO;
import br.dev.ampliar.caipora.model.UserSearchDTO;
import br.dev.ampliar.caipora.util.ReferencedWarning;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;


public interface UserService {

    Page<UserDTO> findAll(UserSearchDTO searchDTO, Pageable pageable);

    UserDTO get(Integer id);

    @SuppressWarnings("UnusedReturnValue")
    Integer create(UserDTO userDTO);

    void update(Integer id, UserDTO userDTO);

    void delete(Integer id);

    boolean usernameExists(String username);

    ReferencedWarning getReferencedWarning(Integer id);

}
