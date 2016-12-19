package io.fnx.backend.tools.hydration;

import com.googlecode.objectify.Key;
import io.fnx.backend.tools.auth.User;
import io.fnx.backend.tools.auth.UserContext;
import io.fnx.backend.tools.ofy.OfyProvider;

import javax.inject.Inject;
import java.util.*;

/**
 * Created by Jiri Zuna on 12/12/2016.
 */
public class Hydrator {
    private final OfyProvider ofyProvider;

    @Inject
    public Hydrator(OfyProvider ofyProvider) {
        this.ofyProvider = ofyProvider;
    }

    /**
     * Hydrate using the recipe.
     * Will collect keys, load appropriate entities and then populate respective properties
     * @param recipe entity to hydrate
     * @param ctx context to take in account when populating properties
     * @param <T> type of the entity to be hydrated
     * @param <ID> type of the ID of the principal entity
     * @param <U> type of the user representing the principal
     * @param <C> type of the context holding currently authenticated user
     * @param <H> type of the hydration context binding together currently authenticated user and useful info needed by
     *           custom hydration recipes
     * @return hydrated entity
     */
    public <T,
            ID,
            U extends User<ID>,
            C extends UserContext<ID, U>,
            H extends HydrationContext<ID, U, C>>
    T hydrate(final HydrationRecipe<ID, U, C, H, T> recipe, H ctx) {
        if (recipe == null) return null;
        final T target = recipe.transformForApi(ctx);
        if (target == null) return null;
        if (ctx == null) {
            @SuppressWarnings("unchecked")
            final H tmp = (H) new HydrationContext<>(null);
            ctx = tmp;
        }

        final Collection<HydratedProperty<T, ?>> props = recipe.propsToHydrate(ctx);
        if (props == null) return target;
        final List<Hydration<T>> hydrations = collectKeys(target, props);

        doHydration(hydrations);
        return target;
    }

    /**
     * @see #hydrate(HydrationRecipe, HydrationContext)
     */
    public <T,
            ID,
            U extends User<ID>,
            C extends UserContext<ID, U>,
            H extends HydrationContext<ID, U, C>,
            CBH extends CanBeHydrated<ID, U, C, H, T>>
    T hydrate(final CBH canBeHydrated, final H ctx) {
        if (canBeHydrated == null) return null;
        return hydrate(canBeHydrated.getRecipe(), ctx);
    }

    /**
     * Hydrate collection of entities according to hydration recipes.
     * Each item in the collection should be handled as if hydrated with {@link #hydrate(HydrationRecipe, HydrationContext)}.
     * The difference here is, that keys get fetched at a single point in time for all given recipes.
     * @param toHydrate the collection to hydrate
     * @param ctx context to take in account when populating properties
     * @param <T> type of the entity to be hydrated
     * @param <ID> type of the ID of the principal entity
     * @param <U> type of the user representing the principal
     * @param <C> type of the context holding currently authenticated user
     * @param <H> type of the hydration context binding together currently authenticated user and useful info needed by
     *           custom hydration recipes
     * @return list containing hydrated entities
     */
    public <T, ID, U extends User<ID>, C extends UserContext<ID, U>, H extends HydrationContext<ID, U, C>> List<T> hydrateCol(
            final Collection<HydrationRecipe<ID, U, C, H, T>> toHydrate, H ctx) {
        if (toHydrate == null) return new LinkedList<>();
        if (ctx == null) {
            @SuppressWarnings("unchecked")
            final H tmp = (H) new HydrationContext<>(null);
            ctx = tmp;
        }
        final List<T> result = new LinkedList<>();
        final List<Hydration<T>> hydrations = new LinkedList<>();
        for (HydrationRecipe<ID, U, C, H, T> t : toHydrate) {
            if (t == null) continue;
            final T target = t.transformForApi(ctx);
            if (target == null) continue;
            result.add(target);
            final Collection<HydratedProperty<T, ?>> props = t.propsToHydrate(ctx);
            hydrations.addAll(collectKeys(target, props));
        }
        doHydration(hydrations);
        return result;
    }

