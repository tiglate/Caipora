package br.dev.ampliar.caipora.dev;

import net.datafaker.Faker;

import java.text.Normalizer;
import java.util.Locale;

/**
 * Name helpers shared by the loaders.
 */
final class DevNames {

    private DevNames() {
        throw new AssertionError("No instances for you!");
    }

    /**
     * @return lower-case ASCII slug, e.g. "José da Silva" → "jose.da.silva"
     */
    static String slug(final String text) {
        final String ascii = Normalizer.normalize(text, Normalizer.Form.NFD).replaceAll("\\p{M}", "");
        return ascii.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]+", ".").replaceAll("^\\.|\\.$", "");
    }

    static String email(final String first, final String last, final Faker faker) {
        return slug(first) + "." + slug(last) + "@" + faker.internet().domainName();
    }
}
