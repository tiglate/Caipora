package br.dev.ampliar.caipora.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;


@Controller
@PreAuthorize("isAuthenticated()")
public class HomeController {

    @SuppressWarnings("SameReturnValue")
    @GetMapping("/")
    public String index() {
        return "home/index";
    }

}
