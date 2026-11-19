package br.dev.ampliar.caipora.util;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.safety.Safelist;

/**
 * Sanitizes rich-text HTML produced by the WYSIWYG editor before it is stored or rendered.
 * Only a conservative set of formatting tags is kept; scripts, event handlers, iframes,
 * styles and javascript: URLs are removed.
 */
public final class HtmlSanitizer {

    private static final Safelist SAFELIST = Safelist.relaxed()
            .addTags("hr", "s", "del", "ins", "mark")
            .addAttributes(":all", "class")
            .addProtocols("a", "href", "http", "https", "mailto")
            .addProtocols("img", "src", "http", "https", "data")
            .preserveRelativeLinks(false);

    private static final Document.OutputSettings OUTPUT_SETTINGS = new Document.OutputSettings().prettyPrint(false);

    private HtmlSanitizer() {
        throw new AssertionError("No instances for you!");
    }

    /**
     * @param html untrusted HTML (may be {@code null})
     * @return the sanitized HTML, or {@code null} when the input is {@code null} or blank
     */
    public static String sanitize(final String html) {
        if (html == null || html.isBlank()) {
            return null;
        }
        final String clean = Jsoup.clean(html, "", SAFELIST, OUTPUT_SETTINGS);
        return clean.isBlank() ? null : clean;
    }

}
