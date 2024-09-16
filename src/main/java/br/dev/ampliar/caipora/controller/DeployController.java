package br.dev.ampliar.caipora.controller;

import br.dev.ampliar.caipora.domain.Software;
import br.dev.ampliar.caipora.domain.Stakeholder;
import br.dev.ampliar.caipora.domain.Version;
import br.dev.ampliar.caipora.model.DeployDTO;
import br.dev.ampliar.caipora.model.Environment;
import br.dev.ampliar.caipora.model.WebFormsUserDetails;
import br.dev.ampliar.caipora.repos.SoftwareRepository;
import br.dev.ampliar.caipora.repos.StakeholderRepository;
import br.dev.ampliar.caipora.repos.UserRepository;
import br.dev.ampliar.caipora.repos.VersionRepository;
import br.dev.ampliar.caipora.service.DeployService;
import br.dev.ampliar.caipora.service.SoftwareDeployedService;
import br.dev.ampliar.caipora.util.CustomCollectors;
import br.dev.ampliar.caipora.util.FlashMessages;
import br.dev.ampliar.caipora.util.UserRoles;
import jakarta.validation.Valid;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;


@Controller
@RequestMapping("/deploys")
public class DeployController {

    private final static String ENTITY_NAME = "Deploy";
    private final DeployService deployService;
    private final VersionRepository versionRepository;
    private final UserRepository userRepository;
    private final StakeholderRepository stakeholderRepository;
    private final SoftwareDeployedService softwareDeployedService;
    private final SoftwareRepository softwareRepository;

    public DeployController(final DeployService deployService,
                            final VersionRepository versionRepository,
                            final UserRepository userRepository,
                            final StakeholderRepository stakeholderRepository,
                            final SoftwareDeployedService softwareDeployedService,
                            final SoftwareRepository softwareRepository) {
        this.deployService = deployService;
        this.versionRepository = versionRepository;
        this.userRepository = userRepository;
        this.stakeholderRepository = stakeholderRepository;
        this.softwareDeployedService = softwareDeployedService;
        this.softwareRepository = softwareRepository;
    }

    @ModelAttribute
    public void prepareContext(final Model model) {
        model.addAttribute("environmentValues", Environment.values());
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

    @GetMapping("/add/{softwareId}")
    @PreAuthorize("hasAnyAuthority('" + UserRoles.ADMIN + "', '" + UserRoles.ITOPS + "')")
    public String add(@PathVariable(name = "softwareId") final Integer softwareId,
                      @ModelAttribute("deploy") final DeployDTO deployDTO,
                      final RedirectAttributes redirectAttributes,
                      final Model model) {
        if (loadSoftwareVersions(softwareId, deployDTO, redirectAttributes, model)) {
            return "redirect:/deploys";
        }
        deployDTO.setExecutionDate(LocalDate.now());
        return "deploy/add";
    }

    @PostMapping("/add/{softwareId}")
    @PreAuthorize("hasAnyAuthority('" + UserRoles.ADMIN + "', '" + UserRoles.ITOPS + "')")
    public String add(@PathVariable(name = "softwareId") final Integer softwareId,
                      @ModelAttribute("deploy") @Valid final DeployDTO deployDTO,
                      final BindingResult bindingResult,
                      final RedirectAttributes redirectAttributes,
                      final Model model) {
        if (loadSoftwareVersions(softwareId, deployDTO, redirectAttributes, model)) {
            return "redirect:/deploys";
        }
        deployDTO.setOperatorId(getCurrentUserId());
        deployDTO.setIsActive(true);
        if (bindingResult.hasErrors()) {
            return "deploy/add";
        }
        deployService.create(deployDTO);
        FlashMessages.createSuccess(redirectAttributes, ENTITY_NAME);
        return "redirect:/deploys";
    }

    @GetMapping("/view/{id}")
    @PreAuthorize("hasAnyAuthority('" + UserRoles.ADMIN + "', '" + UserRoles.ITOPS + "')")
    public String view(@PathVariable(name = "id") final Integer id,
                       final Model model,
                       final RedirectAttributes redirectAttributes) {
        final var deployDTO = deployService.get(id);
        if (loadSoftwareVersions(deployDTO.getSoftwareId(), deployDTO, redirectAttributes, model)) {
            return "redirect:/deploys";
        }
        model.addAttribute("deploy", deployDTO);
        return "deploy/view";
    }

    private boolean loadSoftwareVersions(final Integer softwareId,
                                         final DeployDTO deployDTO,
                                         final RedirectAttributes redirectAttributes,
                                         final Model model) {
        final var software = softwareRepository.findById(softwareId);
        if (software.isEmpty()) {
            FlashMessages.error(redirectAttributes, "Application not found!");
            return true;
        }
        loadVersionsCombo(model, software.get());
        deployDTO.setSoftwareCode(software.get().getCode());
        deployDTO.setSoftwareName(software.get().getName());
        return false;
    }

    private void loadVersionsCombo(Model model, Software software) {
        model.addAttribute("versionValues", versionRepository.findAllBySoftware(software, Sort.by("releaseDate").descending())
                .stream()
                .collect(CustomCollectors.toSortedMap(Version::getId, Version::getName)));
    }

    private Integer getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            if (authentication.getPrincipal() instanceof WebFormsUserDetails principal) {
                return principal.id;
            }
        }
        return null;
    }
}
