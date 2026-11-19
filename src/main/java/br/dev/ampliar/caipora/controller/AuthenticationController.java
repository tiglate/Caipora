package br.dev.ampliar.caipora.controller;

import br.dev.ampliar.caipora.model.AuthenticationRequest;
import br.dev.ampliar.caipora.util.FlashMessages;
import br.dev.ampliar.caipora.util.SecurityUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;


@Controller
public class AuthenticationController {

    @SuppressWarnings("SameReturnValue")
    @GetMapping("/login")
    public String login(
            @RequestParam(name = "loginRequired", required = false) final Boolean loginRequired,
            @RequestParam(name = "loginError", required = false) final Boolean loginError,
            @RequestParam(name = "logoutSuccess", required = false) final Boolean logoutSuccess,
            final Model model) {
        if (SecurityUtils.currentUser().isPresent()) {
            // already signed in: nothing to do on the login page
            return "redirect:/";
        }
        model.addAttribute("authentication", new AuthenticationRequest());
        if (loginRequired == Boolean.TRUE) {
            model.addAttribute(FlashMessages.MSG_INFO, "Please sign in to continue.");
        }
        if (loginError == Boolean.TRUE) {
            model.addAttribute(FlashMessages.MSG_ERROR, "Invalid username or password. After several failed attempts the account is temporarily locked.");
        }
        if (logoutSuccess == Boolean.TRUE) {
            model.addAttribute(FlashMessages.MSG_INFO, "You have been signed out.");
        }
        return "authentication/login";
    }

}
