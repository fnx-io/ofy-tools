package io.fnx.backend.tools.authorization;

import com.googlecode.objectify.Key;
import io.fnx.backend.tools.auth.User;
import io.fnx.backend.tools.auth.UserRole;
import org.aopalliance.intercept.MethodInvocation;

import java.lang.annotation.Annotation;
import java.util.Collection;

public interface AuthorizationGuard {

    Collection<Class<? extends Annotation>> getAnnotationClasses();

    /**
     * Tells to the evaluator, if this guard can be skipped.
     * Most guards are additive, in that if you have 2 guards, and only one
     * is completed successfully for current user (eg role and owner, and the owner is successful),
     * you are authorized to access the resource. Both these are non definitive guards.
     * Example of an definitive guard is such that requires you to have a certain role
     * {@link AllowedForAdminsOnlyAuthorizationGuard} you have to pass this check or else the access is denied.
     * Additive guards can be skipped if we know, that the check already passed. Definitive guards,
     * on the other hand, have to be run always, since they might throw PermissionDeniedExceptions
     *
     * @return true if this guard is required to run every time a check is about to be done.
     * Return false if this guard should be run only if the user had no successful guards run yet.
     */
    boolean isDefinitive();

    AuthorizationResult guardInvocation(MethodInvocation invocation,
                                        Annotation annotation,
                                        UserRole callingRole,
                                        Key<? extends User> callingUser);
}
