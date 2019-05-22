package io.fnx.backend.tools.authorization;

import io.fnx.backend.tools.auth.PrincipalRole;

public enum TestPrincipalRole implements PrincipalRole {

    ADMIN,
    USER,
    ANONYMOUS;

    @Override
    public boolean isAdmin() {
        return this == ADMIN;
    }

    @Override
    public boolean isAnonymous() {
        return this == ANONYMOUS;
    }
}
