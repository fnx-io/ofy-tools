package io.fnx.backend.tools.authorization;

import io.fnx.backend.tools.auth.UserContext;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marked method will be called, if check for ownership of supplied arguments passes.
 *
 * As of now, it only checks entity Ids, keys and objects implementing the
 * {@link OwnedEntity} interface.
 *
 * @see AllowedForOwnerAuthorizationGuard
 * @see OwnedEntity
 * @see IdHasOwner
 * @see KeyHasOwner
 * @see UserContext
 */
@Retention(RetentionPolicy.RUNTIME) @Target({ElementType.METHOD})
public @interface AllowedForOwner {
}
