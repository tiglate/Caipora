package br.dev.ampliar.caipora.model;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;


public class DepartmentDTO {

    private Integer id;

    @NotNull
    @Size(max = 255)
    @DepartmentNameUnique
    private String name;

    @Email
    @Size(max = 255)
    private String email;

    public DepartmentDTO() {
    }

    public DepartmentDTO(Integer id, String name, String email) {
        this.id = id;
        this.name = name;
        this.email = email;
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

    public String getEmail() {
        return email;
    }

    public void setEmail(final String email) {
        this.email = email;
    }

}
