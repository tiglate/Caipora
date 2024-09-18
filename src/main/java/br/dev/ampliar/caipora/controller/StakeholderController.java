package br.dev.ampliar.caipora.controller;

import br.dev.ampliar.caipora.domain.Department;
import br.dev.ampliar.caipora.model.Gender;
import br.dev.ampliar.caipora.model.StakeholderDTO;
import br.dev.ampliar.caipora.model.StakeholderSearchDTO;
import br.dev.ampliar.caipora.repos.DepartmentRepository;
import br.dev.ampliar.caipora.service.StakeholderService;
import br.dev.ampliar.caipora.util.*;
import jakarta.validation.Valid;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.SortDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Map;

import static java.util.Map.entry;


@Controller
@RequestMapping("/stakeholders")
public class StakeholderController {

    private final static String ENTITY_NAME = "Stakeholder";
    private final StakeholderService stakeholderService;
    private final DepartmentRepository departmentRepository;

    public StakeholderController(final StakeholderService stakeholderService,
            final DepartmentRepository departmentRepository) {
        this.stakeholderService = stakeholderService;
        this.departmentRepository = departmentRepository;
    }

    @ModelAttribute
    public void prepareContext(final Model model) {
        model.addAttribute("genderValues", Gender.values());
        model.addAttribute("departmentValues", departmentRepository.findAll(Sort.by("id"))
                .stream()
                .collect(CustomCollectors.toSortedMap(Department::getId, Department::getName)));
    }

    @SuppressWarnings("SameReturnValue")
    @GetMapping
    @PreAuthorize("hasAnyAuthority('" + UserRoles.ROLE_STAKEHOLDER_VIEW + "')")
    public String list(@ModelAttribute("stakeholderSearch") StakeholderSearchDTO filter,
                       @RequestParam(name = "sort", required = false) String sort,
                       @SortDefault(sort = "id", direction = Sort.Direction.DESC) @PageableDefault(size = 20) final Pageable pageable,
                       final Model model) {
        if (sort == null) {
            sort = "id,desc";
        }
        final var sortOrder = SortUtils.addSortAttributesToModel(model, sort, pageable, Map.ofEntries(
                entry("id", "sortById"),
                entry("name", "sortByName"),
                entry("email", "sortByEmail"),
                entry("gender", "sortByGender"),
                entry("department.name", "sortByDepartmentName")
        ));
        final var pageRequest = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), sortOrder);
        final var stakeholders = stakeholderService.findAll(filter, pageRequest);
        model.addAttribute("stakeholders", stakeholders);
        model.addAttribute("filter", filter);
        model.addAttribute("paginationModel", WebUtils.getPaginationModel(stakeholders));
        return "stakeholder/list";
    }

    @SuppressWarnings("SameReturnValue")
    @GetMapping("/view/{id}")
    @PreAuthorize("hasAnyAuthority('" + UserRoles.ROLE_STAKEHOLDER_VIEW + "')")
    public String view(@PathVariable(name = "id") final Integer id, final Model model) {
        model.addAttribute("stakeholder", stakeholderService.get(id));
        return "stakeholder/view";
    }

    @SuppressWarnings("SameReturnValue")
    @GetMapping("/add")
    @PreAuthorize("hasAnyAuthority('" + UserRoles.ROLE_STAKEHOLDER_MANAGE + "')")
    public String add(@ModelAttribute("stakeholder") final StakeholderDTO stakeholderDTO) {
        stakeholderDTO.setGender(Gender.MALE);
        return "stakeholder/add";
    }

    @PostMapping("/add")
    @PreAuthorize("hasAnyAuthority('" + UserRoles.ROLE_STAKEHOLDER_MANAGE + "')")
    public String add(@ModelAttribute("stakeholder") @Valid final StakeholderDTO stakeholderDTO,
            final BindingResult bindingResult, final RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            return "stakeholder/add";
        }
        stakeholderService.create(stakeholderDTO);
        FlashMessages.createSuccess(redirectAttributes, ENTITY_NAME);
        return "redirect:/stakeholders";
    }

    @SuppressWarnings("SameReturnValue")
    @GetMapping("/edit/{id}")
    @PreAuthorize("hasAnyAuthority('" + UserRoles.ROLE_STAKEHOLDER_MANAGE + "')")
    public String edit(@PathVariable(name = "id") final Integer id, final Model model) {
        model.addAttribute("stakeholder", stakeholderService.get(id));
        return "stakeholder/edit";
    }

    @PostMapping("/edit/{id}")
    @PreAuthorize("hasAnyAuthority('" + UserRoles.ROLE_STAKEHOLDER_MANAGE + "')")
    public String edit(@PathVariable(name = "id") final Integer id,
            @ModelAttribute("stakeholder") @Valid final StakeholderDTO stakeholderDTO,
            final BindingResult bindingResult, final RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            return "stakeholder/edit";
        }
        stakeholderService.update(id, stakeholderDTO);
        FlashMessages.updateSuccess(redirectAttributes, ENTITY_NAME);
        return "redirect:/stakeholders";
    }

    @SuppressWarnings("SameReturnValue")
    @PostMapping("/delete/{id}")
    @PreAuthorize("hasAnyAuthority('" + UserRoles.ROLE_STAKEHOLDER_MANAGE + "')")
    public String delete(@PathVariable(name = "id") final Integer id,
            final RedirectAttributes redirectAttributes) {
        final ReferencedWarning referencedWarning = stakeholderService.getReferencedWarning(id);
        if (referencedWarning != null) {
            redirectAttributes.addFlashAttribute(WebUtils.MSG_ERROR,
                    WebUtils.getMessage(referencedWarning.getKey(), referencedWarning.getParams().toArray()));
        } else {
            stakeholderService.delete(id);
            FlashMessages.deleteSuccess(redirectAttributes, ENTITY_NAME);
        }
        return "redirect:/stakeholders";
    }
}
