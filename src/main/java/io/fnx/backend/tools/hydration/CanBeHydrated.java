package io.fnx.backend.tools.hydration;

import io.fnx.backend.tools.auth.User;
import io.fnx.backend.tools.auth.UserContext;

public interface CanBeHydrated<ID, U extends User<ID>, C extends UserContext<ID, U>, HC extends HydrationContext<ID, U, C>, D> {

    HydrationRecipe<ID, U, C, HC, D> getRecipe();
}
