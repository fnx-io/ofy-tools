package io.fnx.backend.tools.authorization;

import com.google.inject.AbstractModule;
import com.google.inject.matcher.Matchers;

import java.util.ArrayList;

class TestAuthorizationModule extends AbstractModule {

    @Override
    protected void configure() {
        final AuthorizationInterceptor fnxAuthorizationInterceptor = new AuthorizationInterceptor(TestPrincipal::new);
        fnxAuthorizationInterceptor.setGuards(createAuthorizationGuards());
        requestInjection(fnxAuthorizationInterceptor);
        bindInterceptor(Matchers.subclassesOf(TestResource.class), Matchers.any(), fnxAuthorizationInterceptor);
    }

    private AuthorizationGuard[] createAuthorizationGuards() {
        final ArrayList<AuthorizationGuard> guards = new ArrayList<>();
        guards.add(new AllowedForAuthenticatedAuthorizationGuard());
        guards.add(new AllowedForAdminsAuthorizationGuard());
        final AllowedForOwnerAuthorizationGuard ownerGuard = new AllowedForOwnerAuthorizationGuard();
        requestInjection(ownerGuard);
        guards.add(ownerGuard);

        return guards.toArray(new AuthorizationGuard[0]);
    }

}