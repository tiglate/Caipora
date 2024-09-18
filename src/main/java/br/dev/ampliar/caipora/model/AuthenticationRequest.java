package br.dev.ampliar.caipora.model;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;


public class AuthenticationRequest {

    @NotNull
    @Size(max = 255)
    private String name;

    @NotNull
    @Size(max = 255)
    private String password;

    @NotNull
    private Boolean rememberMe;

    @SuppressWarnings("unused")
    public String getName() {
        return name;
    }

    @SuppressWarnings("unused")
    public void setName(final String name) {
        this.name = name;
    }

    @SuppressWarnings("unused")
    public String getPassword() {
        return password;
    }

    @SuppressWarnings("unused")
    public void setPassword(final String password) {
        this.password = password;
    }

    @SuppressWarnings("unused")
    public Boolean getRememberMe() {
        return rememberMe;
    }

    @SuppressWarnings("unused")
    public void setRememberMe(final Boolean rememberMe) {
        this.rememberMe = rememberMe;
    }

}
