package br.dev.ampliar.caipora.dev;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

/**
 * Fills an empty database with random sample data, in development only.
 * <p>
 * It runs only when a development profile is active ({@code local}, {@code dev*}) and no production-like profile
 * ({@code prod}, {@code uat}, {@code qa}) is. Unlike other environments, the absence of any profile does
 * <strong>not</strong> enable it: a container started without a profile must never receive fake data.
 * {@code ./mvnw spring-boot:run} activates the {@code local} profile (see pom.xml).
 * <p>
 * Runs after the administrator account is created ({@code @Order(2)}).
 */
@Component
@Order(2)
public class DevAppRunner implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(DevAppRunner.class);

    private final List<DataLoaderCommand> dataLoaders;
    private final Environment environment;

    public DevAppRunner(final List<DataLoaderCommand> dataLoaders, final Environment environment) {
        this.dataLoaders = dataLoaders;
        this.environment = environment;
    }

    @Override
    public void run(final ApplicationArguments args) {
        if (!canItRun()) {
            log.debug("Sample data loading is disabled for non-development environments.");
            return;
        }
        log.info("Development environment detected. Starting sample data loading.");

        final List<DataLoaderCommand> ordered = dataLoaders.stream()
                .sorted(Comparator.comparingInt(DataLoaderCommand::getOrder))
                .toList();
        for (final DataLoaderCommand command : ordered) {
            if (!command.canItRun()) {
                log.info("DATA LOADER <{}> - SKIPPED", command.getName());
                continue;
            }
            log.info("DATA LOADER <{}> - START", command.getName());
            final long start = System.nanoTime();
            final int count = command.run();
            log.info("DATA LOADER <{}> - COMPLETED. AFFECTED ROWS: {}. It took {}ms to run.",
                    command.getName(), count, TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start));
        }
        log.info("Sample data loading completed.");
    }

    protected boolean canItRun() {
        final List<String> profiles = Arrays.stream(environment.getActiveProfiles())
                .map(profile -> profile.toLowerCase(Locale.ROOT))
                .toList();
        final boolean noBlockingProfile = profiles.stream()
                .noneMatch(p -> p.contains("prod") || p.contains("uat") || p.contains("qa"));
        final boolean developmentProfile = profiles.stream()
                .anyMatch(p -> p.contains("dev") || p.equals("local"));
        return noBlockingProfile && developmentProfile;
    }
}
