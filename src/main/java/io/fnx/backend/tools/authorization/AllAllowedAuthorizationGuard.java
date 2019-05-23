package io.fnx.backend.tools.authorization;

import io.fnx.backend.tools.auth.Principal;
import org.aopalliance.intercept.MethodInvocation;

import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.Collections;

public class AllAllowedAuthorizationGuard implements AuthorizationGuard {

    private static final Collection<Class<? extends Annotation>> annotationClasses = Collections.singletonList(AllAllowed.class);

    @Override
    public Collection<Class<? extends Annotation>> getAnnotationClasses() {
        return annotationClasses;
    }

    @Override
    public AuthorizationResult guardInvocation(MethodInvocation invocation, Annotation annotation, Principal principal) {
        return AuthorizationResult.SUCCESS;
    }
}
