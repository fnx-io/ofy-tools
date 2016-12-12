package io.fnx.backend.tools.authorization;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks parameter as Owned. This parameter will then be subjected
 * to ownership check when on method marked with {@link AllowedForOwnerAuthorizationGuard}.
 *
 * @see AllowedForOwnerAuthorizationGuard
 */
@Retention(RetentionPolicy.RUNTIME) @Target({ElementType.PARAMETER})
public @interface KeyHasOwner {
}
