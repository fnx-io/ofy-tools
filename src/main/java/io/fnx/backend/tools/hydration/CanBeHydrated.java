package io.fnx.backend.tools.hydration;

public interface CanBeHydrated <ENTITY, HC extends HydrationContext> {

    HydrationRecipe<ENTITY, HC> getRecipe();

}
