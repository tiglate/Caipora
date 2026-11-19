package br.dev.ampliar.caipora.controller;

import br.dev.ampliar.caipora.repos.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;


@Controller
@PreAuthorize("isAuthenticated()")
public class HomeController {

    private final SoftwareRepository softwareRepository;
    private final VersionRepository versionRepository;
    private final DeployRepository deployRepository;
    private final StakeholderRepository stakeholderRepository;
    private final DepartmentRepository departmentRepository;
    private final UserRepository userRepository;

    public HomeController(final SoftwareRepository softwareRepository,
                          final VersionRepository versionRepository,
                          final DeployRepository deployRepository,
                          final StakeholderRepository stakeholderRepository,
                          final DepartmentRepository departmentRepository,
                          final UserRepository userRepository) {
        this.softwareRepository = softwareRepository;
        this.versionRepository = versionRepository;
        this.deployRepository = deployRepository;
        this.stakeholderRepository = stakeholderRepository;
        this.departmentRepository = departmentRepository;
        this.userRepository = userRepository;
    }

    @SuppressWarnings("SameReturnValue")
    @GetMapping("/")
    public String index(final Model model) {
        // the template only shows the counters of the sections the user is allowed to see
        model.addAttribute("softwareCount", softwareRepository.count());
        model.addAttribute("versionCount", versionRepository.count());
        model.addAttribute("deployCount", deployRepository.count());
        model.addAttribute("stakeholderCount", stakeholderRepository.count());
        model.addAttribute("departmentCount", departmentRepository.count());
        model.addAttribute("userCount", userRepository.count());
        return "home/index";
    }

}
