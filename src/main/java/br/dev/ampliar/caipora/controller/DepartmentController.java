package br.dev.ampliar.caipora.controller;

import br.dev.ampliar.caipora.model.DepartmentDTO;
import br.dev.ampliar.caipora.service.DepartmentService;
import br.dev.ampliar.caipora.util.ReferencedWarning;
import br.dev.ampliar.caipora.util.SortUtils;
import br.dev.ampliar.caipora.util.UserRoles;
import br.dev.ampliar.caipora.util.WebUtils;
import jakarta.validation.Valid;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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


@SuppressWarnings("SameReturnValue")
@Controller
@RequestMapping("/departments")
public class DepartmentController {

    private final DepartmentService departmentService;

    public DepartmentController(final DepartmentService departmentService) {
        this.departmentService = departmentService;
    }

    @GetMapping
    @PreAuthorize("hasAnyAuthority('" + UserRoles.ADMIN + "', '" + UserRoles.OBSERVER + "')")
    public String list(@ModelAttribute("departmentSearch") DepartmentDTO filter,
                       @RequestParam(name = "sort", required = false) String sort,
                       @SortDefault(sort = "id") @PageableDefault(size = 20) final Pageable pageable,
                       final Model model) {
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

    @GetMapping("/add")
    @PreAuthorize("hasAuthority('" + UserRoles.ADMIN + "')")
    public String add(@ModelAttribute("department") final DepartmentDTO departmentDTO) {
        return "department/add";
    }

    @PostMapping("/add")
    @PreAuthorize("hasAuthority('" + UserRoles.ADMIN + "')")
    public String add(@ModelAttribute("department") @Valid final DepartmentDTO departmentDTO,
                      final BindingResult bindingResult, final RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            return "department/add";
        }
        departmentService.create(departmentDTO);
        redirectAttributes.addFlashAttribute(WebUtils.MSG_SUCCESS, WebUtils.getMessage("department.create.success"));
        return "redirect:/departments";
    }

    @GetMapping("/view/{id}")
    @PreAuthorize("hasAuthority('" + UserRoles.ADMIN + "')")
    public String view(@PathVariable(name = "id") final Integer id, final Model model) {
        model.addAttribute("department", departmentService.get(id));
        return "department/view";
    }

    @GetMapping("/edit/{id}")
    @PreAuthorize("hasAuthority('" + UserRoles.ADMIN + "')")
    public String edit(@PathVariable(name = "id") final Integer id, final Model model) {
        model.addAttribute("department", departmentService.get(id));
        return "department/edit";
    }

    @PostMapping("/edit/{id}")
    @PreAuthorize("hasAuthority('" + UserRoles.ADMIN + "')")
    public String edit(@PathVariable(name = "id") final Integer id,
                       @ModelAttribute("department") @Valid final DepartmentDTO departmentDTO,
                       final BindingResult bindingResult, final RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            return "department/edit";
        }
        departmentService.update(id, departmentDTO);
        redirectAttributes.addFlashAttribute(WebUtils.MSG_SUCCESS, WebUtils.getMessage("department.update.success"));
        return "redirect:/departments";
    }

    @PostMapping("/delete/{id}")
    @PreAuthorize("hasAuthority('" + UserRoles.ADMIN + "')")
    public String delete(@PathVariable(name = "id") final Integer id,
                         final RedirectAttributes redirectAttributes) {
        final ReferencedWarning referencedWarning = departmentService.getReferencedWarning(id);
        if (referencedWarning != null) {
            redirectAttributes.addFlashAttribute(WebUtils.MSG_ERROR,
                    WebUtils.getMessage(referencedWarning.getKey(), referencedWarning.getParams().toArray()));
        } else {
            departmentService.delete(id);
            redirectAttributes.addFlashAttribute(WebUtils.MSG_INFO, WebUtils.getMessage("department.delete.success"));
        }
        return "redirect:/departments";
    }

}
