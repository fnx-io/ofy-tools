package io.fnx.backend.tools.hydration;

import com.googlecode.objectify.Key;
import io.fnx.backend.tools.auth.User;
import io.fnx.backend.tools.auth.UserContext;

/**
 * Context each hydration can use to decide what should be returned.
 *
 * Some hydration should finish differently based on the authorization status
 * of currently logged user.
 * Project should have more details for its owner than for a regular user, and {@link HydrationRecipe}
 * can take this into account.
 */
public class HydrationContext<ID, U extends User<ID>, C extends UserContext<ID, U>> {

    protected C userCtx;

    public HydrationContext(C ctx) {
        this.userCtx = ctx;
    }

    public boolean isLogged() {
        return userCtx != null && userCtx.getUserKey() != null;
    }

    public Key<U> getUserKey() {
        if (userCtx == null) {
            return null;
        } else {
            return userCtx.getUserKey();
        }
    }
}
