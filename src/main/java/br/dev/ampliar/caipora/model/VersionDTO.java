package br.dev.ampliar.caipora.model;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.time.OffsetDateTime;


@SuppressWarnings("unused")
public class VersionDTO {

    private Integer id;

    @NotNull
    @Size(max = 255)
    private String name;

    @Size(max = 255)
    private String fileName;

    @NotNull
    private LocalDate releaseDate;

    private String changelog;

    private String comments;

    @NotNull
    private Integer softwareId;

    private String softwareName;

    private boolean packageAvailable;

    private String fileSize;

    private OffsetDateTime createdAt;

    private OffsetDateTime updatedAt;

    public VersionDTO() {
    }

    public VersionDTO(Integer id, String name, String fileName, LocalDate releaseDate, Integer softwareId, String softwareName, OffsetDateTime createdAt, OffsetDateTime updatedAt) {
        this.id = id;
        this.name = name;
        this.softwareId = softwareId;
        this.softwareName = softwareName;
        this.releaseDate = releaseDate;
        this.fileName = fileName;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public Integer getId() {
        return id;
    }

    public void setId(final Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(final String fileName) {
        this.fileName = fileName;
    }

    public LocalDate getReleaseDate() {
        return releaseDate;
    }

    public void setReleaseDate(final LocalDate releaseDate) {
        this.releaseDate = releaseDate;
    }

    public String getChangelog() {
        return changelog;
    }

    public void setChangelog(final String changelog) {
        this.changelog = changelog;
    }

    public String getComments() {
        return comments;
    }

    public void setComments(final String comments) {
        this.comments = comments;
    }

    public Integer getSoftwareId() {
        return softwareId;
    }

    public void setSoftwareId(final Integer softwareId) {
        this.softwareId = softwareId;
    }

    public String getSoftwareName() {
        return softwareName;
    }

    public void setSoftwareName(String softwareName) {
        this.softwareName = softwareName;
    }

    /**
     * @return whether the package file is present in the application folder
     */
    public boolean isPackageAvailable() {
        return packageAvailable;
    }

    public void setPackageAvailable(boolean packageAvailable) {
        this.packageAvailable = packageAvailable;
    }

    public String getFileSize() {
        return fileSize;
    }

    public void setFileSize(String fileSize) {
        this.fileSize = fileSize;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(OffsetDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(OffsetDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
