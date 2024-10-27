package br.dev.ampliar.caipora.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class NotFoundExceptionTest {

    @Test
    void testNotFoundException_NoMessage() {
        var exception = new NotFoundException();
        assertNull(exception.getMessage());
    }

    @Test
    void testNotFoundException_WithMessage() {
        var message = "Entity not found";
        var exception = new NotFoundException(message);
        assertEquals(message, exception.getMessage());
    }
}