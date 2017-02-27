package io.fnx.backend.tools.hydration;

import java.util.List;

/**
 * Recipe which describes hydration of single entity
 */
public interface HydrationRecipe<ENTITY, HC extends HydrationContext> {

    /**
     * @param ctx hydration context to take into account when populating properties
     * @return Collection of properties to hydrate
     */
    List<HydrationRecipeStep<ENTITY, HC>> buildSteps(ENTITY entity, HC ctx);
    
}
