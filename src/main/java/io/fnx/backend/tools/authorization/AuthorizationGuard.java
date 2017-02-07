package io.fnx.backend.tools.authorization;

import com.googlecode.objectify.Key;
import io.fnx.backend.tools.auth.Principal;
import io.fnx.backend.tools.auth.PrincipalRole;
import org.aopalliance.intercept.MethodInvocation;

import java.lang.annotation.Annotation;
import java.util.Collection;

public interface AuthorizationGuard {

    Collection<Class<? extends Annotation>> getAnnotationClasses();

    AuthorizationResult guardInvocation(MethodInvocation invocation,
                                        Annotation annotation,
                                        PrincipalRole callingRole,
                                        Key<? extends Principal> callingPrincipal);
}
