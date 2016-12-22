package io.fnx.backend.manager;

public class UniqueViolationException extends RuntimeException {

    public UniqueViolationException(String message) {
        super(message);
    }
}
