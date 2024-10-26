package br.dev.ampliar.caipora.controller;

import br.dev.ampliar.caipora.domain.Software;
import br.dev.ampliar.caipora.model.VersionDTO;
import br.dev.ampliar.caipora.repos.SoftwareRepository;
import br.dev.ampliar.caipora.service.VersionService;
import br.dev.ampliar.caipora.util.*;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.SortDefault;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

import static java.util.Map.entry;


@Controller
@RequestMapping("/versions")
public class VersionController {

    private static final String ENTITY_NAME = "Version";
    private static final String REDIRECT_TO_CONTROLLER_INDEX = "redirect:/versions";
    private final VersionService versionService;
    private final SoftwareRepository softwareRepository;
    private final String uploadDirectory;

    public VersionController(final VersionService versionService,
                             final SoftwareRepository softwareRepository,
                             @Value("${file.upload.directory}") String uploadDirectory) {
        this.versionService = versionService;
        this.softwareRepository = softwareRepository;
        this.uploadDirectory = uploadDirectory;
    }

    @ModelAttribute
    public void prepareContext(final Model model) {
        model.addAttribute("softwareValues", softwareRepository.findAll(Sort.by("id"))
                .stream()
                .collect(CustomCollectors.toSortedMap(Software::getId, Software::getName)));
    }

    @SuppressWarnings("SameReturnValue")
    @GetMapping
    @PreAuthorize("hasAnyAuthority('" + UserRoles.ROLE_VERSION_VIEW + "')")
    public String list(@ModelAttribute("versionSearch") VersionDTO filter,
                       @RequestParam(name = "sort", required = false) String sort,
                       @SortDefault(sort = "id", direction = Sort.Direction.DESC) @PageableDefault(size = 20) final Pageable pageable,
                       final Model model) {
        if (sort == null) {
            sort = "id,desc";
        }
        final var sortOrder = SortUtils.addSortAttributesToModel(model, sort, pageable, Map.ofEntries(
                entry("id", "sortById"),
                entry("name", "sortByName"),
                entry("releaseDate", "sortByReleaseDate"),
                entry("software.name", "sortBySoftware")
        ));
        final var pageRequest = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), sortOrder);
        final var versions = versionService.findAll(filter, pageRequest);
        model.addAttribute("versions", versions);
        model.addAttribute("filter", filter);
        model.addAttribute("paginationModel", WebUtils.getPaginationModel(versions));
        return "version/list";
    }

    @SuppressWarnings("SameReturnValue")
    @GetMapping("/view/{id}")
    @PreAuthorize("hasAnyAuthority('" + UserRoles.ROLE_VERSION_VIEW + "')")
    public String view(@PathVariable(name = "id") final Integer id, final Model model) {
        model.addAttribute("version", versionService.get(id));
        return "version/view";
    }

    @SuppressWarnings("SameReturnValue")
    @GetMapping("/add")
    @PreAuthorize("hasAnyAuthority('" + UserRoles.ROLE_VERSION_MANAGE + "')")
    public String add(@ModelAttribute("version") final VersionDTO versionDTO) {
        return "version/add";
    }

    @PostMapping("/add")
    @PreAuthorize("hasAnyAuthority('" + UserRoles.ROLE_VERSION_MANAGE + "')")
    public String add(@ModelAttribute("version") @Valid final VersionDTO versionDTO,
                      final BindingResult bindingResult, final RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            return "version/add";
        }
        try {
            versionService.create(versionDTO);
            FlashMessages.createSuccess(redirectAttributes, ENTITY_NAME);
        } catch (IllegalArgumentException e) {
            bindingResult.rejectValue("file", "error.file", e.getMessage());
            return "version/add";
        } catch (RuntimeException e) {
            FlashMessages.error(redirectAttributes, e);
            return "version/add";
        }
        return REDIRECT_TO_CONTROLLER_INDEX;
    }

    @SuppressWarnings("SameReturnValue")
    @GetMapping("/edit/{id}")
    @PreAuthorize("hasAnyAuthority('" + UserRoles.ROLE_VERSION_MANAGE + "')")
    public String edit(@PathVariable(name = "id") final Integer id, final Model model) {
        model.addAttribute("version", versionService.get(id));
        return "version/edit";
    }

    @PostMapping("/edit/{id}")
    @PreAuthorize("hasAnyAuthority('" + UserRoles.ROLE_VERSION_MANAGE + "')")
    public String edit(@PathVariable(name = "id") final Integer id,
                       @ModelAttribute("version") @Valid final VersionDTO versionDTO,
                       final BindingResult bindingResult, final RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            return "version/edit";
        }
        versionService.update(id, versionDTO);
        FlashMessages.updateSuccess(redirectAttributes, ENTITY_NAME);
        return REDIRECT_TO_CONTROLLER_INDEX;
    }

    @SuppressWarnings("SameReturnValue")
    @PostMapping("/delete/{id}")
    @PreAuthorize("hasAnyAuthority('" + UserRoles.ROLE_VERSION_MANAGE + "')")
    public String delete(@PathVariable(name = "id") final Integer id,
                         final RedirectAttributes redirectAttributes) {
        final ReferencedWarning referencedWarning = versionService.getReferencedWarning(id);
        if (referencedWarning != null) {
            redirectAttributes.addFlashAttribute(WebUtils.MSG_ERROR,
                    WebUtils.getMessage(referencedWarning.getKey(), referencedWarning.getParams().toArray()));
        } else {
            versionService.delete(id);
            FlashMessages.deleteSuccess(redirectAttributes, ENTITY_NAME);
        }
        return REDIRECT_TO_CONTROLLER_INDEX;
    }

    @GetMapping("/download/{id}")
    @PreAuthorize("hasAnyAuthority('" + UserRoles.ROLE_VERSION_MANAGE + "')")
    public ResponseEntity<Resource> downloadFile(@PathVariable("id") Integer id) throws IOException {
        VersionDTO versionDTO = versionService.get(id);
        Path filePath = Paths.get(uploadDirectory, versionDTO.getFileName());

        Resource resource = new UrlResource(filePath.toUri());

        if (resource.exists() && resource.isReadable()) {
            String contentType = Files.probeContentType(filePath);

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
                    .body(resource);
        } else {
            throw new FileNotFoundException("File not found");
        }
    }
}