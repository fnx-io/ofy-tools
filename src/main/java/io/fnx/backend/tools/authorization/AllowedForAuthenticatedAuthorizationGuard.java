package io.fnx.backend.tools.authorization;

import com.googlecode.objectify.Key;
import io.fnx.backend.tools.auth.User;
import io.fnx.backend.tools.auth.UserRole;
import org.aopalliance.intercept.MethodInvocation;

import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class AllowedForAuthenticatedAuthorizationGuard implements AuthorizationGuard {

    private final static List<Class<? extends Annotation>> annotations;

    static {
        final List<Class<? extends Annotation>> res = new LinkedList<>();
        res.add(AllowedForAuthenticated.class);
        annotations = Collections.unmodifiableList(res);
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
