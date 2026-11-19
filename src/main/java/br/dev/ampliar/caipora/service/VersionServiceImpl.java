package br.dev.ampliar.caipora.service;

import br.dev.ampliar.caipora.domain.Deploy;
import br.dev.ampliar.caipora.domain.Software;
import br.dev.ampliar.caipora.domain.Version;
import br.dev.ampliar.caipora.model.VersionDTO;
import br.dev.ampliar.caipora.repos.DeployRepository;
import br.dev.ampliar.caipora.repos.SoftwareRepository;
import br.dev.ampliar.caipora.repos.VersionRepository;
import br.dev.ampliar.caipora.util.HtmlSanitizer;
import br.dev.ampliar.caipora.util.NotFoundException;
import br.dev.ampliar.caipora.util.ReferencedWarning;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Pattern;


@Service
@Transactional(readOnly = true)
public class VersionServiceImpl implements VersionService {

    private static final Logger log = LoggerFactory.getLogger(VersionServiceImpl.class);

    private static final Set<String> ALLOWED_EXTENSIONS = Set.of("zip", "msi", "exe");
    private static final int MAX_ORIGINAL_NAME_LENGTH = 150;
    private static final Pattern UNSAFE_FILENAME_CHARS = Pattern.compile("[^A-Za-z0-9._-]");
    private static final Pattern STORED_NAME_PREFIX = Pattern.compile("^[0-9a-fA-F-]{36}_");

    private final VersionRepository versionRepository;
    private final SoftwareRepository softwareRepository;
    private final DeployRepository deployRepository;
    private final String uploadDirectory;

    public VersionServiceImpl(final VersionRepository versionRepository,
                              final SoftwareRepository softwareRepository,
                              final DeployRepository deployRepository,
                              @Value("${file.upload.directory}") final String uploadDirectory) {
        this.versionRepository = versionRepository;
        this.softwareRepository = softwareRepository;
        this.deployRepository = deployRepository;
        this.uploadDirectory = uploadDirectory;
    }

    @Override
    public Page<VersionDTO> findAll(VersionDTO searchDTO, Pageable pageable) {
        return versionRepository.findAllBySearchCriteria(
                searchDTO.getName(),
                searchDTO.getSoftwareId(),
                pageable
        );
    }

    @Override
    public VersionDTO get(final Integer id) {
        return versionRepository.findById(id)
                .map(version -> mapToDTO(version, new VersionDTO()))
                .orElseThrow(NotFoundException::new);
    }

    @Override
    @Transactional
    public Integer create(final VersionDTO versionDTO) throws IOException {
        validateFile(versionDTO.getFile());
        final Version version = mapToEntity(versionDTO);
        final String storedFileName = saveFile(versionDTO.getFile());
        version.setFileName(storedFileName);
        try {
            return versionRepository.save(version).getId();
        } catch (RuntimeException e) {
            // don't leave orphan files behind when the database insert fails
            deleteFileQuietly(storedFileName);
            throw e;
        }
    }

    @Override
    @Transactional
    public void update(final Integer id, final VersionDTO versionDTO) {
        final Version version = versionRepository.findById(id)
                .orElseThrow(NotFoundException::new);
        // the package file cannot be replaced through an update
        versionDTO.setFileName(version.getFileName());
        mapToEntity(versionDTO, version);
        versionRepository.save(version);
    }

