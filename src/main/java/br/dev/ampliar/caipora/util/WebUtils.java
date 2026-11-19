package br.dev.ampliar.caipora.util;

import br.dev.ampliar.caipora.model.PaginationModel;
import br.dev.ampliar.caipora.model.PaginationStep;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.ArrayList;
import java.util.Objects;


@Component
public class WebUtils {

    private static MessageSource messageSource;
    private static LocaleResolver localeResolver;

    public WebUtils(final MessageSource messageSource, final LocaleResolver localeResolver) {
        WebUtils.messageSource = messageSource;
        WebUtils.localeResolver = localeResolver;
    }

    public static HttpServletRequest getRequest() {
        return ((ServletRequestAttributes) Objects.requireNonNull(RequestContextHolder.getRequestAttributes())).getRequest();
    }

    public static String getMessage(final String code, final Object... args) {
        return messageSource.getMessage(code, args, code, localeResolver.resolveLocale(getRequest()));
    }

    /**
     * Creates a <em>relative</em> URI builder (path + query string) for the current request. Relative links are used
     * on purpose: an absolute link would be derived from the client-controlled {@code Host} header.
     * The query string is kept as received (already encoded), so search filters survive pagination and sorting.
     */
    public static UriComponentsBuilder currentRequestUriBuilder() {
        final HttpServletRequest request = getRequest();
        final String path = request.getRequestURI() == null ? "" : request.getRequestURI();
        return UriComponentsBuilder.fromPath(path).query(request.getQueryString());
    }

    private static String getStepUrl(final Page<?> page, final int targetPage) {
        return currentRequestUriBuilder()
                .replaceQueryParam("page", targetPage)
                .replaceQueryParam("size", page.getSize())
                .build()
                .toUriString();
    }

    public static PaginationModel getPaginationModel(final Page<?> page) {
        if (page.isEmpty()) {
            return null;
        }

        final ArrayList<PaginationStep> steps = new ArrayList<>();
        final PaginationStep previous = new PaginationStep();
        previous.setDisabled(!page.hasPrevious());
        previous.setLabel("Previous");
        previous.setUrl(getStepUrl(page, page.previousOrFirstPageable().getPageNumber()));
        steps.add(previous);
        // find a range of up to 5 pages around the current active page
        final int startAt = Math.max(0, Math.min(page.getNumber() - 2, page.getTotalPages() - 5));
        final int endAt = Math.min(startAt + 5, page.getTotalPages());
        for (int i = startAt; i < endAt; i++) {
            final PaginationStep step = new PaginationStep();
            step.setActive(i == page.getNumber());
            step.setLabel(String.valueOf(i + 1));
            step.setUrl(getStepUrl(page, i));
            steps.add(step);
        }
        final PaginationStep next = new PaginationStep();
        next.setDisabled(!page.hasNext());
        next.setLabel("Next");
        next.setUrl(getStepUrl(page, page.nextOrLastPageable().getPageNumber()));
        steps.add(next);

        final long rangeStart = (long) page.getNumber() * page.getSize() + 1L;
        final long rangeEnd = Math.min(rangeStart + page.getSize() - 1, page.getTotalElements());
        final String range = rangeStart == rangeEnd ? String.valueOf(rangeStart) : rangeStart + " - " + rangeEnd;
        final PaginationModel paginationModel = new PaginationModel();
        paginationModel.setSteps(steps);
        paginationModel.setElements("Showing %s of %d".formatted(range, page.getTotalElements()));
        return paginationModel;
    }

}
