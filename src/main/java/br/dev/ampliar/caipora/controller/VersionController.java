package br.dev.ampliar.caipora.controller;

import br.dev.ampliar.caipora.domain.Software;
import br.dev.ampliar.caipora.model.VersionDTO;
import br.dev.ampliar.caipora.repos.SoftwareRepository;
import br.dev.ampliar.caipora.service.VersionService;
import br.dev.ampliar.caipora.util.*;
import jakarta.validation.Valid;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static java.util.Map.entry;


@SuppressWarnings("SameReturnValue")
@Controller
@RequestMapping("/versions")
public class VersionController {

    private static final String ENTITY_NAME = "Version";
    private static final String CONTROLLER_ADD = "version/add";
    private static final String CONTROLLER_EDIT = "version/edit";
    private static final String CONTROLLER_VIEW = "version/view";
    private static final String CONTROLLER_LIST = "version/list";
    private static final String REDIRECT_TO_CONTROLLER_INDEX = "redirect:/versions";
    private static final Map<String, String> SORTABLE_COLUMNS = Map.ofEntries(
            entry("id", "sortById"),
            entry("name", "sortByName"),
            entry("releaseDate", "sortByReleaseDate"),
            entry("software.name", "sortBySoftware"));

    private final VersionService versionService;
    private final SoftwareRepository softwareRepository;
    private final SortUtils sortUtils = new SortUtils();

    public VersionController(final VersionService versionService,
                             final SoftwareRepository softwareRepository) {
        this.versionService = versionService;
        this.softwareRepository = softwareRepository;
    }

    @ModelAttribute
    public void prepareContext(final Model model) {
        model.addAttribute("softwareValues", softwareRepository.findAll(Sort.by("name"))
                .stream()
                .collect(CustomCollectors.toSortedMap(Software::getId, Software::getName)));
    }

