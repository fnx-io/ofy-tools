package io.fnx.backend.manager;

import com.googlecode.objectify.Key;
import com.googlecode.objectify.Objectify;
import com.googlecode.objectify.VoidWork;
import io.fnx.backend.domain.UniqueIndexEntity;
import io.fnx.backend.tools.ofy.OfyProvider;
import io.fnx.backend.tools.ofy.OfyUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.Objects;

import static java.lang.String.format;

/**
 * <p>
 * This manager can ensure that there is only one owner of single value for any property.
 * </p>
 * Example:
 * <p>
 * When registering an user you want their email to remain unique. As part of the transaction of
 * saving the user, you could call the {@link #saveUniqueIndexOwner(Enum, String, Key)} which will
 * throw if there is already given property with same value owned by another key
 * </p>
 * <code>
 *     Key&lt;UserEntity&gt; userKey = ofy.factory().allocateId(UserEntity.class);
 *     user.setKey(userKey);
 *     ofy().transact(new VoidWork() {
 *       &amp;Override
 *       public void vrun() {
 *         saveUniqueIndexOwner(UniqueProperties.user_email, user.getEmail(), userKey);
 *         ofy().save().entity(user).now();
 *       }
 *     });
 * </code>
 */
public class UniqueIndexManager {

    private Logger log = LoggerFactory.getLogger(UniqueIndexManager.class);

    private OfyProvider ofyProvider;

    /**
     * Only single owner can own the combination of property and given value.
     * If called with different owner will throw {@link UniqueViolationException}.
     *
     * Must be called inside Objectify transaction.
     *
     * @param property the property to bind the unique value to (namespace for unique value)
     * @param value the value which should be for given property unique
     * @param owner owner of the unique value
     * @param <E> type of the enum describing the properties
     * @throws UniqueViolationException if different owner for given value and property already exists
     * @throws IllegalStateException if called outside Objectify transaction {@link Objectify#transact(Runnable)}
     */
    public <E extends Enum<E>> void saveUniqueIndexOwner(E property, String value, Key<?> owner) {
        OfyUtils.assertTransaction(ofy());
        if (property == null) throw new NullPointerException("Property must not be null!");

        final Key<UniqueIndexEntity> k = UniqueIndexEntity.createKey(property, value);
        final UniqueIndexEntity existing = ofy().load().key(k).now();

        final boolean sameOwner = existing != null && Objects.equals(existing.getOwnerId(), owner);
        if (existing != null && !sameOwner) {
            log.error(format("%s does not own unique value %s", owner, k));
            throw new UniqueViolationException(format("Unique constraint viloation for proerty [%s]", property.toString()));
        }
        UniqueIndexEntity index = new UniqueIndexEntity();
        index.setOwnerId(owner);
        index.setUniqueKey(k.getName());

        ofy().save().entities(index).now();
    }

    /**
     * Returns owner key for given property and value. Or null if no such unique value exists.
     *
     * @param property the property for which we want the unique value
     * @param value the value of the property
     * @param <T> owning entity type
     * @param <E> unique properties enum
     *
     * @return owner which has given unique value for requested property, or null if no such value exist
     */
    @SuppressWarnings("unchecked")
    public <T, E extends Enum<E>> Key<T> getUniqueValueOwner(E property, String value) {
        if (property == null) throw new NullPointerException("Property must not be null!");

        final UniqueIndexEntity entity = ofy().load().key(UniqueIndexEntity.createKey(property, value)).now();
        if (entity == null) return null;

        return (Key<T>) entity.getOwnerId();
    }

    /**
     * Evicts given unique value from the datastore
     *
     * Must be called inside Objectify transaction.
     *
     * @param property the property to bind the unique value to (namespace for unique value)
     * @param value the value which should be for given property unique
     * @param <E> type of the enum describing the properties
     * @throws IllegalStateException if called outside Objectify transaction {@link Objectify#transact(Runnable)}
     */
    public <E extends Enum<E>> void deleteUniqueIndexOwner(E property, String value) {
        OfyUtils.assertTransaction(ofy());
        if (property == null) throw new NullPointerException("Property must not be null!");

        Key<UniqueIndexEntity> k = UniqueIndexEntity.createKey(property, value);
        ofy().delete().entity(k).now();
    }

    private Objectify ofy() {
        return ofyProvider.get();
    }

    @Inject
    public void setOfyProvider(OfyProvider ofyProvider) {
        this.ofyProvider = ofyProvider;
    }
}
