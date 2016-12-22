package io.fnx.backend.tools.ofy;

import com.google.common.base.Function;
import com.google.common.base.Predicates;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.Objectify;
import com.googlecode.objectify.Ref;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Useful function to call when you are dealing with Objectify.
 * Can be statically imported (shhh. don't tell Tomuchacha)
 * @author Jiri Zuna (jiri@zunovi.cz)
 */
public class OfyUtils {
    /**
     * Null safe getter of the underlying id in given key
     * @param k key to get id for
     * @return id bound to the given key
     */
    public static Long keyToId(Key<?> k) {
        if (k != null) {
            return k.getId();
        } else {
            return null;
        }
    }

    /**
     * Null safe getter of the underlying id in given ref
     * @param ref ref to get id for
     * @return id bound to the given ref
     */
    public static Long refToId(Ref<?> ref) {
        if (ref != null) {
            return keyToId(ref.getKey());
        } else {
            return null;
        }
    }

    /**
     * Null safe converter from refs to keys
     * @param ref ref to convert
     * @param <T> entity type
     * @return key from given ref. Null if ref is null.
     */
    public static <T> Key<T> refToKey(Ref<T> ref) {
        if (ref != null) {
            return ref.getKey();
        } else {
            return null;
        }
    }

    /**
     * Convert all refs to respective keys
     * @param refs to convert
     * @param <T> refs type
     * @return all non null refs as keys
     */
    public static <T> List<Key<T>> refsToKeys(Collection<Ref<T>> refs) {
        if (refs == null) return new ArrayList<>();
        final List<Key<T>> results = new ArrayList<>(refs.size());
        for (Ref<T> ref : refs) {
            if (ref != null) results.add(ref.getKey());
        }
        return results;
    }

    /**
     * Null safe factory for keys from ids
     * @param entity the entity to create key for
     * @param id entity id
     * @param <T> entity Type
     * @return entity key
     */
    public static <T> Key<T> idToKey(Class<T> entity, Long id) {
        if (id == null) {
            return null;
        } else {
            return Key.create(entity, id);
        }
    }

    /**
     * Null safe factory for keys from ids
     * @param entity the entity to create key for
     * @param name entity id (name)
     * @param <T> entity Type
     * @return entity key
     */
    public static <T> Key<T> nameToKey(Class<T> entity, String name) {
        if (name == null) {
            return null;
        } else {
            return Key.create(entity, name);
        }
    }

    /**
     * Null safe factory for refs from ids
     * @param entity the entity to create ref for
     * @param id entity id
     * @param <T> entity type
     * @return entity ref
     */
    public static <T> Ref<T> idToRef(Class<T> entity, Long id) {
        if (id == null) {
            return null;
        } else {
            return Ref.create(idToKey(entity, id));
        }
    }

    /**
     * Null safe factory for refs from keys
     * @param key key to convert to ref
     * @param <T> key type
     * @return entity ref
     */
    public static <T> Ref<T> keyToRef(Key<T> key) {
        if (key == null) {
            return null;
        } else {
            return Ref.create(key);
        }
    }

    /**
     * Null safe load of given Ref
     * @param ref the ref to load
     * @param <T> entity type
     * @return loaded entity
     */
    public static <T> T loadRef(Ref<T> ref) {
        if (ref == null) {
            return null;
        } else {
            return ref.get();
        }
    }

    /**
     * Null safe load of given Key
     * @param key the entity key to load
     * @param <T> entity type
     * @return loaded entity
     */
    public static <T> T loadKey(Key<T> key) {
        return loadRef(keyToRef(key));
    }

    /**
     * Loads all not null refs from given collection
     * @param ref refs to load
     * @param <T> entity type
     * @return list of hydrated entities instead of refs
     */
    public static <T> List<T> loadRefs(Collection<Ref<T>> ref) {
        if (ref == null) {
            return Lists.newArrayList();
        }

        final ImmutableList<T> hydrated = FluentIterable.from(ref).transform(new Function<Ref<T>, T>() {
            @Override
            public T apply(final Ref<T> ref) {
                return loadRef(ref);
            }
        }).toList();

        return Lists.newArrayList(hydrated);
    }

    /**
     * Transforms given ids to keys of given entity
     * @param entityClass the class of the entity
     * @param ids entity ids
     * @param <T> entity type
     * @return All not null ids converted to keys. Never null and no nulls inside the collection.
     */
    public static <T> List<Key<T>> idsToKeys(final Class<T> entityClass, Collection<Long> ids) {
        if (ids == null) return Lists.newArrayList();

        return FluentIterable.from(ids).filter(Predicates.notNull()).transform(new Function<Long, Key<T>>() {
            @Override
            public Key<T> apply(Long id) {
                return idToKey(entityClass, id);
            }
        }).toList();
    }

    public static <T> Function<? super Ref<T>, Key<T>> refToKeyTransformer() {
        return new Function<Ref<T>, Key<T>>() {
            @Override
            public Key<T> apply(Ref<T> ref) {
                return refToKey(ref);
            }
        };
    }

    /**
     * Asserts that Objectify has started a transaction
     *
     * @param ofy Objectify
     * @throws IllegalStateException when called outside transaction
     */
    public static void assertTransaction(Objectify ofy) {
        if (ofy.getTransaction() == null) throw new IllegalStateException("No transaction");
        if (!ofy.getTransaction().isActive()) throw new IllegalStateException("Transaction is not active!");
    }
}
