package br.dev.ampliar.caipora.model;

import jakarta.validation.groups.Default;

/**
 * Validation group for creation. Extends {@link Default} so that constraints without an explicit group
 * (e.g. {@code @Size}, {@code @Email}, uniqueness checks) are validated as well.
 */
public interface OnCreate extends Default {
}
