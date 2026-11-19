package br.dev.ampliar.caipora.controller;

import br.dev.ampliar.caipora.domain.Department;
import br.dev.ampliar.caipora.domain.Role;
import br.dev.ampliar.caipora.model.*;
import br.dev.ampliar.caipora.repos.DepartmentRepository;
import br.dev.ampliar.caipora.repos.RoleRepository;
import br.dev.ampliar.caipora.service.UserService;
import br.dev.ampliar.caipora.service.UserSessionService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.core.GrantedAuthority;
import br.dev.ampliar.caipora.util.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.Map.entry;


@SuppressWarnings("SameReturnValue")
@Controller
@RequestMapping("/users")
public class UserController {

    private static final String ENTITY_NAME = "User";
    private static final String CONTROLLER_ADD = "user/add";
    private static final String CONTROLLER_EDIT = "user/edit";
    private static final String CONTROLLER_VIEW = "user/view";
    private static final String CONTROLLER_LIST = "user/list";
    private static final String REDIRECT_TO_CONTROLLER_INDEX = "redirect:/users";
    private static final String CANNOT_MANAGE =
            "You cannot modify this user: the account has permissions that you don't have.";
    private static final Map<String, String> SORTABLE_COLUMNS = Map.ofEntries(
            entry("id", "sortById"),
            entry("name", "sortByName"),
            entry("email", "sortByEmail"),
            entry("gender", "sortByGender"),
            entry("username", "sortByUsername"),
            entry("enabled", "sortByEnabled"),
            entry("department.name", "sortByDepartmentName"));

    private final UserService userService;
    private final RoleRepository roleRepository;
    private final DepartmentRepository departmentRepository;
    private final UserSessionService userSessionService;
    private final SortUtils sortUtils = new SortUtils();

    public UserController(final UserService userService,
                          final DepartmentRepository departmentRepository,
                          final RoleRepository roleRepository,
                          final UserSessionService userSessionService) {
        this.userService = userService;
        this.departmentRepository = departmentRepository;
        this.roleRepository = roleRepository;
        this.userSessionService = userSessionService;
    }

    @ModelAttribute
    public void prepareContext(final Model model) {
        model.addAttribute("genderValues", Gender.values());
        model.addAttribute("departmentValues", departmentRepository.findAll(Sort.by("name"))
                .stream()
                .collect(CustomCollectors.toSortedMap(Department::getId, Department::getName)));
        model.addAttribute("rolesValues", roleRepository.findAll(Sort.by("name"))
                .stream()
                .collect(CustomCollectors.toSortedMap(Role::getId, Role::getName)));
        model.addAttribute("currentUserId", SecurityUtils.currentUserId());
        model.addAttribute("grantableRoles", grantableRoleIds());
    }

    /**
     * @return ids of the roles held by the current user (the only ones they may grant or revoke)
     */
    private Set<Integer> grantableRoleIds() {
        final Set<String> authorities = SecurityUtils.currentUser()
                .map(user -> user.getAuthorities().stream()
                        .map(GrantedAuthority::getAuthority)
                        .collect(Collectors.toSet()))
                .orElse(Set.of());
        return roleRepository.findAll().stream()
                .filter(role -> authorities.contains(role.getName()))
                .map(Role::getId)
                .collect(Collectors.toSet());
    }

    /**
     * Applies {@link RoleGrantPolicy} to the submitted roles. Returns false (with a field error) when the request
     * tries to grant roles the current user doesn't hold.
     */
    private boolean applyRolePolicy(final UserDTO userDTO, final List<Integer> existingRoles, final boolean self,
                                    final BindingResult bindingResult) {
        try {
            userDTO.setRoles(RoleGrantPolicy.resolve(userDTO.getRoles(), existingRoles, grantableRoleIds(), self));
            return true;
        } catch (IllegalArgumentException e) {
            bindingResult.rejectValue("roles", "roles.notGrantable", e.getMessage());
            return false;
        }
    }

    private boolean canManage(final UserDTO target) {
        return isCurrentUser(target.getId()) || RoleGrantPolicy.canManage(target.getRoles(), grantableRoleIds());
    }

    private static String currentSessionId(final HttpServletRequest request) {
        final HttpSession session = request.getSession(false);
        return session == null ? null : session.getId();
    }

