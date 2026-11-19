package br.dev.ampliar.caipora.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Sort;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.ui.Model;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

class SortUtilsTest {

    private static final Map<String, String> COLUMNS = Map.of(
            "name", "sortByName",
            "id", "sortById");

    private final AtomicReference<String> currentUrl = new AtomicReference<>("/test?name=abc&page=3");
    private SortUtils sortUtils;
    private Model model;

    @BeforeEach
    void setUp() {
        sortUtils = new SortUtils(() -> UriComponentsBuilder.fromUriString(currentUrl.get()));
        model = new ExtendedModelMap();
    }

    @Test
    void activeColumnTogglesDirectionAndKeepsFilters() {
        final Sort sort = sortUtils.addSortAttributesToModel(model, "name,asc", "id,desc", COLUMNS);

        assertEquals(Sort.by(Sort.Direction.ASC, "name"), sort);
        assertEquals("/test?name=abc&sort=name,desc", model.getAttribute("sortByNameLink"));
        assertEquals("asc", model.getAttribute("sortByNameDirection"));
        assertEquals("/test?name=abc&sort=id,asc", model.getAttribute("sortByIdLink"));
        assertNull(model.getAttribute("sortByIdDirection"));
    }

    @Test
    void missingSortFallsBackToDefault() {
        final Sort sort = sortUtils.addSortAttributesToModel(model, null, "id,desc", COLUMNS);

        assertEquals(Sort.by(Sort.Direction.DESC, "id"), sort);
        assertEquals("desc", model.getAttribute("sortByIdDirection"));
    }

    @Test
    void unknownPropertyIsRejected() {
        final Sort sort = sortUtils.addSortAttributesToModel(model, "password,asc", "id,desc", COLUMNS);

        assertEquals(Sort.by(Sort.Direction.DESC, "id"), sort);
    }

    @Test
    void sortWithoutDirectionDefaultsToAscending() {
        final Sort sort = sortUtils.addSortAttributesToModel(model, "name", "id,desc", COLUMNS);

        assertEquals(Sort.by(Sort.Direction.ASC, "name"), sort);
    }

    @Test
    void linksAreBuiltFromEachRequest() {
        sortUtils.addSortAttributesToModel(model, null, "id,desc", COLUMNS);
        currentUrl.set("/test?name=other");
        final Model secondModel = new ExtendedModelMap();

        sortUtils.addSortAttributesToModel(secondModel, null, "id,desc", COLUMNS);

        assertEquals("/test?name=other&sort=name,asc", secondModel.getAttribute("sortByNameLink"));
    }
}
