package io.fnx.backend.tools.auth;

public interface UserRole {

    boolean isAdmin();

    boolean isAnonymous();
}
