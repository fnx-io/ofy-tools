package io.fnx.backend.tools.hydration;

public interface CanBeHydrated<D, HC extends HydrationContext> {

    HydrationRecipe<D, HC> getRecipe();
}
