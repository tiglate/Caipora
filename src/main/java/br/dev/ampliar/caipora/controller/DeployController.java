package br.dev.ampliar.caipora.controller;

import br.dev.ampliar.caipora.domain.Software;
import br.dev.ampliar.caipora.domain.Stakeholder;
import br.dev.ampliar.caipora.domain.Version;
import br.dev.ampliar.caipora.model.DeployDTO;
import br.dev.ampliar.caipora.model.Environment;
import br.dev.ampliar.caipora.model.WebFormsUserDetails;
import br.dev.ampliar.caipora.repos.SoftwareRepository;
import br.dev.ampliar.caipora.repos.StakeholderRepository;
import br.dev.ampliar.caipora.repos.VersionRepository;
import br.dev.ampliar.caipora.service.DeployService;
import br.dev.ampliar.caipora.service.SoftwareDeployedService;
import br.dev.ampliar.caipora.util.CustomCollectors;
import br.dev.ampliar.caipora.util.FlashMessages;
import br.dev.ampliar.caipora.util.NotFoundException;
import br.dev.ampliar.caipora.util.SecurityUtils;
import br.dev.ampliar.caipora.util.UserRoles;
import br.dev.ampliar.caipora.util.WebUtils;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;


@SuppressWarnings("SameReturnValue")
@Controller
@RequestMapping("/deploys")
public class DeployController {

    private static final String CONTROLLER_ADD = "deploy/add";
    private static final String CONTROLLER_VIEW = "deploy/view";
    private static final String CONTROLLER_LIST = "deploy/list";
    private static final String CONTROLLER_HISTORY = "deploy/history";
    private static final String REDIRECT_TO_CONTROLLER_INDEX = "redirect:/deploys";
    private static final String REDIRECT_TO_VIEW = "redirect:/deploys/view/";

    private final DeployService deployService;
    private final VersionRepository versionRepository;
    private final StakeholderRepository stakeholderRepository;
    private final SoftwareDeployedService softwareDeployedService;
    private final SoftwareRepository softwareRepository;

    public DeployController(final DeployService deployService,
                            final VersionRepository versionRepository,
                            final StakeholderRepository stakeholderRepository,
                            final SoftwareDeployedService softwareDeployedService,
                            final SoftwareRepository softwareRepository) {
        this.deployService = deployService;
        this.versionRepository = versionRepository;
        this.stakeholderRepository = stakeholderRepository;
        this.softwareDeployedService = softwareDeployedService;
        this.softwareRepository = softwareRepository;
    }

    /**
     * Only the fields of the form can be bound: status, activation, operator and output are set by the server.
     */
    @InitBinder("deploy")
    public void initBinder(final WebDataBinder binder) {
        binder.setAllowedFields("versionId", "environment", "rfc", "executionDate", "authorizerId", "notes");
    }

    @GetMapping
    @PreAuthorize("hasAnyAuthority('" + UserRoles.ROLE_DEPLOY_VIEW + "')")
    public String list(final Model model) {
        model.addAttribute("deploys", softwareDeployedService.getSoftwareDeployedDTOs());
        return CONTROLLER_LIST;
    }

