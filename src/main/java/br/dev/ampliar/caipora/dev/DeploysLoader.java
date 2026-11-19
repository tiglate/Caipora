package br.dev.ampliar.caipora.dev;

import br.dev.ampliar.caipora.domain.*;
import br.dev.ampliar.caipora.model.Environment;
import br.dev.ampliar.caipora.repos.*;
import br.dev.ampliar.caipora.util.RandomUtils;
import net.datafaker.Faker;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;

/**
 * Simulates the release pipeline: every version is deployed to DEVELOPMENT, then QA, UAT, PRODUCTION and DR on
 * later dates; the most recent versions usually haven't reached every environment yet. For each application and
 * environment only the most recent deploy is active.
 */
@Component
public class DeploysLoader implements DataLoaderCommand {

    private static final Environment[] PIPELINE = {
            Environment.DEVELOPMENT, Environment.QA, Environment.UAT, Environment.PRODUCTION, Environment.DR};
    private static final int[] DAYS_AFTER_RELEASE = {1, 3, 7, 12, 13};

    private final RandomUtils randomUtils;
    private final DeployRepository deployRepository;
    private final SoftwareRepository softwareRepository;
    private final VersionRepository versionRepository;
    private final UserRepository userRepository;
    private final StakeholderRepository stakeholderRepository;

    public DeploysLoader(final RandomUtils randomUtils,
                         final DeployRepository deployRepository,
                         final SoftwareRepository softwareRepository,
                         final VersionRepository versionRepository,
                         final UserRepository userRepository,
                         final StakeholderRepository stakeholderRepository) {
        this.randomUtils = randomUtils;
        this.deployRepository = deployRepository;
        this.softwareRepository = softwareRepository;
        this.versionRepository = versionRepository;
        this.userRepository = userRepository;
        this.stakeholderRepository = stakeholderRepository;
    }

    @Override
    public int getOrder() {
        return 5;
    }

    @Override
    public String getName() {
        return "Deploys";
    }

    @Override
    public boolean canItRun() {
        return deployRepository.count() == 0;
    }

    @Override
    @Transactional
    public int run() {
        final Faker faker = new Faker();
        final List<User> operators = userRepository.findAll().stream()
                .filter(user -> Boolean.TRUE.equals(user.getEnabled()))
                .toList();
        final List<Stakeholder> authorizers = stakeholderRepository.findAll();
        if (operators.isEmpty() || authorizers.isEmpty()) {
            return 0;
        }
        final Set<String> rfcs = new HashSet<>();
        int count = 0;
        for (final Software software : softwareRepository.findAll()) {
            final List<Version> versions = versionRepository.findAllBySoftware(software, Sort.by("releaseDate", "id"));
            final Map<Environment, Deploy> latest = new EnumMap<>(Environment.class);
            for (int v = 0; v < versions.size(); v++) {
                final Version version = versions.get(v);
                final boolean newest = v == versions.size() - 1;
                // older versions normally went all the way; the newest one is still on its way
                final int reach = newest ? randomUtils.nextInt(1, PIPELINE.length + 1)
                        : (randomUtils.chance(0.8) ? PIPELINE.length : randomUtils.nextInt(1, PIPELINE.length));
                for (int e = 0; e < reach; e++) {
                    final LocalDate date = version.getReleaseDate()
                            .plusDays(DAYS_AFTER_RELEASE[e] + randomUtils.nextInt(0, 3));
                    if (date.isAfter(LocalDate.now())) {
                        break;
                    }
                    final Deploy deploy = new Deploy();
                    deploy.setVersion(version);
                    deploy.setEnvironment(PIPELINE[e]);
                    deploy.setExecutionDate(date);
                    deploy.setRfc(uniqueRfc(faker, rfcs));
                    deploy.setOperator(randomUtils.pickOne(operators));
                    deploy.setAuthorizer(randomUtils.pickOne(authorizers));
                    deploy.setNotes(randomUtils.chance(0.3) ? "<p>" + faker.lorem().sentence(10) + "</p>" : null);
                    deploy.setIsActive(false);
                    deployRepository.save(deploy);
                    latest.put(PIPELINE[e], deploy);
                    count++;
                }
            }
            latest.values().forEach(deploy -> deploy.setIsActive(true));
        }
        return count;
    }

    private static String uniqueRfc(final Faker faker, final Set<String> taken) {
        String rfc;
        do {
            rfc = "CHG" + faker.number().digits(7);
        } while (!taken.add(rfc));
        return rfc;
    }
}
