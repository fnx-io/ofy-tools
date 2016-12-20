package io.fnx.backend.tools.authorization;

import com.googlecode.objectify.Key;
import io.fnx.backend.tools.auth.Principal;
import io.fnx.backend.tools.auth.PrincipalRole;
import org.aopalliance.intercept.MethodInvocation;

import java.lang.annotation.Annotation;
import java.util.*;

/**
 * This guard allows to implement role based authorization checks.
 *
 * It does not prescribe any Annotations to be inspected, rather it will just delegate to
 * the implementation to extract allowed roles for a particular call.
 *
 * @param <T> type of the annotation, this guard should be reacting to
 */
public abstract class AllowedForRolesAuthorizationGuard<T extends Annotation> implements AuthorizationGuard {

    private final List<Class<? extends Annotation>> annotations;

    public AllowedForRolesAuthorizationGuard(Class<T> annotation) {
        if (annotation == null) throw new IllegalArgumentException("Role annotation cannot be null");
        final LinkedList<Class<? extends Annotation>> l = new LinkedList<>();
        l.add(annotation);
        this.annotations = Collections.unmodifiableList(l);
    }

    @Override
    public Collection<Class<? extends Annotation>> getAnnotationClasses() {
        return annotations;
    }

    @Override
    public boolean isDefinitive() {
        return false;
    }

    @Override
    public AuthorizationResult guardInvocation(MethodInvocation invocation,
                                               Annotation annotation,
                                               PrincipalRole callingRole,
                                               Key<? extends Principal> callingUser) {
        if (annotation == null) return AuthorizationResult.SUCCESS;

        @SuppressWarnings("unchecked")
        Collection<PrincipalRole> roles = getRoles((T) annotation);
        for (PrincipalRole role : roles) {
            if (Objects.equals(callingRole, role)) return AuthorizationResult.SUCCESS;
        }
        return AuthorizationResult.failure("Insufficient role");
    }

    public abstract Collection<PrincipalRole> getRoles(T annotation);
}
