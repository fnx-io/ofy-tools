package io.fnx.backend.tools.auth;

import com.googlecode.objectify.Key;

import java.util.List;

public interface Principal {

    Key<? extends Principal> getPrincipalKey();

    List<? extends PrincipalRole> getUserRoles();

}