    /**
     * New deploy form. Optional parameters preselect the version and environment: {@code promote} (a deploy whose
     * version goes to the next environment), {@code rollback} (an environment that goes back to its previous
     * version), or {@code versionId} / {@code environment} directly.
     */
    @GetMapping("/add/{softwareId}")
    @PreAuthorize("hasAnyAuthority('" + UserRoles.ROLE_DEPLOY_MANAGE + "')")
    public String add(@PathVariable(name = "softwareId") final Integer softwareId,
                      @ModelAttribute("deploy") final DeployDTO deployDTO,
                      @RequestParam(name = "promote", required = false) final Integer promoteDeployId,
                      @RequestParam(name = "rollback", required = false) final Environment rollbackEnvironment,
                      final RedirectAttributes redirectAttributes,
                      final Model model) {
        final Optional<Software> software = softwareRepository.findById(softwareId);
        if (software.isEmpty()) {
            FlashMessages.error(redirectAttributes, "Application not found!");
            return REDIRECT_TO_CONTROLLER_INDEX;
        }
        if (promoteDeployId != null) {
            final DeployDTO source = deployService.get(promoteDeployId);
            if (!softwareId.equals(source.getSoftwareId()) || source.getEnvironment().next() == null) {
                FlashMessages.error(redirectAttributes, "This deploy cannot be promoted.");
                return REDIRECT_TO_CONTROLLER_INDEX;
            }
            deployDTO.setVersionId(source.getVersionId());
            deployDTO.setEnvironment(source.getEnvironment().next());
            model.addAttribute("intent", "Promote " + source.getVersionName() + " from " + source.getEnvironment()
                    + " to " + source.getEnvironment().next());
        } else if (rollbackEnvironment != null) {
            final Optional<Integer> versionId = deployService.findRollbackVersion(softwareId, rollbackEnvironment);
            if (versionId.isEmpty()) {
                FlashMessages.info(redirectAttributes, "There is no previous version of " + software.get().getName()
                        + " in " + rollbackEnvironment + " to roll back to.");
                return REDIRECT_TO_CONTROLLER_INDEX;
            }
            deployDTO.setVersionId(versionId.get());
            deployDTO.setEnvironment(rollbackEnvironment);
            model.addAttribute("intent", "Roll back " + rollbackEnvironment + " to the previous version");
        }
        deployDTO.setExecutionDate(LocalDate.now());
        prepareForm(software.get(), deployDTO, model);
        return CONTROLLER_ADD;
    }

    @PostMapping("/add/{softwareId}")
    @PreAuthorize("hasAnyAuthority('" + UserRoles.ROLE_DEPLOY_MANAGE + "')")
    public String add(@PathVariable(name = "softwareId") final Integer softwareId,
                      @ModelAttribute("deploy") @Valid final DeployDTO deployDTO,
                      final BindingResult bindingResult,
                      final RedirectAttributes redirectAttributes,
                      final Model model) {
        final Optional<Software> software = softwareRepository.findById(softwareId);
        if (software.isEmpty()) {
            FlashMessages.error(redirectAttributes, "Application not found!");
            return REDIRECT_TO_CONTROLLER_INDEX;
        }
        deployDTO.setOperatorId(SecurityUtils.currentUserId());
        // the version must belong to the application in the URL (the form only lists those, but the request
        // could be forged)
        if (deployDTO.getVersionId() != null && versionRepository.findById(deployDTO.getVersionId())
                .map(version -> version.getSoftware() == null || !softwareId.equals(version.getSoftware().getId()))
                .orElse(true)) {
            bindingResult.rejectValue("versionId", "deploy.version.invalid",
                    "Please select a version of this application.");
        }
        if (!bindingResult.hasErrors()) {
            try {
                final Integer id = deployService.create(deployDTO);
                FlashMessages.success(redirectAttributes, software.get().getDeployScript() == null
                        ? "Deploy recorded." : "Deploy started: its output is shown below.");
                return REDIRECT_TO_VIEW + id;
            } catch (DeployService.DeployInProgressException e) {
                bindingResult.reject("deploy.in.progress", e.getMessage());
            }
        }
        prepareForm(software.get(), deployDTO, model);
        return CONTROLLER_ADD;
    }

    @GetMapping("/view/{id}")
    @PreAuthorize("hasAnyAuthority('" + UserRoles.ROLE_DEPLOY_VIEW + "')")
    public String view(@PathVariable(name = "id") final Integer id, final Model model) {
        model.addAttribute("deploy", deployService.get(id));
        return CONTROLLER_VIEW;
    }

    /**
     * Status and output of a deploy, polled by the view page while the deploy is in progress.
     */
    @GetMapping("/view/{id}/execution")
    @PreAuthorize("hasAnyAuthority('" + UserRoles.ROLE_DEPLOY_VIEW + "')")
    public String execution(@PathVariable(name = "id") final Integer id, final Model model,
                            final HttpServletResponse response) {
        final DeployDTO deploy = deployService.get(id);
        if (!deploy.isInProgress()) {
            // finished: reload the whole page, so the status, the buttons and the active flag are up to date
            response.setHeader("HX-Refresh", "true");
        }
        model.addAttribute("deploy", deploy);
        return CONTROLLER_VIEW + " :: execution";
    }

