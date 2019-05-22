package io.fnx.backend.tools.authorization;

import io.fnx.backend.tools.auth.Principal;
import org.aopalliance.intercept.MethodInvocation;

import java.lang.annotation.Annotation;
import java.util.Collection;

public interface AuthorizationGuard {

    Collection<Class<? extends Annotation>> getAnnotationClasses();

    AuthorizationResult guardInvocation(MethodInvocation invocation,
                                        Annotation annotation,
                                        Principal principal);

}