    @GetMapping
    @PreAuthorize("hasAnyAuthority('" + UserRoles.ROLE_VERSION_VIEW + "')")
    public String list(@ModelAttribute("versionSearch") final VersionDTO filter,
                       @RequestParam(name = "sort", required = false) final String sort,
                       @PageableDefault(size = 20) final Pageable pageable,
                       final Model model) {
        final var sortOrder = sortUtils.addSortAttributesToModel(model, sort, "id,desc", SORTABLE_COLUMNS);
        final var pageRequest = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), sortOrder);
        final var versions = versionService.findAll(filter, pageRequest);
        model.addAttribute("versions", versions);
        model.addAttribute("paginationModel", WebUtils.getPaginationModel(versions));
        return CONTROLLER_LIST;
    }

    @GetMapping("/view/{id}")
    @PreAuthorize("hasAnyAuthority('" + UserRoles.ROLE_VERSION_VIEW + "')")
    public String view(@PathVariable(name = "id") final Integer id, final Model model) {
        model.addAttribute("version", versionService.get(id));
        return CONTROLLER_VIEW;
    }

    @GetMapping("/add")
    @PreAuthorize("hasAnyAuthority('" + UserRoles.ROLE_VERSION_MANAGE + "')")
    public String add(@ModelAttribute("version") final VersionDTO versionDTO, final Model model) {
        if (versionDTO.getReleaseDate() == null) {
            versionDTO.setReleaseDate(LocalDate.now());
        }
        addPackagesToModel(versionDTO.getSoftwareId(), model);
        return CONTROLLER_ADD;
    }

    @PostMapping("/add")
    @PreAuthorize("hasAnyAuthority('" + UserRoles.ROLE_VERSION_MANAGE + "')")
    public String add(@ModelAttribute("version") @Valid final VersionDTO versionDTO,
                      final BindingResult bindingResult, final Model model,
                      final RedirectAttributes redirectAttributes) {
        if (versionDTO.getFileName() == null || versionDTO.getFileName().isBlank()) {
            bindingResult.rejectValue("fileName", "NotNull", "Please select the package.");
        }
        if (!bindingResult.hasErrors()) {
            try {
                versionService.create(versionDTO);
                FlashMessages.createSuccess(redirectAttributes, ENTITY_NAME);
                return REDIRECT_TO_CONTROLLER_INDEX;
            } catch (IllegalArgumentException e) {
                bindingResult.rejectValue("fileName", "version.package.invalid", e.getMessage());
            }
        }
        addPackagesToModel(versionDTO.getSoftwareId(), model);
        return CONTROLLER_ADD;
    }

    /**
     * Fragment with the packages of the selected application, refreshed by htmx when the application changes.
     */
    @GetMapping("/packages")
    @PreAuthorize("hasAnyAuthority('" + UserRoles.ROLE_VERSION_MANAGE + "')")
    public String packages(@ModelAttribute("version") final VersionDTO versionDTO, final Model model) {
        addPackagesToModel(versionDTO.getSoftwareId(), model);
        return "version/_form :: packageField";
    }

    private void addPackagesToModel(final Integer softwareId, final Model model) {
        if (softwareId == null || softwareRepository.findById(softwareId).isEmpty()) {
            model.addAttribute("packageValues", List.of());
            return;
        }
        model.addAttribute("packageValues", versionService.getAvailablePackages(softwareId));
        final Path folder = versionService.getPackagesFolder(softwareId);
        model.addAttribute("packagesFolder", folder == null ? null : folder.toString());
    }

    @GetMapping("/edit/{id}")
    @PreAuthorize("hasAnyAuthority('" + UserRoles.ROLE_VERSION_MANAGE + "')")
    public String edit(@PathVariable(name = "id") final Integer id, final Model model) {
        model.addAttribute("version", versionService.get(id));
        return CONTROLLER_EDIT;
    }

    @PostMapping("/edit/{id}")
    @PreAuthorize("hasAnyAuthority('" + UserRoles.ROLE_VERSION_MANAGE + "')")
    public String edit(@PathVariable(name = "id") final Integer id,
                       @ModelAttribute("version") @Valid final VersionDTO versionDTO,
                       final BindingResult bindingResult, final RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            versionDTO.setId(id);
            return CONTROLLER_EDIT;
        }
        versionService.update(id, versionDTO);
        FlashMessages.updateSuccess(redirectAttributes, ENTITY_NAME);
        return REDIRECT_TO_CONTROLLER_INDEX;
    }

    @PostMapping("/delete/{id}")
    @PreAuthorize("hasAnyAuthority('" + UserRoles.ROLE_VERSION_MANAGE + "')")
    public String delete(@PathVariable(name = "id") final Integer id,
                         final RedirectAttributes redirectAttributes) {
        final ReferencedWarning referencedWarning = versionService.getReferencedWarning(id);
        if (referencedWarning != null) {
            FlashMessages.referencedWarning(redirectAttributes, referencedWarning);
            return REDIRECT_TO_CONTROLLER_INDEX;
        }
        versionService.delete(id);
        FlashMessages.deleteSuccess(redirectAttributes, ENTITY_NAME);
        return REDIRECT_TO_CONTROLLER_INDEX;
    }

    @GetMapping("/download/{id}")
    @PreAuthorize("hasAnyAuthority('" + UserRoles.ROLE_VERSION_MANAGE + "')")
    public ResponseEntity<Resource> downloadFile(@PathVariable("id") final Integer id) {
        final Path packageFile = versionService.getPackageFile(id);
        final ContentDisposition contentDisposition = ContentDisposition.attachment()
                .filename(packageFile.getFileName().toString(), StandardCharsets.UTF_8)
                .build();
        // Always served as a binary download, never rendered inline by the browser.
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION, contentDisposition.toString())
                .header("X-Content-Type-Options", "nosniff")
                .body(new FileSystemResource(packageFile));
    }
}
