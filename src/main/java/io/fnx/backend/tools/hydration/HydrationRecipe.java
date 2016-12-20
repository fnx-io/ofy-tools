package io.fnx.backend.tools.hydration;

import java.util.Collection;

/**
 * Recipe which describes hydration of single entity
 */
public interface HydrationRecipe<D, HC extends HydrationContext> {

    /**
     * Hook for transforming the target entity.
     * Example: When populating UserEntity and hydration context signals,
     * that the user can see only public info, we might want to return different Thing (PublicUserDetails?)
     * and this hook can be used to do that.
     * @param ctx hydration context to take into account when populating properties
     * @return the resulting entity we want our hydration to end with eg. UserEntity -> PublicUserDetails
     */
    D transformForApi(HC ctx);

    /**
     * @param ctx hydration context to take into account when populating properties
     * @return Collection of properties to hydrate
     */
    Collection<HydratedProperty<D, ?>> propsToHydrate(HC ctx);
}
