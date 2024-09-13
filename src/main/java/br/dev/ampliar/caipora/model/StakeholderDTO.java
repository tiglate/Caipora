package br.dev.ampliar.caipora.model;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;


public class StakeholderDTO {

    private Integer id;

    @NotNull
    @Size(max = 255)
    private String name;

    @Email
    @Size(max = 255)
    private String email;

    @NotNull
    private Gender gender;

    @NotNull
    private Integer departmentId;

    private String departmentName;

    public StakeholderDTO() {
    }

    public StakeholderDTO(Integer id, String name, String email, Gender gender, Integer departmentId, String departmentName) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.gender = gender;
        this.departmentId = departmentId;
        this.departmentName = departmentName;
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

    public Gender getGender() {
        return gender;
    }

    public void setGender(final Gender gender) {
        this.gender = gender;
    }

    public Integer getDepartmentId() {
        return departmentId;
    }

    public void setDepartmentId(final Integer departmentId) {
        this.departmentId = departmentId;
    }

    public String getDepartmentName() {
        return departmentName;
    }

    public void setDepartmentName(String departmentName) {
        this.departmentName = departmentName;
    }
}
