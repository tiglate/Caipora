package br.dev.ampliar.caipora.controller;

import br.dev.ampliar.caipora.domain.Stakeholder;
import br.dev.ampliar.caipora.model.SoftwareDTO;
import br.dev.ampliar.caipora.repos.StakeholderRepository;
import br.dev.ampliar.caipora.service.SoftwareService;
import br.dev.ampliar.caipora.util.*;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.SortDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Map;

import static java.util.Map.entry;


@Controller
@RequestMapping("/softwares")
public class SoftwareController {

    private final SoftwareService softwareService;
    private final StakeholderRepository stakeholderRepository;

    public SoftwareController(final SoftwareService softwareService,
            final StakeholderRepository stakeholderRepository) {
        this.softwareService = softwareService;
        this.stakeholderRepository = stakeholderRepository;
    }

    @ModelAttribute
    public void prepareContext(final Model model) {
        model.addAttribute("ownerValues", stakeholderRepository.findAll(Sort.by("id"))
                .stream()
                .collect(CustomCollectors.toSortedMap(Stakeholder::getId, Stakeholder::getName)));
    }

    @GetMapping
    @PreAuthorize("hasAnyAuthority('" + UserRoles.ADMIN + "', '" + UserRoles.OBSERVER + "')")
    public String list(@ModelAttribute("softwareSearch") SoftwareDTO filter,
                       @RequestParam(name = "sort", required = false) String sort,
                       @SortDefault(sort = "id") @PageableDefault(size = 20) final Pageable pageable,
                       final Model model) {
        final var sortOrder = SortUtils.addSortAttributesToModel(model, sort, pageable, Map.ofEntries(
                entry("id", "sortById"),
                entry("code", "sortByCode"),
                entry("name", "sortByName"),
                entry("owner.name", "sortByOwner")
        ));
        final var pageRequest = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), sortOrder);
        final var softwares = softwareService.findAll(filter, pageRequest);
        model.addAttribute("softwares", softwares);
        model.addAttribute("filter", filter);
        model.addAttribute("paginationModel", WebUtils.getPaginationModel(softwares));
        return "software/list";
    }

    @GetMapping("/add")
    @PreAuthorize("hasAuthority('" + UserRoles.ADMIN + "')")
    public String add(@ModelAttribute("software") final SoftwareDTO softwareDTO) {
        return "software/add";
    }

    @PostMapping("/add")
    @PreAuthorize("hasAuthority('" + UserRoles.ADMIN + "')")
    public String add(@ModelAttribute("software") @Valid final SoftwareDTO softwareDTO,
            final BindingResult bindingResult, final RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            return "software/add";
        }
        softwareService.create(softwareDTO);
        redirectAttributes.addFlashAttribute(WebUtils.MSG_SUCCESS, WebUtils.getMessage("software.create.success"));
        return "redirect:/softwares";
    }

    @GetMapping("/view/{id}")
    @PreAuthorize("hasAnyAuthority('" + UserRoles.ADMIN + "', '" + UserRoles.OBSERVER + "')")
    public String view(@PathVariable(name = "id") final Integer id, final Model model) {
        model.addAttribute("software", softwareService.get(id));
        return "software/view";
    }

    @GetMapping("/edit/{id}")
    @PreAuthorize("hasAuthority('" + UserRoles.ADMIN + "')")
    public String edit(@PathVariable(name = "id") final Integer id, final Model model) {
        model.addAttribute("software", softwareService.get(id));
        return "software/edit";
    }

    @PostMapping("/edit/{id}")
    @PreAuthorize("hasAuthority('" + UserRoles.ADMIN + "')")
    public String edit(@PathVariable(name = "id") final Integer id,
            @ModelAttribute("software") @Valid final SoftwareDTO softwareDTO,
            final BindingResult bindingResult, final RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            return "software/edit";
        }
        softwareService.update(id, softwareDTO);
        redirectAttributes.addFlashAttribute(WebUtils.MSG_SUCCESS, WebUtils.getMessage("software.update.success"));
        return "redirect:/softwares";
    }

    @PostMapping("/delete/{id}")
    @PreAuthorize("hasAuthority('" + UserRoles.ADMIN + "')")
    public String delete(@PathVariable(name = "id") final Integer id,
            final RedirectAttributes redirectAttributes) {
        final ReferencedWarning referencedWarning = softwareService.getReferencedWarning(id);
        if (referencedWarning != null) {
            redirectAttributes.addFlashAttribute(WebUtils.MSG_ERROR,
                    WebUtils.getMessage(referencedWarning.getKey(), referencedWarning.getParams().toArray()));
        } else {
            softwareService.delete(id);
            redirectAttributes.addFlashAttribute(WebUtils.MSG_INFO, WebUtils.getMessage("software.delete.success"));
        }
        return "redirect:/softwares";
    }

}
