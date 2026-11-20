package br.dev.ampliar.caipora.model;

/**
 * Environments in pipeline order: a version is promoted from one to the next.
 */
public enum Environment {

    DEVELOPMENT("DEV"),
    QA("QA"),
    UAT("UAT"),
    PRODUCTION("PROD"),
    DR("DR");

    private final String shortName;

    Environment(final String shortName) {
        this.shortName = shortName;
    }

    public String getShortName() {
        return shortName;
    }

    /**
     * @return the environment a version deployed here is promoted to, or null for the last one
     */
    public Environment next() {
        final Environment[] values = values();
        return ordinal() + 1 < values.length ? values[ordinal() + 1] : null;
    }
}
