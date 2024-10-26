package br.dev.ampliar.caipora.controller;

import br.dev.ampliar.caipora.model.AuthenticationRequest;
import br.dev.ampliar.caipora.util.FlashMessages;
import br.dev.ampliar.caipora.util.WebUtils;
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
        model.addAttribute("authentication", new AuthenticationRequest());
        if (loginRequired == Boolean.TRUE) {
            model.addAttribute(FlashMessages.MSG_INFO, "Please login to access this area.");
        }
        if (loginError == Boolean.TRUE) {
            model.addAttribute(FlashMessages.MSG_ERROR, "Your login was not successful - please try again.");
        }
        if (logoutSuccess == Boolean.TRUE) {
            model.addAttribute(FlashMessages.MSG_INFO, "Your logout was successful.");
        }
        return "authentication/login";
    }

}
