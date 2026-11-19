package br.dev.ampliar.caipora.controller;

import br.dev.ampliar.caipora.model.PasswordChangeDTO;
import br.dev.ampliar.caipora.model.WebFormsUserDetails;
import br.dev.ampliar.caipora.service.UserService;
import br.dev.ampliar.caipora.service.UserSessionService;
import br.dev.ampliar.caipora.service.WebFormsUserDetailsService;
import br.dev.ampliar.caipora.util.FlashMessages;
import br.dev.ampliar.caipora.util.SecurityUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;


/**
 * Self-service account pages of the signed-in user.
 */
@Controller
@RequestMapping("/account")
@PreAuthorize("isAuthenticated()")
public class AccountController {

    public static final String PASSWORD_PATH = "/account/password";
    private static final String VIEW_PASSWORD = "account/password";

    private final UserService userService;
    private final UserSessionService userSessionService;
    private final WebFormsUserDetailsService userDetailsService;
    private final SecurityContextRepository securityContextRepository = new HttpSessionSecurityContextRepository();

    public AccountController(final UserService userService,
                             final UserSessionService userSessionService,
                             final WebFormsUserDetailsService userDetailsService) {
        this.userService = userService;
        this.userSessionService = userSessionService;
        this.userDetailsService = userDetailsService;
    }

    @ModelAttribute("passwordChangeRequired")
    public boolean passwordChangeRequired() {
        return SecurityUtils.currentUser().map(WebFormsUserDetails::isPasswordChangeRequired).orElse(false);
    }

    @GetMapping("/password")
    public String password(@ModelAttribute("passwordChange") final PasswordChangeDTO passwordChange) {
        return VIEW_PASSWORD;
    }

    @PostMapping("/password")
    public String password(@ModelAttribute("passwordChange") @Valid final PasswordChangeDTO passwordChange,
                           final BindingResult bindingResult,
                           final RedirectAttributes redirectAttributes,
                           final HttpServletRequest request,
                           final HttpServletResponse response,
                           final Model model) {
        final WebFormsUserDetails principal = SecurityUtils.currentUser().orElseThrow();
        if (!bindingResult.hasFieldErrors("currentPassword")
                && !userService.passwordMatches(principal.id, passwordChange.getCurrentPassword())) {
            bindingResult.rejectValue("currentPassword", "password.current", "The current password is not correct.");
        }
        if (!bindingResult.hasFieldErrors("newPassword") && passwordChange.getNewPassword() != null) {
            if (passwordChange.getNewPassword().equals(passwordChange.getCurrentPassword())) {
                bindingResult.rejectValue("newPassword", "password.same",
                        "The new password must be different from the current one.");
            } else if (passwordChange.getNewPassword().equalsIgnoreCase(principal.getUsername())) {
                bindingResult.rejectValue("newPassword", "password.username",
                        "The password cannot be the same as the username.");
            }
        }
        if (passwordChange.getNewPassword() != null
                && !passwordChange.getNewPassword().equals(passwordChange.getConfirmPassword())) {
            bindingResult.rejectValue("confirmPassword", "password.confirm", "The passwords don't match.");
        }
        if (bindingResult.hasErrors()) {
            // never send passwords back to the browser
            passwordChange.setCurrentPassword(null);
            passwordChange.setNewPassword(null);
            passwordChange.setConfirmPassword(null);
            return VIEW_PASSWORD;
        }

        userService.changeOwnPassword(principal.id, passwordChange.getNewPassword());

        // sign out every other session of this user and refresh the current one (the flag was cleared)
        final var session = request.getSession(false);
        userSessionService.invalidateSessions(principal.getUsername(), session == null ? null : session.getId());
        final var refreshed = userDetailsService.loadUserByUsername(principal.getUsername());
        final var context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(UsernamePasswordAuthenticationToken.authenticated(
                refreshed, null, refreshed.getAuthorities()));
        SecurityContextHolder.setContext(context);
        securityContextRepository.saveContext(context, request, response);

        FlashMessages.success(redirectAttributes, "Your password was changed.");
        return "redirect:/";
    }
}
