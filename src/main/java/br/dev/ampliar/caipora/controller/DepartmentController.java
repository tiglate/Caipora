package br.dev.ampliar.caipora.controller;

import br.dev.ampliar.caipora.model.DepartmentDTO;
import br.dev.ampliar.caipora.service.DepartmentService;
import br.dev.ampliar.caipora.util.*;
import jakarta.validation.Valid;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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
@RequestMapping("/departments")
public class DepartmentController {

    private static final String ENTITY_NAME = "Department";
    private static final String CONTROLLER_ADD = "department/add";
    private static final String CONTROLLER_EDIT = "department/edit";
    private static final String CONTROLLER_VIEW = "department/view";
    private static final String CONTROLLER_LIST = "department/list";
    private static final String REDIRECT_TO_CONTROLLER_INDEX = "redirect:/departments";
    private static final Map<String, String> SORTABLE_COLUMNS = Map.ofEntries(
            entry("id", "sortById"),
            entry("name", "sortByName"),
            entry("email", "sortByEmail"));

    private final DepartmentService departmentService;
    private final SortUtils sortUtils = new SortUtils();

    public DepartmentController(final DepartmentService departmentService) {
        this.departmentService = departmentService;
    }

    @GetMapping
    @PreAuthorize("hasAnyAuthority('" + UserRoles.ROLE_DEPARTMENT_VIEW + "')")
    public String list(@ModelAttribute("departmentSearch") final DepartmentDTO filter,
                       @RequestParam(name = "sort", required = false) final String sort,
                       @PageableDefault(size = 20) final Pageable pageable,
                       final Model model) {
        final var sortOrder = sortUtils.addSortAttributesToModel(model, sort, "id,desc", SORTABLE_COLUMNS);
        final var pageRequest = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), sortOrder);
        final var departments = departmentService.findAll(filter, pageRequest);
        model.addAttribute("departments", departments);
        model.addAttribute("paginationModel", WebUtils.getPaginationModel(departments));
        return CONTROLLER_LIST;
    }

    @GetMapping("/view/{id}")
    @PreAuthorize("hasAnyAuthority('" + UserRoles.ROLE_DEPARTMENT_VIEW + "')")
    public String view(@PathVariable(name = "id") final Integer id, final Model model) {
        model.addAttribute("department", departmentService.get(id));
        return CONTROLLER_VIEW;
    }

    @GetMapping("/edit/{id}")
    @PreAuthorize("hasAnyAuthority('" + UserRoles.ROLE_DEPARTMENT_MANAGE + "')")
    public String edit(@PathVariable(name = "id") final Integer id, final Model model) {
        model.addAttribute("department", departmentService.get(id));
        return CONTROLLER_EDIT;
    }

    @GetMapping("/add")
    @PreAuthorize("hasAnyAuthority('" + UserRoles.ROLE_DEPARTMENT_MANAGE + "')")
    public String add(@ModelAttribute("department") final DepartmentDTO departmentDTO) {
        return CONTROLLER_ADD;
    }

    @PostMapping("/add")
    @PreAuthorize("hasAnyAuthority('" + UserRoles.ROLE_DEPARTMENT_MANAGE + "')")
    public String add(@ModelAttribute("department") @Valid final DepartmentDTO departmentDTO,
                      final BindingResult bindingResult, final RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            return CONTROLLER_ADD;
        }
        departmentService.create(departmentDTO);
        FlashMessages.createSuccess(redirectAttributes, ENTITY_NAME);
        return REDIRECT_TO_CONTROLLER_INDEX;
    }

    @PostMapping("/edit/{id}")
    @PreAuthorize("hasAnyAuthority('" + UserRoles.ROLE_DEPARTMENT_MANAGE + "')")
    public String edit(@PathVariable(name = "id") final Integer id,
                       @ModelAttribute("department") @Valid final DepartmentDTO departmentDTO,
                       final BindingResult bindingResult, final RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            departmentDTO.setId(id);
            return CONTROLLER_EDIT;
        }
        departmentService.update(id, departmentDTO);
        FlashMessages.updateSuccess(redirectAttributes, ENTITY_NAME);
        return REDIRECT_TO_CONTROLLER_INDEX;
    }

    @PostMapping("/delete/{id}")
    @PreAuthorize("hasAnyAuthority('" + UserRoles.ROLE_DEPARTMENT_MANAGE + "')")
    public String delete(@PathVariable(name = "id") final Integer id,
                         final RedirectAttributes redirectAttributes) {
        final ReferencedWarning referencedWarning = departmentService.getReferencedWarning(id);
        if (referencedWarning != null) {
            FlashMessages.referencedWarning(redirectAttributes, referencedWarning);
        } else {
            departmentService.delete(id);
            FlashMessages.deleteSuccess(redirectAttributes, ENTITY_NAME);
        }
        return REDIRECT_TO_CONTROLLER_INDEX;
    }

}
