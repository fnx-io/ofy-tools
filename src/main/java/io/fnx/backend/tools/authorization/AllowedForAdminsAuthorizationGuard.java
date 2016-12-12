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

/**
 * This guard validates, that calling user has Admin role.
 *
 * It is a non-definitive guard.
 *
 * @see AuthorizationGuard#isDefinitive()
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
    public boolean isDefinitive() {
        return false;
    }

    public AuthorizationResult guardInvocation(final MethodInvocation invocation,
                                               final Annotation annotation,
                                               final UserRole callingRole,
                                               final Key<? extends User> callingUser) {
        if (callingRole == null || !callingRole.isAdmin()) {
            return AuthorizationResult.failure("Administrator required");
        } else {
            return AuthorizationResult.SUCCESS;
        }
    }
}
