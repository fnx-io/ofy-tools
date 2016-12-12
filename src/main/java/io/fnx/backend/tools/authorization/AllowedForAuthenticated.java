package io.fnx.backend.tools.authorization;

import io.fnx.backend.tools.auth.UserContext;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Mark method will be called only when the call is made by authenticated user.
 *
 * @see AllowedForAuthenticatedAuthorizationGuard
 * @see UserContext
 */
@Retention(RetentionPolicy.RUNTIME) @Target({ElementType.METHOD})
public @interface AllowedForAuthenticated {
}
