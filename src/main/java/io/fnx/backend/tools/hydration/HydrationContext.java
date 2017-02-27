package io.fnx.backend.tools.hydration;

import com.googlecode.objectify.Key;
import io.fnx.backend.tools.auth.Principal;

/**
 * Context each hydration can use to decide what should be returned.
 *
 * Some hydration should finish differently based on the authorization status
 * of currently logged user.
 * 
 * Project should have more details for its owner than for a regular user, and {@link HydrationRecipe}
 * can take this into account.
 *
 * fnx note: It's possible, even recommended, to descent CallContext from HydrationContext.
 */
public interface HydrationContext {

}
/*

    protected Principal principal;

    public HydrationContext(Principal principal) {
        this.principal = principal;
    }

    public boolean isLogged() {
        return principal != null && principal.getPrincipalKey() != null;
    }

    public Key<? extends Principal> getPrincipalKey() {
        if (principal == null) {
            return null;
        } else {
            return principal.getPrincipalKey();
        }
    }
}
*/
