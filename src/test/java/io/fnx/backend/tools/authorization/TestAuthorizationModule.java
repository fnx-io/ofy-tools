package io.fnx.backend.tools.authorization;

import com.google.inject.AbstractModule;
import com.google.inject.matcher.Matchers;
import io.fnx.backend.tools.auth.Principal;

import javax.inject.Provider;
import java.util.ArrayList;
import java.util.Arrays;

class TestAuthorizationModule extends AbstractModule {

    private final Provider<Principal> principalProvider;
    private final boolean strict;

    TestAuthorizationModule(Provider<Principal> principalProvider, boolean strict) {
        this.principalProvider = principalProvider;
        this.strict = strict;
    }

    @Override
    protected void configure() {
        final AuthorizationInterceptor fnxAuthorizationInterceptor = new AuthorizationInterceptor(principalProvider, strict);
        fnxAuthorizationInterceptor.setGuards(createAuthorizationGuards());
        requestInjection(fnxAuthorizationInterceptor);
        bindInterceptor(Matchers.subclassesOf(TestResource.class), Matchers.any(), fnxAuthorizationInterceptor);
    }

    private AuthorizationGuard[] createAuthorizationGuards() {
        return new AuthorizationGuard[]{
                new AllowedForAuthenticatedAuthorizationGuard(),
                new AllowedForAdminsAuthorizationGuard(),
                new AllAllowedAuthorizationGuard(),
                new AllowedForRolesAuthorizationGuardImpl(),
                new AllowedForOwnerAuthorizationGuard(),
        };
    }

}