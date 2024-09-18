package br.dev.ampliar.caipora.controller;

import br.dev.ampliar.caipora.model.DepartmentDTO;
import br.dev.ampliar.caipora.service.DepartmentService;
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


@SuppressWarnings("SameReturnValue")
@Controller
@RequestMapping("/departments")
public class DepartmentController {

    private final static String ENTITY_NAME = "Department";
    private final DepartmentService departmentService;

    public DepartmentController(final DepartmentService departmentService) {
        this.departmentService = departmentService;
    }

    @GetMapping
    @PreAuthorize("hasAnyAuthority('" + UserRoles.ROLE_DEPARTMENT_VIEW + "')")
    public String list(@ModelAttribute("departmentSearch") DepartmentDTO filter,
                       @RequestParam(name = "sort", required = false) String sort,
                       @SortDefault(sort = "id", direction = Sort.Direction.DESC) @PageableDefault(size = 20) final Pageable pageable,
                       final Model model) {
        if (sort == null) {
            sort = "id,desc";
        }
        final var sortOrder = SortUtils.addSortAttributesToModel(model, sort, pageable, Map.ofEntries(
            entry("id", "sortById"),
            entry("name", "sortByName"),
            entry("email", "sortByEmail")
        ));
        final var pageRequest = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), sortOrder);
        final var departments = departmentService.findAll(filter, pageRequest);
        model.addAttribute("departments", departments);
        model.addAttribute("filter", filter);
        model.addAttribute("paginationModel", WebUtils.getPaginationModel(departments));
        return "department/list";
    }

    @GetMapping("/view/{id}")
    @PreAuthorize("hasAnyAuthority('" + UserRoles.ROLE_DEPARTMENT_VIEW + "')")
    public String view(@PathVariable(name = "id") final Integer id, final Model model) {
        model.addAttribute("department", departmentService.get(id));
        return "department/view";
    }

    @GetMapping("/edit/{id}")
    @PreAuthorize("hasAnyAuthority('" + UserRoles.ROLE_DEPARTMENT_VIEW + "')")
    public String edit(@PathVariable(name = "id") final Integer id, final Model model) {
        model.addAttribute("department", departmentService.get(id));
        return "department/edit";
    }

    @GetMapping("/add")
    @PreAuthorize("hasAnyAuthority('" + UserRoles.ROLE_DEPARTMENT_MANAGE + "')")
    public String add(@ModelAttribute("department") final DepartmentDTO departmentDTO) {
        return "department/add";
    }

    @PostMapping("/add")
    @PreAuthorize("hasAnyAuthority('" + UserRoles.ROLE_DEPARTMENT_MANAGE + "')")
    public String add(@ModelAttribute("department") @Valid final DepartmentDTO departmentDTO,
                      final BindingResult bindingResult, final RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            return "department/add";
        }
        departmentService.create(departmentDTO);
        FlashMessages.createSuccess(redirectAttributes, ENTITY_NAME);
        return "redirect:/departments";
    }

    @PostMapping("/edit/{id}")
    @PreAuthorize("hasAnyAuthority('" + UserRoles.ROLE_DEPARTMENT_MANAGE + "')")
    public String edit(@PathVariable(name = "id") final Integer id,
                       @ModelAttribute("department") @Valid final DepartmentDTO departmentDTO,
                       final BindingResult bindingResult, final RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            return "department/edit";
        }
        departmentService.update(id, departmentDTO);
        FlashMessages.updateSuccess(redirectAttributes, ENTITY_NAME);
        return "redirect:/departments";
    }

    @PostMapping("/delete/{id}")
    @PreAuthorize("hasAnyAuthority('" + UserRoles.ROLE_DEPARTMENT_MANAGE + "')")
    public String delete(@PathVariable(name = "id") final Integer id,
                         final RedirectAttributes redirectAttributes) {
        final ReferencedWarning referencedWarning = departmentService.getReferencedWarning(id);
        if (referencedWarning != null) {
            redirectAttributes.addFlashAttribute(WebUtils.MSG_ERROR,
                    WebUtils.getMessage(referencedWarning.getKey(), referencedWarning.getParams().toArray()));
        } else {
            departmentService.delete(id);
            FlashMessages.deleteSuccess(redirectAttributes, ENTITY_NAME);
        }
        return "redirect:/departments";
    }

}
