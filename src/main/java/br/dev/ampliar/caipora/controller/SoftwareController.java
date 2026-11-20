package br.dev.ampliar.caipora.controller;

import br.dev.ampliar.caipora.domain.Stakeholder;
import br.dev.ampliar.caipora.model.SoftwareDTO;
import br.dev.ampliar.caipora.repos.StakeholderRepository;
import br.dev.ampliar.caipora.service.SoftwareService;
import br.dev.ampliar.caipora.util.*;
import jakarta.validation.Valid;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Map;

import static java.util.Map.entry;


@SuppressWarnings("SameReturnValue")
@Controller
@RequestMapping("/softwares")
public class SoftwareController {

    private static final String ENTITY_NAME = "Application";
    private static final String CONTROLLER_ADD = "software/add";
    private static final String CONTROLLER_EDIT = "software/edit";
    private static final String CONTROLLER_VIEW = "software/view";
    private static final String CONTROLLER_LIST = "software/list";
    private static final String REDIRECT_TO_CONTROLLER_INDEX = "redirect:/softwares";
    private static final Map<String, String> SORTABLE_COLUMNS = Map.ofEntries(
            entry("id", "sortById"),
            entry("code", "sortByCode"),
            entry("name", "sortByName"),
            entry("owner.name", "sortByOwner"));

    private final SoftwareService softwareService;
    private final StakeholderRepository stakeholderRepository;
    private final SortUtils sortUtils = new SortUtils();

    public SoftwareController(final SoftwareService softwareService,
                              final StakeholderRepository stakeholderRepository) {
        this.softwareService = softwareService;
        this.stakeholderRepository = stakeholderRepository;
    }

    @ModelAttribute
    public void prepareContext(final Model model) {
        model.addAttribute("ownerValues", stakeholderRepository.findAll(Sort.by("name"))
                .stream()
                .collect(CustomCollectors.toSortedMap(Stakeholder::getId, Stakeholder::getName)));
    }

    @GetMapping
    @PreAuthorize("hasAnyAuthority('" + UserRoles.ROLE_APPLICATION_VIEW + "')")
    public String list(@ModelAttribute("softwareSearch") final SoftwareDTO filter,
                       @RequestParam(name = "sort", required = false) final String sort,
                       @PageableDefault(size = 20) final Pageable pageable,
                       final Model model) {
        final var sortOrder = sortUtils.addSortAttributesToModel(model, sort, "code,asc", SORTABLE_COLUMNS);
        final var pageRequest = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), sortOrder);
        final var softwares = softwareService.findAll(filter, pageRequest);
        model.addAttribute("softwares", softwares);
        model.addAttribute("paginationModel", WebUtils.getPaginationModel(softwares));
        return CONTROLLER_LIST;
    }

    @GetMapping("/view/{id}")
    @PreAuthorize("hasAnyAuthority('" + UserRoles.ROLE_APPLICATION_VIEW + "')")
    public String view(@PathVariable(name = "id") final Integer id, final Model model) {
        model.addAttribute("software", softwareService.get(id));
        model.addAttribute("packagesFolder", softwareService.getPackagesFolder(id));
        return CONTROLLER_VIEW;
    }

    @GetMapping("/add")
    @PreAuthorize("hasAnyAuthority('" + UserRoles.ROLE_APPLICATION_MANAGE + "')")
    public String add(@ModelAttribute("software") final SoftwareDTO softwareDTO) {
        return CONTROLLER_ADD;
    }

    @PostMapping("/add")
    @PreAuthorize("hasAnyAuthority('" + UserRoles.ROLE_APPLICATION_MANAGE + "')")
    public String add(@ModelAttribute("software") @Valid final SoftwareDTO softwareDTO,
            final BindingResult bindingResult, final RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            return CONTROLLER_ADD;
        }
        try {
            softwareService.create(softwareDTO, canEditScript());
        } catch (IllegalArgumentException e) {
            bindingResult.rejectValue("code", "software.code.invalid", e.getMessage());
            return CONTROLLER_ADD;
        }
        FlashMessages.createSuccess(redirectAttributes, ENTITY_NAME);
        return REDIRECT_TO_CONTROLLER_INDEX;
    }

    @GetMapping("/edit/{id}")
    @PreAuthorize("hasAnyAuthority('" + UserRoles.ROLE_APPLICATION_MANAGE + "')")
    public String edit(@PathVariable(name = "id") final Integer id, final Model model) {
        model.addAttribute("software", softwareService.get(id));
        model.addAttribute("packagesFolder", softwareService.getPackagesFolder(id));
        return CONTROLLER_EDIT;
    }

    @PostMapping("/edit/{id}")
    @PreAuthorize("hasAnyAuthority('" + UserRoles.ROLE_APPLICATION_MANAGE + "')")
    public String edit(@PathVariable(name = "id") final Integer id,
            @ModelAttribute("software") @Valid final SoftwareDTO softwareDTO,
            final BindingResult bindingResult, final RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            softwareDTO.setId(id);
            return CONTROLLER_EDIT;
        }
        try {
            softwareService.update(id, softwareDTO, canEditScript());
        } catch (IllegalArgumentException e) {
            bindingResult.rejectValue("code", "software.code.locked", e.getMessage());
            softwareDTO.setId(id);
            return CONTROLLER_EDIT;
        }
        FlashMessages.updateSuccess(redirectAttributes, ENTITY_NAME);
        return REDIRECT_TO_CONTROLLER_INDEX;
    }

    @PostMapping("/delete/{id}")
    @PreAuthorize("hasAnyAuthority('" + UserRoles.ROLE_APPLICATION_MANAGE + "')")
    public String delete(@PathVariable(name = "id") final Integer id,
            final RedirectAttributes redirectAttributes) {
        final ReferencedWarning referencedWarning = softwareService.getReferencedWarning(id);
        if (referencedWarning != null) {
            FlashMessages.referencedWarning(redirectAttributes, referencedWarning);
        } else {
            softwareService.delete(id);
            FlashMessages.deleteSuccess(redirectAttributes, ENTITY_NAME);
        }
        return REDIRECT_TO_CONTROLLER_INDEX;
    }

    private static boolean canEditScript() {
        return SecurityUtils.currentUser()
                .map(user -> user.getAuthorities().stream()
                        .anyMatch(authority -> UserRoles.ROLE_DEPLOY_SCRIPT_MANAGE.equals(authority.getAuthority())))
                .orElse(false);
    }

}
