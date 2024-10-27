package br.dev.ampliar.caipora.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.ui.Model;
import org.springframework.web.util.UriBuilder;

import java.util.Map;

import static org.mockito.Mockito.*;

class SortUtilsTest {

    @Mock
    private Model model;

    @Mock
    private UriBuilder uriBuilder;

    private SortUtils sortUtils;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        sortUtils = new SortUtils(uriBuilder);
    }

    @Test
    void testAddSortAttributesToModel() {
        Pageable pageable = PageRequest.of(0, 10);
        Map<String, String> sortAttributes = Map.of("name", "name");

        when(uriBuilder.replaceQueryParam(anyString(), anyString())).thenReturn(uriBuilder);
        when(uriBuilder.replaceQueryParam(anyString(), anyInt())).thenReturn(uriBuilder);
        when(uriBuilder.toUriString()).thenReturn("/test?sort=name,asc&page=0");

        sortUtils.addSortAttributesToModel(model, "name,asc", pageable, sortAttributes);

        verify(model).addAttribute("nameLink", "/test?sort=name,asc&page=0");
        verify(model).addAttribute("nameDirection", "desc");
    }
}