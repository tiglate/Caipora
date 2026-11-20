package br.dev.ampliar.caipora.model;

/**
 * Lifecycle of a deploy: PENDING (queued) → RUNNING (script executing) → SUCCEEDED, FAILED or CANCELLED.
 * Only a SUCCEEDED deploy becomes the active one of its application and environment.
 */
public enum DeployStatus {

    PENDING,
    RUNNING,
    SUCCEEDED,
    FAILED,
    CANCELLED;

    public boolean isInProgress() {
        return this == PENDING || this == RUNNING;
    }

    public boolean isFinished() {
        return !isInProgress();
    }

    public boolean canBeRerun() {
        return this == FAILED || this == CANCELLED;
    }
}
