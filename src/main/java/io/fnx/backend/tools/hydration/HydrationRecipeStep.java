package io.fnx.backend.tools.hydration;

import com.googlecode.objectify.Key;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * One step of the HydrationRecipe.
 * 
 * Should contain simple atomic hydration: I'm created by a user, here is that users key, fetch me details and
 * I will populate some of them then into ENTITY. Or: I will check logged user (from HydrationContext) and
 * will remove sensitive information from the ENTITY accordingly.
 *
 * @param <ENTITY>
 * @param <HC>
 */
public interface HydrationRecipeStep<ENTITY, HC extends HydrationContext> {

	/**
	 * Gives the Hydrator information about keys you need to fetch from datasource.
	 *
	 * Note: null or Collection.singletonList(...) are acceptable values.
	 *
	 */
    Collection<Key<?>> getDependencies(ENTITY entity, HC context);

	/**
	 * Called by Hydrator after fetching all dependencies from all steps and recipes.
	 *
	 * The map of all fetched entities is shared, so changing fetched entity here changes the entity
	 * for all steps (actually for non-transactional call in whole request).
	 * 
	 */
	void executeStep(ENTITY entity, HC context, Map<Key<Object>, Object> allFetchedEntities);

}
