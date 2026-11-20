package br.dev.ampliar.caipora.service;

import br.dev.ampliar.caipora.model.DeployDTO;
import br.dev.ampliar.caipora.model.Environment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;


public interface DeployService {

    DeployDTO get(Integer id);

    /**
     * Queues a deploy; its script runs in the background once the transaction commits.
     *
     * @throws DeployInProgressException when the application is already being deployed to that environment
     */
    @SuppressWarnings("UnusedReturnValue")
    Integer create(DeployDTO deployDTO);

    /**
     * Runs a failed or cancelled deploy again (same RFC), on behalf of the given operator.
     *
     * @throws DeployInProgressException when the application is already being deployed to that environment
     * @throws IllegalStateException     when the deploy did not fail
     */
    void rerun(Integer id, Integer operatorId);

    /**
     * @return whether the deploy was in progress (and is now cancelled)
     */
    boolean cancel(Integer id, String reason);

    /**
     * Cancels every deploy of the application still queued or running, e.g. one left stuck by a server that is
     * gone.
     *
     * @return how many deploys were cancelled
     */
    int unlock(Integer softwareId, String reason);

    Page<DeployDTO> findHistory(Integer softwareId, Pageable pageable);

    /**
     * @return the version to roll back to: the latest successfully deployed to the environment that is not the
     * active one
     */
    Optional<Integer> findRollbackVersion(Integer softwareId, Environment environment);

    boolean rfcExists(String rfc);

    /**
     * Thrown when a deploy of the same application and environment is already queued or running.
     */
    class DeployInProgressException extends IllegalStateException {

        public DeployInProgressException(final String message) {
            super(message);
        }
    }

}
