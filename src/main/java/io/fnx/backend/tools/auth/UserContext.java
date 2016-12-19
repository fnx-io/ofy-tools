package io.fnx.backend.tools.auth;

import com.googlecode.objectify.Key;

/**
 *
 */
public interface UserContext<ID, U extends User<ID>> {

    Key<U> getUserKey();
    UserRole getUserRole();
}
