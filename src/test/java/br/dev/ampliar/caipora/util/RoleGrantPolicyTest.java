package br.dev.ampliar.caipora.util;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class RoleGrantPolicyTest {

    // the current user holds roles 1 and 2 only
    private static final Set<Integer> GRANTABLE = Set.of(1, 2);

    @Test
    void grantsRolesHeldByTheCurrentUser() {
        assertEquals(List.of(1, 2), RoleGrantPolicy.resolve(List.of(1, 2), List.of(), GRANTABLE, false));
    }

    @Test
    void refusesToGrantOtherRoles() {
        assertThrows(IllegalArgumentException.class,
                () -> RoleGrantPolicy.resolve(List.of(1, 9), List.of(), GRANTABLE, false));
    }

    @Test
    void keepsRolesTheCurrentUserCannotManage() {
        // the target already had role 9: it is neither removable nor an escalation
        assertEquals(List.of(2, 9), RoleGrantPolicy.resolve(List.of(2), List.of(1, 9), GRANTABLE, false));
        assertEquals(List.of(1, 9), RoleGrantPolicy.resolve(List.of(1, 9), List.of(9), GRANTABLE, false));
    }

    @Test
    void ownRolesCannotChange() {
        assertEquals(List.of(1), RoleGrantPolicy.resolve(List.of(1, 2, 9), List.of(1), GRANTABLE, true));
    }

    @Test
    void cannotManageMorePrivilegedAccounts() {
        assertTrue(RoleGrantPolicy.canManage(List.of(1), GRANTABLE));
        assertTrue(RoleGrantPolicy.canManage(List.of(), GRANTABLE));
        assertFalse(RoleGrantPolicy.canManage(List.of(1, 9), GRANTABLE));
    }
}
