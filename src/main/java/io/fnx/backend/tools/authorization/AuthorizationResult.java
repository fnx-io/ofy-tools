package io.fnx.backend.tools.authorization;

public class AuthorizationResult {
    public static final AuthorizationResult SUCCESS = new AuthorizationResult(true, null);
    public final boolean success;
    public final String msg;

    public AuthorizationResult(final boolean success, final String msg) {
        this.success = success;
        this.msg = msg;
    }

    public static AuthorizationResult failure(String msg) {
        return new AuthorizationResult(false, msg);
    }
}
