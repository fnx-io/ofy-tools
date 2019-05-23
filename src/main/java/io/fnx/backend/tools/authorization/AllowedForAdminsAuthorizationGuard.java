package io.fnx.backend.tools.authorization;

import io.fnx.backend.tools.auth.Principal;
import org.aopalliance.intercept.MethodInvocation;

import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * This guard validates, that calling user has Admin role.
 */
public class AllowedForAdminsAuthorizationGuard implements AuthorizationGuard {

    private final static List<Class<? extends Annotation>> annotations;

    static {
        final List<Class<? extends Annotation>> res = new LinkedList<>();
        res.add(AllowedForAdmins.class);
        annotations = Collections.unmodifiableList(res);
    }

    @Override
    @SuppressWarnings("unchecked")
    public Collection<Class<? extends Annotation>> getAnnotationClasses() {
        return annotations;
    }

    @Override
    public AuthorizationResult guardInvocation(MethodInvocation invocation, Annotation annotation, Principal principal) {
        return principal != null && principal.hasAdminRole()
                ? AuthorizationResult.SUCCESS
                : AuthorizationResult.failure("Administrator role required for user to call method " + invocation.getMethod()
                + ", current roles: " + principalRolesToString(principal));
    }

}
