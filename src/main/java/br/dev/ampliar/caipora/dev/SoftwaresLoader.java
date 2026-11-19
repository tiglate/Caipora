package br.dev.ampliar.caipora.dev;

import br.dev.ampliar.caipora.domain.Software;
import br.dev.ampliar.caipora.domain.Stakeholder;
import br.dev.ampliar.caipora.repos.SoftwareRepository;
import br.dev.ampliar.caipora.repos.StakeholderRepository;
import br.dev.ampliar.caipora.util.RandomUtils;
import net.datafaker.Faker;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

@Component
public class SoftwaresLoader implements DataLoaderCommand {

    private static final int QUANTITY = 12;

    private final RandomUtils randomUtils;
    private final SoftwareRepository softwareRepository;
    private final StakeholderRepository stakeholderRepository;

    public SoftwaresLoader(final RandomUtils randomUtils,
                           final SoftwareRepository softwareRepository,
                           final StakeholderRepository stakeholderRepository) {
        this.randomUtils = randomUtils;
        this.softwareRepository = softwareRepository;
        this.stakeholderRepository = stakeholderRepository;
    }

    @Override
    public int getOrder() {
        return 2;
    }

    @Override
    public String getName() {
        return "Applications";
    }

    @Override
    public boolean canItRun() {
        return softwareRepository.count() == 0;
    }

    @Override
    @Transactional
    public int run() {
        final Faker faker = new Faker();
        final List<Stakeholder> owners = stakeholderRepository.findAll();
        final Set<String> names = new HashSet<>();
        final Set<String> codes = new HashSet<>();
        int count = 0;
        while (count < QUANTITY) {
            final String name = (faker.app().name() + " " + faker.options()
                    .option("Portal", "Gateway", "Manager", "Hub", "Engine", "Platform", "Monitor", "Integrator"))
                    .toUpperCase(Locale.ROOT);
            if (!names.add(name)) {
                continue;
            }
            final Software software = new Software();
            software.setName(name);
            software.setCode(uniqueCode(name, codes));
            if (!owners.isEmpty() && randomUtils.chance(0.85)) {
                software.setOwner(randomUtils.pickOne(owners));
            }
            softwareRepository.save(software);
            count++;
        }
        return count;
    }

    /**
     * Initials of the name (padded to 3 letters), with a numeric suffix when already taken, e.g. "PTH", "PTH2".
     */
    private static String uniqueCode(final String name, final Set<String> codes) {
        final StringBuilder initials = new StringBuilder();
        for (final String word : DevNames.slug(name).split("\\.")) {
            if (!word.isEmpty()) {
                initials.append(Character.toUpperCase(word.charAt(0)));
            }
        }
        final String letters = DevNames.slug(name).replace(".", "").toUpperCase(Locale.ROOT);
        String base = initials.length() >= 3 ? initials.substring(0, 3) : (initials + letters).substring(0, 3);
        String code = base;
        for (int i = 2; !codes.add(code); i++) {
            code = base + i;
        }
        return code;
    }
}
