package br.dev.ampliar.caipora.dev;

/**
 * Generates sample data for local development. Implementations are executed by {@link DevAppRunner}, in
 * {@link #getOrder()} order, only when {@link #canItRun()} (typically: the target table is still empty).
 */
public interface DataLoaderCommand {

    int getOrder();

    String getName();

    boolean canItRun();

    /**
     * @return number of rows created
     */
    int run();
}
