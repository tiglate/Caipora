package br.dev.ampliar.caipora.service;

import br.dev.ampliar.caipora.domain.Deploy;
import br.dev.ampliar.caipora.domain.Software;
import br.dev.ampliar.caipora.domain.Version;
import br.dev.ampliar.caipora.model.VersionDTO;
import br.dev.ampliar.caipora.repos.DeployRepository;
import br.dev.ampliar.caipora.repos.SoftwareRepository;
import br.dev.ampliar.caipora.repos.VersionRepository;
import br.dev.ampliar.caipora.util.NotFoundException;
import br.dev.ampliar.caipora.util.ReferencedWarning;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;


@Service
public class VersionServiceImpl implements VersionService {

    private final VersionRepository versionRepository;
    private final SoftwareRepository softwareRepository;
    private final DeployRepository deployRepository;
    private final String uploadDirectory;

    public VersionServiceImpl(final VersionRepository versionRepository,
                              final SoftwareRepository softwareRepository,
                              final DeployRepository deployRepository,
                              @Value("${file.upload.directory}") String uploadDirectory) {
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
    public Integer create(final VersionDTO versionDTO) {
        validateFile(versionDTO.getFile());
        final Version version = mapToEntity(versionDTO);
        version.setFileName(saveFile(versionDTO.getFile()));
        return versionRepository.save(version).getId();
    }


    @Override
    public void update(final Integer id, final VersionDTO versionDTO) {
        final Version version = versionRepository.findById(id)
                .orElseThrow(NotFoundException::new);
        versionDTO.setFileName(version.getFileName());
        mapToEntity(versionDTO, version);
        versionRepository.save(version);
    }

    @Override
    public void delete(final Integer id) {
        Version version = versionRepository.findById(id)
                .orElseThrow(NotFoundException::new);
        deleteFile(version.getFileName());
        versionRepository.deleteById(id);
    }

    private void deleteFile(String fileName) {
        Path filePath = Paths.get(uploadDirectory, fileName);
        try {
            Files.deleteIfExists(filePath);
        } catch (IOException e) {
            throw new RuntimeException("Failed to delete file: " + fileName, e);
        }
    }

    private VersionDTO mapToDTO(final Version version, final VersionDTO versionDTO) {
        versionDTO.setId(version.getId());
        versionDTO.setName(version.getName());
        versionDTO.setFileName(version.getFileName());
        versionDTO.setReleaseDate(version.getReleaseDate());
        versionDTO.setChangelog(version.getChangelog());
        versionDTO.setComments(version.getComments());
        versionDTO.setSoftwareId(version.getSoftware() == null ? null : version.getSoftware().getId());
        return versionDTO;
    }

    private Version mapToEntity(final VersionDTO versionDTO) {
        return mapToEntity(versionDTO, new Version());
    }

    private Version mapToEntity(final VersionDTO versionDTO, final Version version) {
        version.setName(versionDTO.getName());
        version.setFileName(versionDTO.getFileName());
        version.setReleaseDate(versionDTO.getReleaseDate());
        version.setChangelog(versionDTO.getChangelog());
        version.setComments(versionDTO.getComments());
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

    private String saveFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File is required");
        }

        String originalFilename = file.getOriginalFilename();
        String uniqueFilename = UUID.randomUUID() + "_" + originalFilename; // Generate a unique filename
        Path filePath = Paths.get(uploadDirectory, uniqueFilename);

        try {
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new RuntimeException("Failed to save uploaded file", e);
        }

        return uniqueFilename;
    }

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File is required");
        }

        long fileSizeInBytes = file.getSize();
        long fileSizeInMB = fileSizeInBytes / (1024 * 1024);

        if (fileSizeInMB > 1024) { // 1 GB = 1024 MB
            throw new IllegalArgumentException("File size exceeds 1 GB limit");
        }

        String originalFilename = file.getOriginalFilename();
        assert FilenameUtils.getExtension(originalFilename) != null;
        String fileExtension = FilenameUtils.getExtension(originalFilename).toLowerCase();

        if (!fileExtension.equals("zip") && !fileExtension.equals("msi") && !fileExtension.equals("exe")) {
            throw new IllegalArgumentException("Invalid file type. Only .zip, .msi, and .exe files are allowed.");
        }
    }

}
