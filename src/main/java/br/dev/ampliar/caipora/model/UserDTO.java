package br.dev.ampliar.caipora.model;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.OffsetDateTime;
import java.util.List;


@SuppressWarnings("unused")
public class UserDTO {

    private Integer id;

    @NotNull(groups = {OnCreate.class, OnUpdate.class})
    @Size(max = 255)
    private String name;

    @Size(max = 255)
    @Email
    private String email;

    @NotNull(groups = {OnCreate.class, OnUpdate.class})
    private Gender gender;

    @NotNull(groups = {OnCreate.class, OnUpdate.class})
    @Size(max = 50)
    @UserUsernameUnique
    private String username;

    @NotNull(groups = OnCreate.class)
    @Size(max = 255)
    private String password;

    @NotNull(groups = {OnCreate.class, OnUpdate.class})
    private Boolean enabled;

    @NotNull(groups = {OnCreate.class, OnUpdate.class})
    private Integer departmentId;

    private String departmentName;

    private List<Integer> roles;

    private OffsetDateTime createdAt;

    private OffsetDateTime updatedAt;

    public UserDTO() {
    }

    public UserDTO(Integer id, String name, String email, Gender gender, String username, String password,
                   Boolean enabled, Integer departmentId, String departmentName, OffsetDateTime createdAt,
                   OffsetDateTime updatedAt) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.gender = gender;
        this.username = username;
        this.password = password;
        this.enabled = enabled;
        this.departmentId = departmentId;
        this.departmentName = departmentName;
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

    public String getUsername() {
        return username;
    }

    public void setUsername(final String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(final String password) {
        this.password = password;
    }

    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(final Boolean enabled) {
        this.enabled = enabled;
    }

    public Integer getDepartmentId() {
        return departmentId;
    }

    public void setDepartmentId(final Integer departmentId) {
        this.departmentId = departmentId;
    }

    public List<Integer> getRoles() {
        return roles;
    }

    public void setRoles(final List<Integer> roles) {
        this.roles = roles;
    }

    public String getDepartmentName() {
        return departmentName;
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
