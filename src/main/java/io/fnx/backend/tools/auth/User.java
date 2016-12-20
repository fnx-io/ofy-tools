package io.fnx.backend.tools.auth;

import com.googlecode.objectify.Key;

public interface User {

    Key<? extends User> getUserKey();
    UserRole getUserRole();
}
