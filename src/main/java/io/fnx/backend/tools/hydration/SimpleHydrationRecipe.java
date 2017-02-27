package io.fnx.backend.tools.hydration;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Use this recipe for simple list of recipe steps which don't change depending on context or entity.
 * Probably - most of the cases.
 *
 * Created by tomucha on 27.02.17.
 */
public class SimpleHydrationRecipe<ENTITY, HC extends HydrationContext> implements HydrationRecipe<ENTITY, HC> {

	private List<HydrationRecipeStep<ENTITY, HC>> recipeSteps;

	public SimpleHydrationRecipe(List<HydrationRecipeStep<ENTITY, HC>> steps) {
		this.recipeSteps = steps;
	}

	public SimpleHydrationRecipe(HydrationRecipeStep<ENTITY, HC> ... steps) {
		this.recipeSteps = Arrays.asList(steps);
	}

	@Override
	public List<HydrationRecipeStep<ENTITY, HC>> buildSteps(ENTITY entity, HC ctx) {
		return recipeSteps;
	}

}