    @GetMapping
    @PreAuthorize("hasAnyAuthority('" + UserRoles.ROLE_USER_VIEW + "')")
    public String list(@ModelAttribute("userSearch") final UserSearchDTO filter,
                       @RequestParam(name = "sort", required = false) final String sort,
                       @PageableDefault(size = 20) final Pageable pageable,
                       final Model model) {
        final var sortOrder = sortUtils.addSortAttributesToModel(model, sort, "id,desc", SORTABLE_COLUMNS);
        final var pageRequest = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), sortOrder);
        final Page<UserDTO> users = userService.findAll(filter, pageRequest);
        model.addAttribute("users", users);
        // only offer Edit/Delete for accounts the current user is allowed to manage
        final Set<Integer> grantable = grantableRoleIds();
        final Integer currentUserId = SecurityUtils.currentUserId();
        final Set<Integer> manageable = userService.getRoleIds(users.map(UserDTO::getId).getContent()).entrySet().stream()
                .filter(entry -> entry.getKey().equals(currentUserId)
                        || RoleGrantPolicy.canManage(entry.getValue(), grantable))
                .map(Map.Entry::getKey)
                .collect(Collectors.toSet());
        model.addAttribute("manageableUserIds", manageable);
        model.addAttribute("paginationModel", WebUtils.getPaginationModel(users));
        return CONTROLLER_LIST;
    }

    @GetMapping("/view/{id}")
    @PreAuthorize("hasAnyAuthority('" + UserRoles.ROLE_USER_VIEW + "')")
    public String view(@PathVariable(name = "id") final Integer id, final Model model) {
        final UserDTO user = userService.get(id);
        model.addAttribute("user", user);
        model.addAttribute("canManage", canManage(user));
        return CONTROLLER_VIEW;
    }

    @GetMapping("/add")
    @PreAuthorize("hasAnyAuthority('" + UserRoles.ROLE_USER_MANAGE + "')")
    public String add(@ModelAttribute("user") final UserDTO userDTO) {
        userDTO.setEnabled(true);
        return CONTROLLER_ADD;
    }

    @PostMapping("/add")
    @PreAuthorize("hasAnyAuthority('" + UserRoles.ROLE_USER_MANAGE + "')")
    public String add(@ModelAttribute("user") @Validated(OnCreate.class) final UserDTO userDTO,
            final BindingResult bindingResult, final RedirectAttributes redirectAttributes) {
        applyRolePolicy(userDTO, List.of(), false, bindingResult);
        if (bindingResult.hasErrors()) {
            userDTO.setPassword(null);
            return CONTROLLER_ADD;
        }
        userService.create(userDTO);
        FlashMessages.createSuccess(redirectAttributes, ENTITY_NAME);
        return REDIRECT_TO_CONTROLLER_INDEX;
    }

    @GetMapping("/edit/{id}")
    @PreAuthorize("hasAnyAuthority('" + UserRoles.ROLE_USER_MANAGE + "')")
    public String edit(@PathVariable(name = "id") final Integer id, final Model model,
                       final RedirectAttributes redirectAttributes) {
        final UserDTO user = userService.get(id);
        if (!canManage(user)) {
            FlashMessages.error(redirectAttributes, CANNOT_MANAGE);
            return REDIRECT_TO_CONTROLLER_INDEX;
        }
        model.addAttribute("user", user);
        return CONTROLLER_EDIT;
    }

    @PostMapping("/edit/{id}")
    @PreAuthorize("hasAnyAuthority('" + UserRoles.ROLE_USER_MANAGE + "')")
    public String edit(@PathVariable(name = "id") final Integer id,
                       @ModelAttribute("user") @Validated(OnUpdate.class) final UserDTO userDTO,
                       final BindingResult bindingResult,
                       final RedirectAttributes redirectAttributes,
                       final HttpServletRequest request) {
        final UserDTO existing = userService.get(id);
        if (!canManage(existing)) {
            FlashMessages.error(redirectAttributes, CANNOT_MANAGE);
            return REDIRECT_TO_CONTROLLER_INDEX;
        }
        final boolean self = isCurrentUser(id);
        applyRolePolicy(userDTO, existing.getRoles(), self, bindingResult);
        if (self && Boolean.FALSE.equals(userDTO.getEnabled())) {
            bindingResult.rejectValue("enabled", "user.cannotDisableSelf", "You cannot disable your own account.");
        }
        if (bindingResult.hasErrors()) {
            userDTO.setId(id);
            userDTO.setPassword(null);
            return CONTROLLER_EDIT;
        }
        userService.update(id, userDTO);
        // apply the changes (status, password, roles) to the sessions of that user right away;
        // when editing one's own account, the current session is kept
        final String keep = self ? currentSessionId(request) : null;
        userSessionService.invalidateSessions(existing.getUsername(), keep);
        if (!existing.getUsername().equalsIgnoreCase(userDTO.getUsername())) {
            userSessionService.invalidateSessions(userDTO.getUsername(), keep);
        }
        FlashMessages.updateSuccess(redirectAttributes, ENTITY_NAME);
        return REDIRECT_TO_CONTROLLER_INDEX;
    }

    @PostMapping("/delete/{id}")
    @PreAuthorize("hasAnyAuthority('" + UserRoles.ROLE_USER_MANAGE + "')")
    public String delete(@PathVariable(name = "id") final Integer id,
            final RedirectAttributes redirectAttributes) {
        if (isCurrentUser(id)) {
            FlashMessages.error(redirectAttributes, "You cannot delete your own account.");
            return REDIRECT_TO_CONTROLLER_INDEX;
        }
        final UserDTO existing = userService.get(id);
        if (!canManage(existing)) {
            FlashMessages.error(redirectAttributes, CANNOT_MANAGE);
            return REDIRECT_TO_CONTROLLER_INDEX;
        }
        final ReferencedWarning referencedWarning = userService.getReferencedWarning(id);
        if (referencedWarning != null) {
            FlashMessages.referencedWarning(redirectAttributes, referencedWarning);
        } else {
            userService.delete(id);
            userSessionService.invalidateSessions(existing.getUsername(), null);
            FlashMessages.deleteSuccess(redirectAttributes, ENTITY_NAME);
        }
        return REDIRECT_TO_CONTROLLER_INDEX;
    }

    private static boolean isCurrentUser(final Integer id) {
        return Objects.equals(id, SecurityUtils.currentUserId());
    }
}