    /**
     * @see #hydrateCol(Collection, HydrationContext)
     */
    public <T, ID, U extends User<ID>, C extends UserContext<ID, U>, H extends HydrationContext<ID, U, C>> List<T> hydrate(
            final Collection<? extends CanBeHydrated<ID, U, C, H, T>> canBeHydrated, final H ctx) {
        if (canBeHydrated == null) return null;
        final Collection<HydrationRecipe<ID, U, C, H, T>> recipes = new LinkedList<>();
        for (CanBeHydrated<ID, U, C, H, T> toHydrate : canBeHydrated) {
            final HydrationRecipe<ID, U, C, H, T> recipe;
            if (toHydrate == null) {
                recipe = null;
            } else {
                recipe = toHydrate.getRecipe();
            }
            recipes.add(recipe);
        }
        return hydrateCol(recipes, ctx);
    }

    private <T> void doHydration(List<Hydration<T>> hydrations) {
        final Set<Key<Object>> keys = new HashSet<>(500);

        for (Hydration<T> hydration : hydrations) {
            hydration.addKeysTo(keys);
        }
        // batch load all keys
        final Map<Key<Object>, Object> entityMap = ofyProvider.get().load().keys(keys);

        setProps(hydrations, entityMap);
    }

    @SuppressWarnings("unchecked")
    private static <T> List<Hydration<T>> collectKeys(T target, Collection<HydratedProperty<T, ?>> props) {
        if (props == null) return new LinkedList<>();
        final List<Hydration<T>> result = new LinkedList<>();
        for (HydratedProperty<T, ?> prop : props) {
            if (prop instanceof SingleValueHydratedProperty) {
                final SingleValueHydratedProperty<T, Object> singleProp = (SingleValueHydratedProperty<T, Object>) prop;
                result.add(new SingleValueHydration<>(target, singleProp));
            } else if (prop instanceof CollectionHydratedProperty) {
                final CollectionHydratedProperty<T, Object> colProp = (CollectionHydratedProperty<T, Object>) prop;
                result.add(new CollectionHydration<>(target, colProp));
            }
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    public static <T> void setProps(List<Hydration<T>> hydrations, Map<Key<Object>, Object> entityMap) {
        for (Hydration<T> e : hydrations) {
            e.hydrate(entityMap);
        }
    }

    public static abstract class Hydration<T> {
        public final T target;

        protected Hydration(T target) {
            this.target = target;
        }

        public abstract void addKeysTo(Collection<Key<Object>> keys);

        public abstract void hydrate(Map<Key<Object>, Object> entityMap);
    }

    /**
     * Holder of target entity and the handler for single property of the entity
     * @param <T>
     */
    public static class SingleValueHydration<T> extends Hydration<T> {

        public final SingleValueHydratedProperty<T, Object> prop;
        private final Key<Object> key;

        public SingleValueHydration(T target, SingleValueHydratedProperty<T, Object> prop) {
            super(target);
            this.prop = prop;
            this.key = prop.getKey(target);
        }

        @Override
        public void addKeysTo(Collection<Key<Object>> keys) {
            if (key != null) keys.add(key);
        }

        @Override
        public void hydrate(Map<Key<Object>, Object> entityMap) {
            final Object value = entityMap.get(key);
            prop.setProperty(target, value);
        }
    }

    public static class CollectionHydration<T> extends Hydration<T> {

        public final CollectionHydratedProperty<T, Object> prop;
        private final Collection<Key<Object>> keys;

        public CollectionHydration(T target, CollectionHydratedProperty<T, Object> prop) {
            super(target);
            this.prop = prop;
            this.keys = clearKeys(prop.getKeys(target));
        }

        private Collection<Key<Object>> clearKeys(Collection<Key<Object>> keys) {
            if (keys == null) return new LinkedList<>();
            LinkedList<Key<Object>> res = new LinkedList<>();
            for (Key<Object> key : keys) {
                if (key != null) res.add(key);
            }
            return res;
        }

        @Override
        public void addKeysTo(Collection<Key<Object>> allKeys) {
            allKeys.addAll(this.keys);
        }

        @Override
        public void hydrate(final Map<Key<Object>, Object> entityMap) {
            final List<Object> objects = new LinkedList<>();
            for (Key<Object> key : keys) {
                if (key == null) continue;
                final Object val = entityMap.get(key);
                if (val == null) continue;

                objects.add(val);
            }
            prop.setProperty(target, objects);
        }
    }
}
