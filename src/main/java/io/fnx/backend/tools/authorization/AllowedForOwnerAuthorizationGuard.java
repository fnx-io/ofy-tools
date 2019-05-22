package io.fnx.backend.tools.authorization;

import com.googlecode.objectify.Key;
import io.fnx.backend.tools.auth.Principal;
import io.fnx.backend.tools.ofy.OfyProvider;
import org.aopalliance.intercept.MethodInvocation;

import javax.inject.Inject;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static io.fnx.backend.tools.ofy.OfyUtils.idToKey;
import static io.fnx.backend.tools.ofy.OfyUtils.nameToKey;
import static java.lang.String.format;

/**
 * This guard validates, that calling user is owner of all parameters annotated either with
 * {@link IdHasOwner} or {@link KeyHasOwner}, or which implement the {@link OwnedEntity} interface.
 */
public class AllowedForOwnerAuthorizationGuard implements AuthorizationGuard {

    private static final List<Class<? extends Annotation>> annotations;

    static {
        List<Class<? extends Annotation>> res = new ArrayList<>();
        res.add(AllowedForOwner.class);
        annotations = Collections.unmodifiableList(res);
    }

    private OfyProvider ofyProvider;

    @Override
    public Collection<Class<? extends Annotation>> getAnnotationClasses() {
        return annotations;
    }

    @Override
    @SuppressWarnings("unchecked")
    public AuthorizationResult guardInvocation(MethodInvocation invocation, Annotation annotation, Principal principal) {
        final AuthorizationResult failure =
                AuthorizationResult.failure("Insufficient rights to access resource for: " + invocation.getMethod());
        if (principal == null) {
            return failure;
        }

        final Object[] args = invocation.getArguments();
        final Annotation[][] parameterAnnotations = invocation.getMethod().getParameterAnnotations();
        for (int i = 0; i < args.length; i++) {
            final Object arg = args[i];
            final IdHasOwner ownedId = findHasOwnerId(parameterAnnotations[i]);
            final KeyHasOwner paramOwnedKey = findHasOwnerKey(parameterAnnotations[i]);

            final OwnedEntity owned;
            if (arg instanceof OwnedEntity) {
                owned = (OwnedEntity) arg;
            } else if (ownedId != null) {
                final Key<? extends OwnedEntity<?>> ownedKey;
                if (arg instanceof Long) {
                    ownedKey = idToKey(ownedId.value(), (Long) arg);
                } else if (arg instanceof String) {
                    ownedKey = nameToKey(ownedId.value(), (String) arg);
                } else {
                    throw new IllegalArgumentException(format("@IdHasOwner can be used only for Long ids or String names. Not for parameters of type %s", arg.getClass()));
                }
                owned = ofyProvider.get().load().key(ownedKey).now();
            } else if (paramOwnedKey != null) {
                owned = ofyProvider.get().load().key((Key<? extends OwnedEntity<?>>) arg).now();
            } else {
                continue;
            }
            if (owned == null) continue;
            final Key ownerKey = owned.getOwnerKey();
            if (ownerKey == null) continue;

            if (!ownerKey.equals(principal.getPrincipalKey())) return failure;
        }
        return AuthorizationResult.SUCCESS;
    }

    private IdHasOwner findHasOwnerId(final Annotation[] argAnnotations) {
        for (Annotation argAnnotation : argAnnotations) {
            if (argAnnotation instanceof IdHasOwner) return (IdHasOwner) argAnnotation;
        }
        return null;
    }

    private KeyHasOwner findHasOwnerKey(Annotation[] annotations) {
        for (Annotation a : annotations) {
            if (a instanceof KeyHasOwner) return (KeyHasOwner) a;
        }
        return null;
    }

    @Inject
    public void setOfyProvider(OfyProvider ofyProvider) {
        this.ofyProvider = ofyProvider;
    }
}