    @Override
    @Transactional
    public void delete(final Integer id) throws IOException {
        final Version version = versionRepository.findById(id)
                .orElseThrow(NotFoundException::new);
        final String fileName = version.getFileName();
        versionRepository.deleteById(id);
        // Remove the file only once the row is really gone, otherwise a rollback would leave a version without file.
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    deleteFileQuietly(fileName);
                }
            });
        } else {
            deleteFile(fileName);
        }
    }

    @Override
    public StoredFile getStoredFile(final Integer id) {
        final Version version = versionRepository.findById(id)
                .orElseThrow(NotFoundException::new);
        final Path path = resolveInUploadDirectory(version.getFileName());
        if (!Files.isRegularFile(path) || !Files.isReadable(path)) {
            throw new NotFoundException("file not found");
        }
        return new StoredFile(path, STORED_NAME_PREFIX.matcher(version.getFileName()).replaceFirst(""));
    }

    private void deleteFile(final String fileName) throws IOException {
        if (fileName == null || fileName.isBlank()) {
            return;
        }
        Files.deleteIfExists(resolveInUploadDirectory(fileName));
    }

    private void deleteFileQuietly(final String fileName) {
        try {
            deleteFile(fileName);
        } catch (IOException | RuntimeException e) {
            log.warn("Could not delete uploaded file {}", fileName, e);
        }
    }

    private Path uploadRoot() {
        return Paths.get(uploadDirectory).toAbsolutePath().normalize();
    }

    /**
     * Resolves a stored file name inside the upload directory, refusing anything that would escape it
     * (path traversal): the normalized path must start with the upload directory and sit directly inside it.
     */
    private Path resolveInUploadDirectory(final String fileName) {
        final Path root = uploadRoot();
        final Path resolved = root.resolve(fileName).normalize();
        if (!resolved.startsWith(root) || !root.equals(resolved.getParent())) {
            throw new IllegalArgumentException("Invalid file name");
        }
        return resolved;
    }

    private VersionDTO mapToDTO(final Version version, final VersionDTO versionDTO) {
        versionDTO.setId(version.getId());
        versionDTO.setName(version.getName());
        versionDTO.setFileName(version.getFileName());
        versionDTO.setReleaseDate(version.getReleaseDate());
        // sanitize on read as well, so content stored before sanitization was introduced is also safe
        versionDTO.setChangelog(HtmlSanitizer.sanitize(version.getChangelog()));
        versionDTO.setComments(HtmlSanitizer.sanitize(version.getComments()));
        if (version.getSoftware() != null) {
            versionDTO.setSoftwareId(version.getSoftware().getId());
            versionDTO.setSoftwareName(version.getSoftware().getName());
        }
        versionDTO.setCreatedAt(version.getCreatedAt());
        versionDTO.setUpdatedAt(version.getUpdatedAt());
        return versionDTO;
    }

    private Version mapToEntity(final VersionDTO versionDTO) {
        return mapToEntity(versionDTO, new Version());
    }

    private Version mapToEntity(final VersionDTO versionDTO, final Version version) {
        version.setName(versionDTO.getName());
        version.setFileName(versionDTO.getFileName());
        version.setReleaseDate(versionDTO.getReleaseDate());
        version.setChangelog(HtmlSanitizer.sanitize(versionDTO.getChangelog()));
        version.setComments(HtmlSanitizer.sanitize(versionDTO.getComments()));
        final Software software = versionDTO.getSoftwareId() == null ? null : softwareRepository.findById(versionDTO.getSoftwareId())
                .orElseThrow(() -> new NotFoundException("software not found"));
        version.setSoftware(software);
        return version;
    }

    @Override
    public ReferencedWarning getReferencedWarning(final Integer id) {
        final ReferencedWarning referencedWarning = new ReferencedWarning();
        final Version version = versionRepository.findById(id)
                .orElseThrow(NotFoundException::new);
        final Deploy versionDeploy = deployRepository.findFirstByVersion(version);
        if (versionDeploy != null) {
            referencedWarning.setKey("version.deploy.version.referenced");
            referencedWarning.addParam(versionDeploy.getId());
            return referencedWarning;
        }
        return null;
    }

    private String saveFile(final MultipartFile file) throws IOException {
        final Path root = uploadRoot();
        Files.createDirectories(root);
        final String uniqueFilename = UUID.randomUUID() + "_" + safeOriginalName(file.getOriginalFilename());
        final Path filePath = resolveInUploadDirectory(uniqueFilename);
        try (InputStream inputStream = file.getInputStream()) {
            // no REPLACE_EXISTING: the UUID makes collisions practically impossible, never overwrite silently
            Files.copy(inputStream, filePath);
        }
        return uniqueFilename;
    }

    /**
     * Keeps only the base name of the client-supplied file name and replaces anything outside a conservative
     * character set, so the stored name is safe on every file system and inside HTTP headers.
     */
    static String safeOriginalName(final String originalFilename) {
        String name = FilenameUtils.getName(originalFilename == null ? "" : originalFilename);
        name = UNSAFE_FILENAME_CHARS.matcher(name).replaceAll("_");
        if (name.length() > MAX_ORIGINAL_NAME_LENGTH) {
            final String extension = FilenameUtils.getExtension(name);
            name = name.substring(0, MAX_ORIGINAL_NAME_LENGTH - extension.length() - 1) + "." + extension;
        }
        return name.isBlank() ? "package" : name;
    }

    private void validateFile(final MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File is required");
        }
        final String extension = FilenameUtils.getExtension(file.getOriginalFilename());
        if (extension == null || !ALLOWED_EXTENSIONS.contains(extension.toLowerCase(Locale.ROOT))) {
            throw new IllegalArgumentException("Invalid file type. Only .zip, .msi, and .exe files are allowed.");
        }
    }

}
