package br.dev.ampliar.caipora.controller;

import br.dev.ampliar.caipora.domain.Stakeholder;
import br.dev.ampliar.caipora.domain.User;
import br.dev.ampliar.caipora.domain.Version;
import br.dev.ampliar.caipora.model.DeployDTO;
import br.dev.ampliar.caipora.model.Environment;
import br.dev.ampliar.caipora.repos.StakeholderRepository;
import br.dev.ampliar.caipora.repos.UserRepository;
import br.dev.ampliar.caipora.repos.VersionRepository;
import br.dev.ampliar.caipora.service.DeployService;
import br.dev.ampliar.caipora.service.SoftwareDeployedService;
import br.dev.ampliar.caipora.util.CustomCollectors;
import br.dev.ampliar.caipora.util.UserRoles;
import br.dev.ampliar.caipora.util.WebUtils;
import jakarta.validation.Valid;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;


@Controller
@RequestMapping("/deploys")
public class DeployController {

    private final DeployService deployService;
    private final VersionRepository versionRepository;
    private final UserRepository userRepository;
    private final StakeholderRepository stakeholderRepository;
    private final SoftwareDeployedService softwareDeployedService;

    public DeployController(final DeployService deployService,
                            final VersionRepository versionRepository,
                            final UserRepository userRepository,
                            final StakeholderRepository stakeholderRepository,
                            final SoftwareDeployedService softwareDeployedService) {
        this.deployService = deployService;
        this.versionRepository = versionRepository;
        this.userRepository = userRepository;
        this.stakeholderRepository = stakeholderRepository;
        this.softwareDeployedService = softwareDeployedService;
    }

    @ModelAttribute
    public void prepareContext(final Model model) {
        model.addAttribute("environmentValues", Environment.values());
        model.addAttribute("versionValues", versionRepository.findAll(Sort.by("id"))
                .stream()
                .collect(CustomCollectors.toSortedMap(Version::getId, Version::getName)));
        model.addAttribute("operatorValues", userRepository.findAll(Sort.by("id"))
                .stream()
                .collect(CustomCollectors.toSortedMap(User::getId, User::getName)));
        model.addAttribute("authorizerValues", stakeholderRepository.findAll(Sort.by("id"))
                .stream()
                .collect(CustomCollectors.toSortedMap(Stakeholder::getId, Stakeholder::getName)));
    }

    @GetMapping
    @PreAuthorize("hasAnyAuthority('" + UserRoles.ADMIN + "', '" + UserRoles.ITOPS + "', '" + UserRoles.OBSERVER + "')")
    public String list(final Model model) {
        model.addAttribute("deploys", softwareDeployedService.getSoftwareDeployedDTOs());
        return "deploy/list";
    }

    @GetMapping("/add")
    @PreAuthorize("hasAnyAuthority('" + UserRoles.ADMIN + "', '" + UserRoles.ITOPS + "')")
    public String add(@ModelAttribute("deploy") final DeployDTO deployDTO) {
        return "deploy/add";
    }

    @PostMapping("/add")
    @PreAuthorize("hasAnyAuthority('" + UserRoles.ADMIN + "', '" + UserRoles.ITOPS + "')")
    public String add(@ModelAttribute("deploy") @Valid final DeployDTO deployDTO,
                      final BindingResult bindingResult, final RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            return "deploy/add";
        }
        deployService.create(deployDTO);
        redirectAttributes.addFlashAttribute(WebUtils.MSG_SUCCESS, WebUtils.getMessage("deploy.create.success"));
        return "redirect:/deploys";
    }

    @GetMapping("/view/{id}")
    @PreAuthorize("hasAnyAuthority('" + UserRoles.ADMIN + "', '" + UserRoles.ITOPS + "')")
    public String view(@PathVariable(name = "id") final Integer id, final Model model) {
        model.addAttribute("deploy", deployService.get(id));
        return "deploy/view";
    }
}
