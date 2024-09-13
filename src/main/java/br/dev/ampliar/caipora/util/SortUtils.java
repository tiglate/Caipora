package br.dev.ampliar.caipora.util;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.ui.Model;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.util.Map;

public class SortUtils {

    public static Sort addSortAttributesToModel(Model model, String sort, Pageable pageable, Map<String, String> sortAttributes) {
        Sort sortOrder = Sort.unsorted();
        if (sort != null && !sort.isEmpty()) {
            String[] sortParts = sort.split(",");
            Sort.Direction direction = sortParts[1].equalsIgnoreCase("asc") ? Sort.Direction.ASC : Sort.Direction.DESC;
            sortOrder = Sort.by(direction, sortParts[0]);
        }
        for (Map.Entry<String, String> entry : sortAttributes.entrySet()) {
            String property = entry.getKey();
            String attributeName = entry.getValue();
            String sortLink = buildSortLink(property, sort, pageable);
            String sortDirection = getSortDirection(property, sort);
            model.addAttribute(attributeName + "Link", sortLink);
            model.addAttribute(attributeName + "Direction", sortDirection);
        }
        return sortOrder;
    }

    private static String buildSortLink(String property, String currentSort, Pageable pageable) {
        String direction = "asc";
        if (currentSort != null && currentSort.startsWith(property + ",")) {
            direction = currentSort.endsWith(",asc") ? "desc" : "asc";
        }
        return ServletUriComponentsBuilder.fromCurrentRequest()
                .replaceQueryParam("sort", property + "," + direction)
                .replaceQueryParam("page", pageable.getPageNumber())
                .toUriString();
    }

    private static String getSortDirection(String property, String currentSort) {
        if (currentSort != null && currentSort.startsWith(property + ",")) {
            return currentSort.endsWith(",asc") ? "desc" : "asc";
        }
        return "asc"; // Default to ascending if not sorted
    }
}