    @PostMapping("/rerun/{id}")
    @PreAuthorize("hasAnyAuthority('" + UserRoles.ROLE_DEPLOY_MANAGE + "')")
    public String rerun(@PathVariable(name = "id") final Integer id, final RedirectAttributes redirectAttributes) {
        try {
            deployService.rerun(id, SecurityUtils.currentUserId());
            FlashMessages.success(redirectAttributes, "Deploy started again.");
        } catch (IllegalStateException e) {
            FlashMessages.error(redirectAttributes, e.getMessage());
        }
        return REDIRECT_TO_VIEW + id;
    }

    @PostMapping("/cancel/{id}")
    @PreAuthorize("hasAnyAuthority('" + UserRoles.ROLE_DEPLOY_MANAGE + "')")
    public String cancel(@PathVariable(name = "id") final Integer id, final RedirectAttributes redirectAttributes) {
        if (deployService.cancel(id, "Cancelled by " + currentUsername() + ".")) {
            FlashMessages.success(redirectAttributes, "Deploy cancelled.");
        } else {
            FlashMessages.info(redirectAttributes, "The deploy had already finished.");
        }
        return REDIRECT_TO_VIEW + id;
    }

    /**
     * Releases an application whose deploy is stuck (e.g. the server that ran it is gone) by cancelling every
     * deploy of it still queued or running.
     */
    @PostMapping("/unlock/{softwareId}")
    @PreAuthorize("hasAnyAuthority('" + UserRoles.ROLE_DEPLOY_MANAGE + "')")
    public String unlock(@PathVariable(name = "softwareId") final Integer softwareId,
                         final RedirectAttributes redirectAttributes) {
        final int count = deployService.unlock(softwareId, "Released (unlock) by " + currentUsername() + ".");
        if (count == 0) {
            FlashMessages.info(redirectAttributes, "No deploy of this application was in progress.");
        } else {
            FlashMessages.success(redirectAttributes, count == 1 ? "1 deploy cancelled." : count + " deploys cancelled.");
        }
        return REDIRECT_TO_CONTROLLER_INDEX;
    }

    @GetMapping("/history/{softwareId}")
    @PreAuthorize("hasAnyAuthority('" + UserRoles.ROLE_DEPLOY_VIEW + "')")
    public String history(@PathVariable(name = "softwareId") final Integer softwareId,
                          @PageableDefault(size = 20) final Pageable pageable,
                          final Model model) {
        final Software software = softwareRepository.findById(softwareId)
                .orElseThrow(NotFoundException::new);
        final var pageRequest = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(),
                Sort.by(Sort.Order.desc("createdAt"), Sort.Order.desc("id")));
        final var deploys = deployService.findHistory(softwareId, pageRequest);
        model.addAttribute("software", software);
        model.addAttribute("deploys", deploys);
        model.addAttribute("paginationModel", WebUtils.getPaginationModel(deploys));
        return CONTROLLER_HISTORY;
    }

    private void prepareForm(final Software software, final DeployDTO deployDTO, final Model model) {
        deployDTO.setSoftwareId(software.getId());
        deployDTO.setSoftwareCode(software.getCode());
        deployDTO.setSoftwareName(software.getName());
        final List<Version> versions = versionRepository.findAllBySoftware(software,
                Sort.by("releaseDate").descending().and(Sort.by("id").descending()));
        model.addAttribute("versionValues", versions);
        model.addAttribute("environmentValues", Environment.values());
        model.addAttribute("authorizerValues", stakeholderRepository.findAll(Sort.by("name"))
                .stream()
                .collect(CustomCollectors.toSortedMap(Stakeholder::getId, Stakeholder::getName)));
        model.addAttribute("hasScript", software.getDeployScript() != null && !software.getDeployScript().isBlank());
    }

    private static String currentUsername() {
        return SecurityUtils.currentUser().map(WebFormsUserDetails::getUsername).orElse("unknown");
    }
}
