package br.dev.ampliar.caipora.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class HtmlSanitizerTest {

    @Test
    void keepsFormatting() {
        final String html = "<p><strong>Bold</strong> and <em>italic</em></p><ul><li>item</li></ul>";
        assertEquals(html, HtmlSanitizer.sanitize(html));
    }

    @Test
    void removesScriptsAndEventHandlers() {
        final String result = HtmlSanitizer.sanitize(
                "<p onclick=\"alert(1)\">text</p><script>alert(1)</script><img src=x onerror=alert(1)>");
        assertNotNull(result);
        assertFalse(result.contains("script"));
        assertFalse(result.contains("onclick"));
        assertFalse(result.contains("onerror"));
        assertTrue(result.contains("text"));
    }

    @Test
    void removesJavascriptUrls() {
        final String result = HtmlSanitizer.sanitize("<a href=\"javascript:alert(1)\">link</a>");
        assertNotNull(result);
        assertFalse(result.contains("javascript"));
    }

    @Test
    void removesIframes() {
        final String result = HtmlSanitizer.sanitize("<iframe src=\"https://evil.example\"></iframe><p>ok</p>");
        assertEquals("<p>ok</p>", result);
    }

    @Test
    void blankBecomesNull() {
        assertNull(HtmlSanitizer.sanitize(null));
        assertNull(HtmlSanitizer.sanitize("   "));
        assertNull(HtmlSanitizer.sanitize("<script>x</script>"));
    }
}
