package br.dev.ampliar.caipora.controller;

import br.dev.ampliar.caipora.util.UserRoles;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;


@Controller
@PreAuthorize("hasAuthority('" + UserRoles.ADMIN + "')")
public class HomeController {

    @GetMapping("/")
    public String index() {
        return "home/index";
    }

}
