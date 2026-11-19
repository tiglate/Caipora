package br.dev.ampliar.caipora.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;


/**
 * Form used by a signed-in user to change their own password.
 */
public class PasswordChangeDTO {

    @NotBlank
    @Size(max = 255)
    private String currentPassword;

    @NotBlank
    @Size(min = 8, max = 255)
    private String newPassword;

    @NotBlank
    @Size(max = 255)
    private String confirmPassword;

    public String getCurrentPassword() {
        return currentPassword;
    }

    public void setCurrentPassword(final String currentPassword) {
        this.currentPassword = currentPassword;
    }

    public String getNewPassword() {
        return newPassword;
    }

    public void setNewPassword(final String newPassword) {
        this.newPassword = newPassword;
    }

    public String getConfirmPassword() {
        return confirmPassword;
    }

    public void setConfirmPassword(final String confirmPassword) {
        this.confirmPassword = confirmPassword;
    }
}
