package io.fnx.backend.tools.authorization;

import io.fnx.backend.tools.auth.PrincipalRole;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marked method will be called, if call is authenticated as Admin (in therms of {@link PrincipalRole#isAdmin()}).
 *
 * @see AllowedForAdminsAuthorizationGuard
 * @see PrincipalRole
 */
@Retention(RetentionPolicy.RUNTIME) @Target({ElementType.METHOD})
public @interface AllowedForAdmins {
}
