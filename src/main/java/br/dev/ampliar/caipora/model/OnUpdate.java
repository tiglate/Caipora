package br.dev.ampliar.caipora.model;

import jakarta.validation.groups.Default;

/**
 * Validation group for updates. Extends {@link Default} so that constraints without an explicit group
 * (e.g. {@code @Size}, {@code @Email}, uniqueness checks) are validated as well.
 */
public interface OnUpdate extends Default {
}
