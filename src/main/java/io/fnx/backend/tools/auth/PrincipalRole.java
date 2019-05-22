package io.fnx.backend.tools.auth;

public interface PrincipalRole {

    boolean isAdmin();

    boolean isAnonymous();

    default boolean isAuthenticated() {
        return !isAnonymous();
    }

}
