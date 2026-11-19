package br.dev.ampliar.caipora.util;

import org.springframework.data.domain.Sort;
import org.springframework.ui.Model;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Map;
import java.util.function.Supplier;

/**
 * Helper for sortable list pages.
 * <p>
 * This class is <strong>stateless</strong>: a fresh URI builder is obtained for every link, so it can safely be
 * shared by a singleton controller across threads and users. The requested sort property is validated against the
 * whitelist of sortable properties to avoid errors (or probing of the entity model) through the {@code sort}
 * query parameter.
 */
public class SortUtils {

    private static final String ASC = "asc";
    private static final String DESC = "desc";

    private final Supplier<UriComponentsBuilder> uriBuilderSupplier;

    public SortUtils() {
        this(WebUtils::currentRequestUriBuilder);
    }

    public SortUtils(final Supplier<UriComponentsBuilder> uriBuilderSupplier) {
        this.uriBuilderSupplier = uriBuilderSupplier;
    }

    /**
     * Resolves the requested sort order and publishes, for each sortable property, a link that toggles the sort
     * ({@code <attribute>Link}) and the current direction of that column ({@code <attribute>Direction}: {@code asc},
     * {@code desc} or {@code null} when the column is not the active sort).
     *
     * @param model          the MVC model
     * @param sort           raw value of the {@code sort} request parameter, e.g. {@code "name,asc"} (may be null)
     * @param defaultSort    sort used when {@code sort} is missing or invalid, e.g. {@code "id,desc"}
     * @param sortAttributes map of sortable property to model attribute prefix
     * @return the resolved {@link Sort}
     */
    public Sort addSortAttributesToModel(final Model model, final String sort, final String defaultSort,
                                         final Map<String, String> sortAttributes) {
        SortSpec current = SortSpec.parse(sort, sortAttributes);
        if (current == null) {
            current = SortSpec.parse(defaultSort, sortAttributes);
        }
        final SortSpec active = current;
        sortAttributes.forEach((property, attributeName) -> {
            final boolean isActive = active != null && active.property().equals(property);
            final String nextDirection = isActive && ASC.equals(active.direction()) ? DESC : ASC;
            model.addAttribute(attributeName + "Link", buildSortLink(property, nextDirection));
            model.addAttribute(attributeName + "Direction", isActive ? active.direction() : null);
        });
        if (active == null) {
            return Sort.unsorted();
        }
        return Sort.by(DESC.equals(active.direction()) ? Sort.Direction.DESC : Sort.Direction.ASC, active.property());
    }

    private String buildSortLink(final String property, final String direction) {
        return uriBuilderSupplier.get()
                .replaceQueryParam("sort", property + "," + direction)
                // changing the order brings the user back to the first page
                .replaceQueryParam("page")
                .build()
                .toUriString();
    }

    private record SortSpec(String property, String direction) {

        static SortSpec parse(final String sort, final Map<String, String> allowed) {
            if (sort == null || sort.isBlank()) {
                return null;
            }
            final String[] parts = sort.split(",", -1);
            final String property = parts[0].trim();
            if (!allowed.containsKey(property)) {
                return null;
            }
            final String direction = parts.length > 1 && DESC.equalsIgnoreCase(parts[1].trim()) ? DESC : ASC;
            return new SortSpec(property, direction);
        }
    }
}
