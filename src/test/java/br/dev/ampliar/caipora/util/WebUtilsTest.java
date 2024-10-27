package br.dev.ampliar.caipora.util;

import br.dev.ampliar.caipora.model.PaginationModel;
import br.dev.ampliar.caipora.model.PaginationStep;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.servlet.LocaleResolver;

import java.util.List;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class WebUtilsTest {

    @Mock
    private MessageSource messageSource;

    @Mock
    private LocaleResolver localeResolver;

    @Mock
    private HttpServletRequest request;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
        new WebUtils(messageSource, localeResolver);
    }

    @Test
    void testGetRequest() {
        HttpServletRequest result = WebUtils.getRequest();
        assertNotNull(result);
        assertEquals(request, result);
    }

    @Test
    void testGetMessage() {
        when(localeResolver.resolveLocale(any())).thenReturn(Locale.ENGLISH);
        when(messageSource.getMessage(eq("test.code"), any(), eq("test.code"), eq(Locale.ENGLISH)))
                .thenReturn("Test Message");

        String message = WebUtils.getMessage("test.code");
        assertEquals("Test Message", message);
    }

    @Test
    void testGetPaginationModel() {
        Page<String> page = new PageImpl<>(List.of("item1", "item2", "item3"), PageRequest.of(0, 3), 3);
        when(request.getParameter("sort")).thenReturn(null);
        when(request.getParameter("filter")).thenReturn(null);

        PaginationModel paginationModel = WebUtils.getPaginationModel(page);

        assertNotNull(paginationModel);
        assertEquals(3, paginationModel.getSteps().size());
        assertEquals("Item 1 - 3 of 3", paginationModel.getElements());

        PaginationStep previousStep = paginationModel.getSteps().get(0);
        assertTrue(previousStep.getDisabled());
        assertEquals("Previous", previousStep.getLabel());

        PaginationStep nextStep = paginationModel.getSteps().get(2);
        assertTrue(nextStep.getDisabled());
        assertEquals("Next", nextStep.getLabel());
    }
}