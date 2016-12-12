package io.fnx.backend.tools.authorization;

import com.googlecode.objectify.Key;
import io.fnx.backend.tools.auth.User;
import io.fnx.backend.tools.auth.UserRole;
import org.aopalliance.intercept.MethodInvocation;

import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.List;

import static java.util.Collections.singletonList;

public class AllowedForAuthenticatedAuthorizationGuard implements AuthorizationGuard {

    private static List<? extends Class<? extends Annotation>> annotations = singletonList(AllowedForAuthenticated.class);

    @Override
    @SuppressWarnings("unchecked")
    public Collection<Class<? extends Annotation>> getAnnotationClasses() {
        return (Collection<Class<? extends Annotation>>) annotations;
    }

    @Override
    public boolean isDefinitive() {
        return false;
    }

    @Override
    public AuthorizationResult guardInvocation(final MethodInvocation invocation,
                                                    final Annotation annotation,
                                                    final UserRole callingRole,
                                                    final Key<? extends User> callingUser) {
        if (callingUser == null || callingRole == null || callingRole.isAnonymous()) {
            return AuthorizationResult.failure("Must authenticate.");
        } else {
            return AuthorizationResult.SUCCESS;
        }

    }
}
