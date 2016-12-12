package io.fnx.backend.tools.authorization;

public class PermissionDeniedException extends RuntimeException {

    public PermissionDeniedException() {
    }

    public PermissionDeniedException(String message) {
        super(message);
    }
}
