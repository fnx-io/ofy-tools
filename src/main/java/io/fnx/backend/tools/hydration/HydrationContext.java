package io.fnx.backend.tools.hydration;

import com.googlecode.objectify.Key;
import io.fnx.backend.tools.auth.User;

/**
 * Context each hydration can use to decide what should be returned.
 *
 * Some hydration should finish differently based on the authorization status
 * of currently logged user.
 * Project should have more details for its owner than for a regular user, and {@link HydrationRecipe}
 * can take this into account.
 */
public class HydrationContext {

    protected User principal;

    public HydrationContext(User principal) {
        this.principal = principal;
    }

    public boolean isLogged() {
        return principal != null && principal.getUserKey() != null;
    }

    public Key<? extends User> getUserKey() {
        if (principal == null) {
            return null;
        } else {
            return principal.getUserKey();
        }
    }
}
