package br.dev.ampliar.caipora.service;

import br.dev.ampliar.caipora.model.UserDTO;
import br.dev.ampliar.caipora.model.UserSearchDTO;
import br.dev.ampliar.caipora.util.ReferencedWarning;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Collection;
import java.util.Map;
import java.util.Set;


public interface UserService {

    Page<UserDTO> findAll(UserSearchDTO searchDTO, Pageable pageable);

    UserDTO get(Integer id);

    @SuppressWarnings("UnusedReturnValue")
    Integer create(UserDTO userDTO);

    void update(Integer id, UserDTO userDTO);

    void delete(Integer id);

    boolean usernameExists(String username);

    /**
     * @return role ids of each of the given users (users without roles map to an empty set)
     */
    Map<Integer, Set<Integer>> getRoleIds(Collection<Integer> userIds);

    /**
     * @return whether {@code rawPassword} is the current password of the user
     */
    boolean passwordMatches(Integer id, String rawPassword);

    /**
     * Sets a new password chosen by the user themselves and clears the "password change required" flag.
     */
    void changeOwnPassword(Integer id, String newPassword);

    ReferencedWarning getReferencedWarning(Integer id);

}
