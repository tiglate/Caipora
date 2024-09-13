package br.dev.ampliar.caipora.model;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;


public class SoftwareDTO {

    private Integer id;

    @NotNull
    @Size(max = 20)
    @SoftwareCodeUnique
    private String code;

    @NotNull
    @Size(max = 255)
    @SoftwareNameUnique
    private String name;

    public SoftwareDTO() {
    }

    public SoftwareDTO(Integer id, String code, String name, Integer ownerId, String ownerName) {
        this.id = id;
        this.code = code;
        this.name = name;
        this.ownerId = ownerId;
        this.ownerName = ownerName;
    }

    private Integer ownerId;

    private String ownerName;

    public Integer getId() {
        return id;
    }

    public void setId(final Integer id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(final String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public Integer getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(final Integer ownerId) {
        this.ownerId = ownerId;
    }

    public String getOwnerName() {
        return ownerName;
    }

    public void setOwnerName(String ownerName) {
        this.ownerName = ownerName;
    }
}
