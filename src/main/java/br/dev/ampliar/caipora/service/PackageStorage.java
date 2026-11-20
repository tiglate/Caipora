package br.dev.ampliar.caipora.service;

import br.dev.ampliar.caipora.util.NotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Stream;

/**
 * Read-only access to the packages directory. The packages are copied there outside the application (file share,
 * CI pipeline, scp...), so they never travel through the browser. Layout:
 * <pre>
 * &lt;packages directory&gt;/
 *     &lt;application code&gt;/
 *         app-2.4.1.zip
 *         app-2.5.0.msi
 * </pre>
 * Only regular files sitting directly inside the application folder and with an accepted extension are packages.
 */
@Service
public class PackageStorage {

    private static final Logger log = LoggerFactory.getLogger(PackageStorage.class);

    private final String directory;
    private final List<String> extensions;

    public PackageStorage(@Value("${caipora.packages.directory}") final String directory,
                          @Value("${caipora.packages.extensions:zip,msi,exe,jar,war,ear,tar.gz,tgz,7z,nupkg}")
                          final List<String> extensions) {
        this.directory = directory;
        this.extensions = extensions.stream()
                .map(extension -> extension.trim().toLowerCase(Locale.ROOT))
                .map(extension -> extension.startsWith(".") ? extension.substring(1) : extension)
                .filter(extension -> !extension.isEmpty())
                // longest first, so "tar.gz" wins over "gz"
                .sorted(Comparator.comparingInt(String::length).reversed())
                .toList();
    }

    /**
     * A package file found in an application folder.
     */
    public record PackageFile(String name, long size, Instant lastModified) {

        public LocalDate lastModifiedDate() {
            return LocalDate.ofInstant(lastModified, ZoneId.systemDefault());
        }

        public String displaySize() {
            return PackageStorage.displaySize(size);
        }
    }

    public List<String> getExtensions() {
        return extensions;
    }

    public Path root() {
        return Paths.get(directory).toAbsolutePath().normalize();
    }

    /**
     * @return the folder of the given application (it may not exist yet)
     */
    public Path applicationFolder(final String applicationCode) {
        return resolveChild(root(), applicationCode);
    }

    /**
     * Creates the folder of the application, so whoever publishes the packages finds it ready. Failures are only
     * logged: the folder can always be created by hand.
     */
    public void createApplicationFolder(final String applicationCode) {
        try {
            Files.createDirectories(applicationFolder(applicationCode));
        } catch (IOException | RuntimeException e) {
            log.warn("Could not create the packages folder of application {}", applicationCode, e);
        }
    }

    /**
     * @return the packages of the application, most recent first (empty when its folder doesn't exist)
     */
    public List<PackageFile> listPackages(final String applicationCode) {
        final Path folder = applicationFolder(applicationCode);
        if (!Files.isDirectory(folder)) {
            return List.of();
        }
        try (Stream<Path> files = Files.list(folder)) {
            return files
                    .filter(path -> isAcceptedName(path.getFileName().toString()))
                    .map(this::toPackageFile)
                    .filter(Objects::nonNull)
                    .sorted(Comparator.comparing(PackageFile::lastModified).reversed()
                            .thenComparing(PackageFile::name))
                    .toList();
        } catch (IOException e) {
            throw new UncheckedIOException("Could not list the packages of application " + applicationCode, e);
        }
    }

    /**
     * @return the package file, after checking it is an accepted package directly inside the application folder
     * @throws IllegalArgumentException when the name is not acceptable (path traversal, sub folder, extension)
     * @throws NotFoundException        when the file doesn't exist
     */
    public Path resolvePackage(final String applicationCode, final String fileName) {
        if (fileName == null || fileName.isBlank() || !isAcceptedName(fileName)) {
            throw new IllegalArgumentException("Invalid package name");
        }
        final Path path = resolveChild(applicationFolder(applicationCode), fileName);
        if (!Files.isRegularFile(path) || !Files.isReadable(path)) {
            throw new NotFoundException("package not found");
        }
        return path;
    }

    public Optional<PackageFile> findPackage(final String applicationCode, final String fileName) {
        try {
            return Optional.ofNullable(toPackageFile(resolvePackage(applicationCode, fileName)));
        } catch (IllegalArgumentException | NotFoundException e) {
            return Optional.empty();
        }
    }

    boolean isAcceptedName(final String fileName) {
        final String lower = fileName.toLowerCase(Locale.ROOT);
        return extensions.stream().anyMatch(extension -> lower.endsWith("." + extension)
                && lower.length() > extension.length() + 1);
    }

    /**
     * Resolves a single path segment inside {@code parent}, refusing anything that would escape it (path
     * traversal): the normalized path must start with the parent and sit directly inside it.
     */
    private static Path resolveChild(final Path parent, final String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Invalid name");
        }
        final Path resolved = parent.resolve(name).normalize();
        if (!resolved.startsWith(parent) || !parent.equals(resolved.getParent())) {
            throw new IllegalArgumentException("Invalid name");
        }
        return resolved;
    }

    private PackageFile toPackageFile(final Path path) {
        try {
            final BasicFileAttributes attributes = Files.readAttributes(path, BasicFileAttributes.class);
            if (!attributes.isRegularFile()) {
                return null;
            }
            return new PackageFile(path.getFileName().toString(), attributes.size(),
                    attributes.lastModifiedTime().toInstant());
        } catch (IOException e) {
            log.debug("Ignoring unreadable package {}", path, e);
            return null;
        }
    }

    static String displaySize(final long bytes) {
        if (bytes < 1024) {
            return bytes + " B";
        }
        final String[] units = {"KB", "MB", "GB", "TB"};
        double value = bytes;
        int unit = -1;
        while (value >= 1024 && unit < units.length - 1) {
            value /= 1024;
            unit++;
        }
        return String.format(Locale.ROOT, "%.1f %s", value, units[unit]);
    }
}
