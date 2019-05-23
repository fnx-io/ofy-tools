package io.fnx.backend.tools.authorization;

import io.fnx.backend.tools.auth.Principal;
import io.fnx.backend.tools.auth.PrincipalRole;
import io.fnx.backend.tools.random.StringUtils;
import org.aopalliance.intercept.MethodInvocation;

import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.stream.Collectors;

public interface AuthorizationGuard {

    Collection<Class<? extends Annotation>> getAnnotationClasses();

    AuthorizationResult guardInvocation(MethodInvocation invocation,
                                        Annotation annotation,
                                        Principal principal);

    default String principalRolesToString(Principal principal) {
        return rolesToString(principal != null ? principal.getUserRoles() : null);
    }

    default String rolesToString(Collection<? extends PrincipalRole> roles) {
        if (roles == null || roles.isEmpty()) return "No roles";

        return StringUtils.join(roles, ",");
    }

}
