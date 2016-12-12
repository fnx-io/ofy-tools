package io.fnx.backend.tools.authorization;

import io.fnx.backend.tools.auth.UserContext;
import io.fnx.backend.tools.auth.UserRole;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marked method will be called, if call is authenticated as Admin (in therms of {@link UserRole#isAdmin()}).
 *
 * @see AllowedForAdminsAuthorizationGuard
 * @see UserContext
 * @see UserRole
 */
@Retention(RetentionPolicy.RUNTIME) @Target({ElementType.METHOD})
public @interface AllowedForAdmins {
}
