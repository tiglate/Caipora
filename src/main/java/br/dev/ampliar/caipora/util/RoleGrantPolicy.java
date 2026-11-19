package br.dev.ampliar.caipora.util;

import java.util.*;

/**
 * Prevents privilege escalation through user management.
 * <ul>
 *     <li>A user manager can only grant or revoke roles that they hold themselves.</li>
 *     <li>A user manager cannot modify (or reset the password of, or delete) an account holding roles they don't
 *     have — otherwise resetting an administrator's password would be a way to become administrator.</li>
 *     <li>Nobody can change their own roles.</li>
 * </ul>
 */
public final class RoleGrantPolicy {

    private RoleGrantPolicy() {
        throw new AssertionError("No instances for you!");
    }

    /**
     * @param targetRoles roles of the account to be modified
     * @param grantable   roles held by the current user
     * @return whether the current user may modify the account
     */
    public static boolean canManage(final Collection<Integer> targetRoles, final Collection<Integer> grantable) {
        return targetRoles == null || grantable.containsAll(targetRoles);
    }

    /**
     * Computes the roles to store for an account.
     *
     * @param requested roles submitted in the form (may be null)
     * @param existing  roles the account currently has (empty for a new account)
     * @param grantable roles held by the current user
     * @param self      whether the current user is editing their own account
     * @return the roles to store
     * @throws IllegalArgumentException when roles outside {@code grantable} are being added
     */
    public static List<Integer> resolve(final Collection<Integer> requested, final Collection<Integer> existing,
                                        final Collection<Integer> grantable, final boolean self) {
        final Set<Integer> current = existing == null ? Set.of() : new LinkedHashSet<>(existing);
        if (self) {
            return List.copyOf(current);
        }
        final Set<Integer> wanted = requested == null ? Set.of() : new LinkedHashSet<>(requested);
        final Set<Integer> notAllowed = new LinkedHashSet<>(wanted);
        notAllowed.removeAll(grantable);
        notAllowed.removeAll(current);
        if (!notAllowed.isEmpty()) {
            throw new IllegalArgumentException("You can only grant roles that you hold yourself.");
        }
        final Set<Integer> result = new LinkedHashSet<>();
        // roles the current user can manage: as requested
        wanted.stream().filter(grantable::contains).forEach(result::add);
        // roles the current user cannot manage: kept as they were
        current.stream().filter(role -> !grantable.contains(role)).forEach(result::add);
        return List.copyOf(result);
    }
}
