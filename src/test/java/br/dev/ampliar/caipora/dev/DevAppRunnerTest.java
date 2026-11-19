package br.dev.ampliar.caipora.dev;

import org.junit.jupiter.api.Test;
import org.springframework.mock.env.MockEnvironment;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class DevAppRunnerTest {

    private static boolean runsWith(final String... profiles) {
        final MockEnvironment environment = new MockEnvironment();
        environment.setActiveProfiles(profiles);
        return new DevAppRunner(List.of(), environment).canItRun();
    }

    @Test
    void runsOnlyWithDevelopmentProfiles() {
        assertTrue(runsWith("local"));
        assertTrue(runsWith("dev"));
        assertTrue(runsWith("development", "debug"));
    }

    @Test
    void neverRunsWithoutProfileOrInProductionLikeEnvironments() {
        assertFalse(runsWith());
        assertFalse(runsWith("test"));
        assertFalse(runsWith("prod"));
        assertFalse(runsWith("local", "prod"));
        assertFalse(runsWith("dev", "uat"));
        assertFalse(runsWith("qa"));
    }

    @Test
    void runsLoadersInOrderAndSkipsThoseThatCannotRun() {
        final List<String> executed = new ArrayList<>();
        final MockEnvironment environment = new MockEnvironment();
        environment.setActiveProfiles("local");
        final List<DataLoaderCommand> loaders = new ArrayList<>(List.of(
                loader("second", 2, true, executed),
                loader("skipped", 1, false, executed),
                loader("first", 0, true, executed)));

        new DevAppRunner(loaders, environment).run(null);

        assertEquals(List.of("first", "second"), executed);
    }

    private static DataLoaderCommand loader(final String name, final int order, final boolean canRun,
                                            final List<String> executed) {
        return new DataLoaderCommand() {
            public int getOrder() { return order; }
            public String getName() { return name; }
            public boolean canItRun() { return canRun; }
            public int run() { executed.add(name); return 1; }
        };
    }
}
