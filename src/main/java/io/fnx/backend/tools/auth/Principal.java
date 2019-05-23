package io.fnx.backend.tools.auth;

import com.googlecode.objectify.Key;

import java.util.List;

public interface Principal {

    Key<? extends Principal> getPrincipalKey();

    List<? extends PrincipalRole> getUserRoles();

    default boolean hasAdminRole() {
        if (hasNoRoles()) return false;

        for (PrincipalRole role : getUserRoles()) {
            if (role.isAdmin()) return true;
        }
        return false;
    }

    default boolean hasAnonymousRole() {
        if (hasNoRoles()) return false;

        for (PrincipalRole role : getUserRoles()) {
            if (role.isAnonymous()) return true;
        }
        return false;
    }

    default boolean hasNoRoles() {
        List<? extends PrincipalRole> roles = getUserRoles();
        return roles == null || roles.isEmpty();
    }

}
