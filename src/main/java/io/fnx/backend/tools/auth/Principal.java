package io.fnx.backend.tools.auth;

import com.googlecode.objectify.Key;

public interface Principal {

    Key<? extends Principal> getPrincipalKey();

    PrincipalRole getUserRole();
    
}
