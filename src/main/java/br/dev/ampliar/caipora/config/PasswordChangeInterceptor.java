package br.dev.ampliar.caipora.config;

import br.dev.ampliar.caipora.controller.AccountController;
import br.dev.ampliar.caipora.model.WebFormsUserDetails;
import br.dev.ampliar.caipora.util.SecurityUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * Users flagged with "password change required" (initial or administrator-assigned password) can only reach the
 * change-password page until they choose a new password.
 */
public class PasswordChangeInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(final HttpServletRequest request, final HttpServletResponse response,
                             final Object handler) throws Exception {
        final boolean required = SecurityUtils.currentUser()
                .map(WebFormsUserDetails::isPasswordChangeRequired)
                .orElse(false);
        if (required) {
            response.sendRedirect(request.getContextPath() + AccountController.PASSWORD_PATH);
            return false;
        }
        return true;
    }
}
