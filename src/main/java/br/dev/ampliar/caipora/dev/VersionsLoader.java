package br.dev.ampliar.caipora.dev;

import br.dev.ampliar.caipora.domain.Software;
import br.dev.ampliar.caipora.domain.Version;
import br.dev.ampliar.caipora.repos.SoftwareRepository;
import br.dev.ampliar.caipora.repos.VersionRepository;
import br.dev.ampliar.caipora.service.PackageStorage;
import br.dev.ampliar.caipora.util.RandomUtils;
import net.datafaker.Faker;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Creates a few versions per application, each with a small placeholder package (a zip file with a README) in the
 * folder of the application inside the packages directory, so downloads and deploy scripts work in development.
 */
@Component
public class VersionsLoader implements DataLoaderCommand {

    private final RandomUtils randomUtils;
    private final VersionRepository versionRepository;
    private final SoftwareRepository softwareRepository;
    private final PackageStorage packageStorage;

    public VersionsLoader(final RandomUtils randomUtils,
                          final VersionRepository versionRepository,
                          final SoftwareRepository softwareRepository,
                          final PackageStorage packageStorage) {
        this.randomUtils = randomUtils;
        this.versionRepository = versionRepository;
        this.softwareRepository = softwareRepository;
        this.packageStorage = packageStorage;
    }

    @Override
    public int getOrder() {
        return 3;
    }

    @Override
    public String getName() {
        return "Versions";
    }

    @Override
    public boolean canItRun() {
        return versionRepository.count() == 0;
    }

    @Override
    @Transactional
    public int run() {
        final Faker faker = new Faker();
        int count = 0;
        for (final Software software : softwareRepository.findAll()) {
            int major = randomUtils.nextInt(1, 4);
            int minor = 0;
            int patch = 0;
            LocalDate releaseDate = LocalDate.now().minusDays(randomUtils.nextInt(400, 720));
            final int quantity = randomUtils.nextInt(2, 6);
            for (int i = 0; i < quantity && releaseDate.isBefore(LocalDate.now()); i++) {
                final String suffix = randomUtils.chance(0.15) ? faker.options().option("-beta", "-rc1", "-rc2") : "";
                final String name = "v" + major + "." + minor + "." + patch + suffix;

                final Version version = new Version();
                version.setSoftware(software);
                version.setName(name);
                version.setReleaseDate(releaseDate);
                version.setChangelog(changelog(faker));
                version.setComments(randomUtils.chance(0.4) ? "<p>" + faker.lorem().sentence(12) + "</p>" : null);
                version.setFileName(createPackage(software.getCode() + "-" + name + ".zip", software, name));
                versionRepository.save(version);
                count++;

                if (randomUtils.chance(0.3)) {
                    minor++;
                    patch = 0;
                } else {
                    patch++;
                }
                releaseDate = releaseDate.plusDays(randomUtils.nextInt(20, 120));
            }
        }
        return count;
    }

    private String changelog(final Faker faker) {
        final StringBuilder html = new StringBuilder("<ul>");
        for (int i = 0; i < randomUtils.nextInt(2, 6); i++) {
            html.append("<li>").append(faker.options().option("Fixed", "Improved", "Added", "Updated", "Removed"))
                    .append(' ').append(faker.hacker().noun()).append(": ")
                    .append(faker.lorem().sentence(6)).append("</li>");
        }
        return html.append("</ul>").toString();
    }

    /**
     * Writes a placeholder package in the folder of the application (an existing file is kept as it is).
     */
    private String createPackage(final String fileName, final Software software, final String version) {
        final String safeName = fileName.replaceAll("[^A-Za-z0-9._-]", "_");
        final Path directory = packageStorage.applicationFolder(software.getCode());
        final Path file = directory.resolve(safeName);
        try {
            Files.createDirectories(directory);
            if (Files.exists(file)) {
                return safeName;
            }
            try (OutputStream out = Files.newOutputStream(file);
                 ZipOutputStream zip = new ZipOutputStream(out)) {
                zip.putNextEntry(new ZipEntry("README.txt"));
                zip.write(("Sample package generated for development.\n" + software.getName() + " " + version + "\n")
                        .getBytes(StandardCharsets.UTF_8));
                zip.closeEntry();
            }
        } catch (IOException e) {
            throw new UncheckedIOException("Could not create the sample package " + file, e);
        }
        return safeName;
    }
}
