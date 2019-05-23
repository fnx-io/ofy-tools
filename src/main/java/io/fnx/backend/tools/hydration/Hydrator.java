package io.fnx.backend.tools.hydration;

import com.googlecode.objectify.Key;
import io.fnx.backend.tools.ofy.OfyProvider;

import javax.inject.Inject;
import java.util.*;

/**
 * Hydrator is a two phase mechanism to decorate (or strip) entities you want to deliver view REST API (or so).
 *
 * In the first phase, hydrator collects all datastore keys which are needed for decoration (a.k.a. hydration).
 *
 * Then it fetches all requested entities, and fetched objects are used for hydration of original objects.
 *
 * Example: An article has a Key&lt;Author&gt; and you want to deliver information about that article with
 * the information about it's author.
 *
 * You can (and should) use hydration mechanism to remove sensitive data from outgoing entities.
 *
 */
public class Hydrator {

	private final OfyProvider ofyProvider;

	@Inject
	public Hydrator(OfyProvider ofyProvider) {
		this.ofyProvider = ofyProvider;
	}

	/**
	 * Use to hydrate entity, which is not CanBeHydrated itself.
	 */
	public <ENTITY, HC extends HydrationContext> void hydrateEntity(ENTITY entity, HydrationRecipe<ENTITY, HC> recipe, HC ctx) {
		hydrateCollection(Collections.singleton(entity), recipe, ctx);
	}

	/**
	 * Use to hydrate entity, which is CanBeHydrated and provides it's own recipe.
	 */
	public <ENTITY extends CanBeHydrated<ENTITY, HC>, HC extends HydrationContext> void hydrateEntity(ENTITY entity, HC ctx) {
		hydrateCollection(Collections.singleton(entity), entity.getRecipe(), ctx);
	}

	/**
	 * Generic entities with shared recipe.
	 */
	public <ENTITY, HC extends HydrationContext> void hydrateCollection(Iterable<ENTITY> entities, HydrationRecipe<ENTITY, HC> recipe, HC ctx) {

		Set<Key<Object>> keysToFetch = new HashSet<>();
		List<HydrationRecipeInstance<ENTITY, HC>> hydrationPlan = new ArrayList<>();

		// first iteration - collect keys
		for (ENTITY entity : entities) {
			List<HydrationRecipeStep<ENTITY, HC>> steps = recipe.buildSteps(entity, ctx);
			HydrationRecipeInstance<ENTITY, HC> hydrationPlanStep = new HydrationRecipeInstance(entity, steps);
			hydrationPlan.add(hydrationPlanStep);
			fetchKeys(entity, ctx, hydrationPlanStep, keysToFetch);
		}

		// ... and execute
		executeHydration(hydrationPlan, ctx, keysToFetch);
	}

	/**
	 * Each entity is CanBeHydrated and creates it's own recipe.
	 */
	public <ENTITY extends CanBeHydrated<ENTITY, HC>, HC extends HydrationContext> void hydrateCollection(Iterable<ENTITY> entities, HC ctx) {

		Set<Key<Object>> keysToFetch = new HashSet<>();
		List<HydrationRecipeInstance<ENTITY, HC>> hydrationPlan = new ArrayList<>();

		// first iteration - collect keys
		for (ENTITY entity : entities) {
			HydrationRecipe<ENTITY, HC> recipe = entity.getRecipe();
			List<HydrationRecipeStep<ENTITY, HC>> steps = recipe.buildSteps(entity, ctx);
			HydrationRecipeInstance<ENTITY, HC> hydrationPlanStep = new HydrationRecipeInstance(entity, steps);
			hydrationPlan.add(hydrationPlanStep);
			fetchKeys(entity, ctx, hydrationPlanStep, keysToFetch);
		}

		// ... and execute
		executeHydration(hydrationPlan, ctx, keysToFetch);
	}

	private <ENTITY, HC extends HydrationContext> void fetchKeys(ENTITY entity, HC ctx, HydrationRecipeInstance<ENTITY, HC> hydrationPlanStep, Set<Key<Object>> keysToFetch) {
		for (HydrationRecipeStep<ENTITY, HC> step : hydrationPlanStep.getSteps()) {
			Collection<Key<?>> deps = step.getDependencies(entity, ctx);
			if (deps != null && !deps.isEmpty()) {
				for (Key<?> key : deps) {
					keysToFetch.add((Key<Object>) key);
				}
			}
		}
	}

	private <ENTITY, HC extends HydrationContext> void executeHydration(List<HydrationRecipeInstance<ENTITY, HC>> hydrationPlan, HC ctx, Set<Key<Object>> keysToFetch) {
		// now let's fetch all we need!
		final Map<Key<Object>, Object> entityMap = ofyProvider.get().load().keys(keysToFetch);

		// second iteration - do your hydration you little steps!
		for (HydrationRecipeInstance<ENTITY, HC> hydrationPlanStep : hydrationPlan) {
			if (hydrationPlanStep.getSteps() != null) {
				for (HydrationRecipeStep<ENTITY, HC> step : hydrationPlanStep.getSteps()) {
					step.executeStep(hydrationPlanStep.getEntity(), ctx, entityMap);
				}
			}
		}
	}

	private static class HydrationRecipeInstance<ENTITY, HC extends HydrationContext> {

		private final ENTITY entity;
		private final List<HydrationRecipeStep<ENTITY, HC>> steps;

		public HydrationRecipeInstance(ENTITY entity, List<HydrationRecipeStep<ENTITY, HC>> steps) {
			this.entity = entity;
			this.steps = steps;
		}

		public ENTITY getEntity() {
			return entity;
		}

		public List<HydrationRecipeStep<ENTITY, HC>> getSteps() {
			return steps;
		}
	}

}

