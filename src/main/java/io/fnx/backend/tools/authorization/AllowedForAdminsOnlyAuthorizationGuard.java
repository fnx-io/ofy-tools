package io.fnx.backend.tools.authorization;

import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * This guard validates, that calling user is in admin role.
 *
 * It is a definitive guard, if this check fails, the call will be prevented
 * and authorization error will be raised.
 *
 * @see AuthorizationGuard#isDefinitive
 */
public class AllowedForAdminsOnlyAuthorizationGuard extends AllowedForAdminsAuthorizationGuard implements AuthorizationGuard {

    private final static List<Class<? extends Annotation>> annotations;
    static {
        final List<Class<? extends Annotation>> res = new LinkedList<>();
        res.add(AllowedForAdminsOnly.class);
        annotations = Collections.unmodifiableList(res);
    }

    @Override
    @SuppressWarnings("unchecked")
    public Collection<Class<? extends Annotation>> getAnnotationClasses() {
        return annotations;
    }

    @Override
    public boolean isDefinitive() {
        return true;
    }
}
