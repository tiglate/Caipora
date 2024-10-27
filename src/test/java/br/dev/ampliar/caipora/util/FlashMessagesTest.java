package br.dev.ampliar.caipora.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

class FlashMessagesTest {

    @Mock
    private RedirectAttributes redirectAttributes;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testCreateSuccess() {
        FlashMessages.createSuccess(redirectAttributes, "TestEntity");
        verify(redirectAttributes, times(1)).addFlashAttribute(FlashMessages.MSG_SUCCESS, "TestEntity was created successfully");
    }

    @Test
    void testUpdateSuccess() {
        FlashMessages.updateSuccess(redirectAttributes, "TestEntity");
        verify(redirectAttributes, times(1)).addFlashAttribute(FlashMessages.MSG_SUCCESS, "TestEntity was updated successfully.");
    }

    @Test
    void testDeleteSuccess() {
        FlashMessages.deleteSuccess(redirectAttributes, "TestEntity");
        verify(redirectAttributes, times(1)).addFlashAttribute(FlashMessages.MSG_SUCCESS, "TestEntity was removed successfully.");
    }

    @Test
    void testErrorWithException() {
        Exception exception = new Exception("Test error message");
        FlashMessages.error(redirectAttributes, exception);
        verify(redirectAttributes, times(1)).addFlashAttribute(FlashMessages.MSG_ERROR, "Test error message");
    }

    @Test
    void testErrorWithMessage() {
        FlashMessages.error(redirectAttributes, "Test error message");
        verify(redirectAttributes, times(1)).addFlashAttribute(FlashMessages.MSG_ERROR, "Test error message");
    }

    @Test
    void testInfo() {
        FlashMessages.info(redirectAttributes, "Test info message");
        verify(redirectAttributes, times(1)).addFlashAttribute(FlashMessages.MSG_INFO, "Test info message");
    }

    @Test
    void testSuccess() {
        FlashMessages.success(redirectAttributes, "Test success message");
        verify(redirectAttributes, times(1)).addFlashAttribute(FlashMessages.MSG_SUCCESS, "Test success message");
    }

    @Test
    void testReferencedWarning() {
        FlashMessages.referencedWarning(redirectAttributes, "TestEntity", 123);
        verify(redirectAttributes, times(1)).addFlashAttribute(FlashMessages.MSG_ERROR, "This entity is still referenced by TestEntity 123 via field Software.");
    }
}