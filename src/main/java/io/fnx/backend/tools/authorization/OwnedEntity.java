package io.fnx.backend.tools.authorization;

import com.googlecode.objectify.Key;

/**
 * Marks an entity as owned.
 *
 * Owned entity belongs (logically) to another entity.
 *
 * Example: <code>AccountEntity</code> would implement <code>OwnedEntity&lt;UserEntity&gt;</code>, signalling that
 * it belongs to some user.
 *
 * These entities will be subjected to ownership checks when
 * the method is marked with {@link AllowedForOwnerAuthorizationGuard}
 *
 * @see KeyHasOwner
 * @see IdHasOwner
 * @see AllowedForOwnerAuthorizationGuard
 *
 * @param <T> type of the entity owning this object
 */
public interface OwnedEntity<T> {

    /**
     * @return owner's key
     */
    Key<T> getOwnerKey();
}
