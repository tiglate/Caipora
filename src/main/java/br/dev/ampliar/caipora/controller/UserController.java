package br.dev.ampliar.caipora.controller;

import br.dev.ampliar.caipora.domain.Department;
import br.dev.ampliar.caipora.domain.Role;
import br.dev.ampliar.caipora.model.*;
import br.dev.ampliar.caipora.repos.DepartmentRepository;
import br.dev.ampliar.caipora.repos.RoleRepository;
import br.dev.ampliar.caipora.service.UserService;
import br.dev.ampliar.caipora.util.*;
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
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Map;

import static java.util.Map.entry;


@Controller
@RequestMapping("/users")
public class UserController {

    private static final String ENTITY_NAME = "User";
    private static final String CONTROLLER_ADD = "user/add";
    private static final String CONTROLLER_EDIT = "user/edit";
    private static final String CONTROLLER_VIEW = "user/view";
    private static final String CONTROLLER_LIST = "user/list";
    private static final String REDIRECT_TO_CONTROLLER_INDEX = "redirect:/users";
    private final UserService userService;
    private final RoleRepository roleRepository;
    private final DepartmentRepository departmentRepository;
    private final SortUtils sortUtils;

    public UserController(final UserService userService,
                          final DepartmentRepository departmentRepository,
                          final RoleRepository roleRepository) {
        this.userService = userService;
        this.departmentRepository = departmentRepository;
        this.roleRepository = roleRepository;
        this.sortUtils = new SortUtils();
    }

    @ModelAttribute
    public void prepareContext(final Model model) {
        model.addAttribute("genderValues", Gender.values());
        model.addAttribute("departmentValues", departmentRepository.findAll(Sort.by("id"))
                .stream()
                .collect(CustomCollectors.toSortedMap(Department::getId, Department::getName)));
        model.addAttribute("rolesValues", roleRepository.findAll(Sort.by("id"))
                .stream()
                .collect(CustomCollectors.toSortedMap(Role::getId, Role::getName)));
    }

    @SuppressWarnings("SameReturnValue")
    @GetMapping
    @PreAuthorize("hasAnyAuthority('" + UserRoles.ROLE_USER_VIEW + "')")
    public String list(@ModelAttribute("userSearch") UserSearchDTO filter,
                       @RequestParam(name = "sort", required = false) String sort,
                       @SortDefault(sort = "id", direction = Sort.Direction.DESC) @PageableDefault(size = 20) final Pageable pageable,
                       final Model model) {
        if (sort == null) {
            sort = "id,desc";
        }
        final var sortOrder = this.sortUtils.addSortAttributesToModel(model, sort, pageable, Map.ofEntries(
                entry("id", "sortById"),
                entry("name", "sortByName"),
                entry("email", "sortByEmail"),
                entry("gender", "sortByGender"),
                entry("username", "sortByUsername"),
                entry("enabled", "sortByEnabled"),
                entry("department.name", "sortByDepartmentName")
        ));
        final var pageRequest = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), sortOrder);
        final Page<UserDTO> users = userService.findAll(filter, pageRequest);
        model.addAttribute("users", users);
        model.addAttribute("filter", filter);
        model.addAttribute("paginationModel", WebUtils.getPaginationModel(users));
        return CONTROLLER_LIST;
    }

    @SuppressWarnings("SameReturnValue")
    @GetMapping("/view/{id}")
    @PreAuthorize("hasAnyAuthority('" + UserRoles.ROLE_USER_VIEW + "')")
    public String view(@PathVariable(name = "id") final Integer id, final Model model) {
        model.addAttribute("user", userService.get(id));
        return CONTROLLER_VIEW;
    }

    @SuppressWarnings("SameReturnValue")
    @GetMapping("/add")
    @PreAuthorize("hasAnyAuthority('" + UserRoles.ROLE_USER_MANAGE + "')")
    public String add(@ModelAttribute("user") final UserDTO userDTO) {
        userDTO.setGender(Gender.MALE);
        userDTO.setEnabled(true);
        return CONTROLLER_ADD;
    }

    @PostMapping("/add")
    @PreAuthorize("hasAnyAuthority('" + UserRoles.ROLE_USER_MANAGE + "')")
    public String add(@ModelAttribute("user") @Validated(OnCreate.class) final UserDTO userDTO,
            final BindingResult bindingResult, final RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            return CONTROLLER_ADD;
        }
        userService.create(userDTO);
        FlashMessages.createSuccess(redirectAttributes, ENTITY_NAME);
        return REDIRECT_TO_CONTROLLER_INDEX;
    }

    @SuppressWarnings("SameReturnValue")
    @GetMapping("/edit/{id}")
    @PreAuthorize("hasAnyAuthority('" + UserRoles.ROLE_USER_MANAGE + "')")
    public String edit(@PathVariable(name = "id") final Integer id, final Model model) {
        model.addAttribute("user", userService.get(id));
        return CONTROLLER_EDIT;
    }

    @PostMapping("/edit/{id}")
    @PreAuthorize("hasAnyAuthority('" + UserRoles.ROLE_USER_MANAGE + "')")
    public String edit(@PathVariable(name = "id") final Integer id,
                       @ModelAttribute("user") @Validated(OnUpdate.class) final UserDTO userDTO,
                       final BindingResult bindingResult,
                       final RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            return CONTROLLER_EDIT;
        }
        userService.update(id, userDTO);
        FlashMessages.updateSuccess(redirectAttributes, ENTITY_NAME);
        return REDIRECT_TO_CONTROLLER_INDEX;
    }

    @SuppressWarnings("SameReturnValue")
    @PostMapping("/delete/{id}")
    @PreAuthorize("hasAnyAuthority('" + UserRoles.ROLE_USER_MANAGE + "')")
    public String delete(@PathVariable(name = "id") final Integer id,
            final RedirectAttributes redirectAttributes) {
        final ReferencedWarning referencedWarning = userService.getReferencedWarning(id);
        if (referencedWarning != null) {
            redirectAttributes.addFlashAttribute(FlashMessages.MSG_ERROR,
                    WebUtils.getMessage(referencedWarning.getKey(), referencedWarning.getParams().toArray()));
        } else {
            userService.delete(id);
            FlashMessages.deleteSuccess(redirectAttributes, ENTITY_NAME);
        }
        return REDIRECT_TO_CONTROLLER_INDEX;
    }
}