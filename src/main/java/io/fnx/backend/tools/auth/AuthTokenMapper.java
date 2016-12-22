package io.fnx.backend.tools.auth;

/**
 * Service which can map authentication token (cookie, JWT, Authorization header) to
 * an User whom it belongs
 *
 * @param <T> type of the user entity
 */
public interface AuthTokenMapper<T extends Principal> {

    /**
     * Given a token should return a user owning the token
     *
     * @param token authentication token
     * @return user owning the token or null, if token is invalid or expired
     */
    T useToken(String token);
}
