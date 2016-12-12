package io.fnx.backend.tools.auth;

import com.googlecode.objectify.Key;

/**
 *
 */
public interface UserContext {

    Key<? extends User> getUserKey();
    UserRole getUserRole();
}
