package io.fnx.backend.tools.authorization;

import com.googlecode.objectify.Key;
import io.fnx.backend.tools.auth.User;
import io.fnx.backend.tools.auth.UserContext;
import io.fnx.backend.tools.auth.UserRole;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;

import javax.inject.Inject;
import javax.inject.Provider;
import java.lang.annotation.Annotation;
import java.util.Collection;

/**
 * Main class which should be configured to intercept calls to methods you care about (service methods?) and
 * you want it to check for authorization of these calls.
 *
 * It has simple (plugin) architecture, in that it consists of sets of {@link AuthorizationGuard}s which react to
 * different annotations and different checks.
 * <br>
 * <br>
 * Sample configuration for a Guice based application:
 * <br>
 * <br>
 * <pre>
 *     // be sure to have provider for UserContext
 *     bind(UserContext.class).toProvider(UserContextProvider.class).in(RequestScoped.class);
 *
 *     final AuthorizationInterceptor fnxAuthorizationInterceptor = new AuthorizationInterceptor();
 *     // select your own set of guards
 *     fnxAuthorizationInterceptor.setGuards(createAuthorizationGuards());
 *     requestInjection(fnxAuthorizationInterceptor);
 *     // intercept calls to service methods and check the authorization of these calls
 *     bindInterceptor(Matchers.subclassesOf(Service.class), Matchers.any(), fnxAuthorizationInterceptor);
 * </pre>
 *
 * @see AuthorizationGuard
 * @see UserContext
 * @see User
 */
public class AuthorizationInterceptor implements MethodInterceptor {

    private Provider<UserContext> userCtxProvider;
    private AuthorizationGuard[] guards = new AuthorizationGuard[0];

    @Override
    public Object invoke(MethodInvocation invocation) throws Throwable {
        final UserContext context = userCtxProvider.get();
        final AuthorizationResult result = runGuards(invocation, context);

        if (result == null || result.success) {
            return invocation.proceed();
        } else {
            throw new PermissionDeniedException(result.msg);
        }
    }

    private AuthorizationResult runGuards(MethodInvocation invocation, UserContext context) {
        AuthorizationResult result = null;
        final Key<? extends User> callingUser = context.getUserKey();
        final UserRole callingRole = context.getUserRole();

        for (final AuthorizationGuard guard : guards) {
            // skip if the guard is non-definitive and authorization had already been successful
            if (!guard.isDefinitive() && result != null && result.success) continue;
            final Collection<Class<? extends Annotation>> annotationClasses = guard.getAnnotationClasses();
            if (annotationClasses == null) continue;
            for (Class<? extends Annotation> annotationClass : annotationClasses) {
                if (annotationClass == null) continue;
                final Annotation annotation = invocation.getMethod().getAnnotation(annotationClass);
                if (annotation != null) {
                    final AuthorizationResult inspectionResult = guard.guardInvocation(invocation, annotation, callingRole, callingUser);
                    if (result == null) { // seed authorization outcome from the first result
                        result = inspectionResult;
                    } else if (inspectionResult != null && inspectionResult.success) { // allow the total outcome to be changed to success
                        result = inspectionResult;                                     // so when first authorization guard suggests failure
                                                                                       // other guards might allow the call (they are additive)
                    } else if (inspectionResult != null && !inspectionResult.success && guard.isDefinitive()) { // definitive guards must succeed every time
                        return result;
                    }
                }
            }
        }
        return result;
    }

    @Inject
    public void setUserCtxProvider(Provider<UserContext> userCtxProvider) {
        this.userCtxProvider = userCtxProvider;
    }

    public void setGuards(AuthorizationGuard[] guards) {
        if (guards != null) {
            this.guards = guards;
        } else {
            this.guards = new AuthorizationGuard[]{};
        }
    }
}