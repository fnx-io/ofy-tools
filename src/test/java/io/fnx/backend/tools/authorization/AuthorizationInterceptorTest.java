package io.fnx.backend.tools.authorization;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import io.fnx.backend.tools.auth.PrincipalRole;
import io.fnx.backend.tools.random.StringUtils;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.Assert.fail;

public class AuthorizationInterceptorTest {

    private Injector injector;

    private TestPrincipal testLoggedUser;

    public TestPrincipal getTestLoggedUser() {
        return testLoggedUser;
    }

    @Before
    public void setUp() {
        injector = createInjector(true);
    }

    private Injector createInjector(boolean strictInterceptor) {
        System.out.println("Creating test injector, strict interceptor: " + strictInterceptor);
        return Guice.createInjector((Module) binder -> {
            binder.bind(TestResource.class).to(TestResourceImpl.class);
            binder.install(new TestAuthorizationModule(this::getTestLoggedUser, strictInterceptor));
        });
    }

    @Test
    public void testNotAnnotated() {
        TestResource testResource = injector.getInstance(TestResource.class);
        testCallInRole(TestPrincipalRole.ADMIN, false, testResource::notAnnotated);
        testCallInRole(TestPrincipalRole.FRONTEND_USER, false, testResource::notAnnotated);
        testCallInRole(TestPrincipalRole.BACKEND_USER, false, testResource::notAnnotated);
        testCallInRole(TestPrincipalRole.ANONYMOUS, false, testResource::notAnnotated);
    }

    @Test
    public void testNotAnnotatedWithNotStrictInterceptor() {
        injector = createInjector(false);
        TestResource testResource = injector.getInstance(TestResource.class);
        testCallInRole(TestPrincipalRole.ADMIN, true, testResource::notAnnotated);
        testCallInRole(TestPrincipalRole.FRONTEND_USER, true, testResource::notAnnotated);
        testCallInRole(TestPrincipalRole.BACKEND_USER, true, testResource::notAnnotated);
        testCallInRole(TestPrincipalRole.ANONYMOUS, true, testResource::notAnnotated);
    }

    @Test
    public void testAllAllowed() {
        TestResource testResource = injector.getInstance(TestResource.class);
        testCallInRole(TestPrincipalRole.ADMIN, true, testResource::allAllowed);
        testCallInRole(TestPrincipalRole.FRONTEND_USER, true, testResource::allAllowed);
        testCallInRole(TestPrincipalRole.BACKEND_USER, true, testResource::allAllowed);
        testCallInRole(TestPrincipalRole.ANONYMOUS, true, testResource::allAllowed);
    }

    @Test
    public void testAdminAllowed() {
        TestResource testResource = injector.getInstance(TestResource.class);
        testCallInRole(TestPrincipalRole.ADMIN, true, testResource::adminAllowed);
        testCallInRole(TestPrincipalRole.FRONTEND_USER, false, testResource::adminAllowed);
        testCallInRole(TestPrincipalRole.BACKEND_USER, false, testResource::adminAllowed);
        testCallInRole(TestPrincipalRole.ANONYMOUS, false, testResource::adminAllowed);
    }

    @Test
    public void testAuthenticatedAllowed() {
        TestResource testResource = injector.getInstance(TestResource.class);
        testCallInRole(TestPrincipalRole.ADMIN, true, testResource::authenticatedAllowed);
        testCallInRole(TestPrincipalRole.FRONTEND_USER, true, testResource::authenticatedAllowed);
        testCallInRole(TestPrincipalRole.BACKEND_USER, true, testResource::authenticatedAllowed);
        testCallInRole(TestPrincipalRole.ANONYMOUS, false, testResource::authenticatedAllowed);
    }

    @Test
    public void testRolesAllowed() {
        TestResource testResource = injector.getInstance(TestResource.class);
        testCallInRole(TestPrincipalRole.ADMIN, false, testResource::backendFrontendUsersAllowed);
        testCallInRole(TestPrincipalRole.FRONTEND_USER, true, testResource::backendFrontendUsersAllowed);
        testCallInRole(TestPrincipalRole.BACKEND_USER, true, testResource::backendFrontendUsersAllowed);
        testCallInRole(TestPrincipalRole.ANONYMOUS, false, testResource::backendFrontendUsersAllowed);
    }

    @Test
    public void testUserWithMultipleRoles() {
        TestResource testResource = injector.getInstance(TestResource.class);
        testCallInRoles(Arrays.asList(TestPrincipalRole.ADMIN, TestPrincipalRole.ANONYMOUS), false, testResource::backendFrontendUsersAllowed);
        testCallInRoles(Arrays.asList(TestPrincipalRole.FRONTEND_USER, TestPrincipalRole.BACKEND_USER), true, testResource::backendFrontendUsersAllowed);
        testCallInRoles(Arrays.asList(TestPrincipalRole.BACKEND_USER, TestPrincipalRole.ANONYMOUS), true, testResource::backendFrontendUsersAllowed);
        testCallInRoles(Arrays.asList(TestPrincipalRole.FRONTEND_USER, TestPrincipalRole.ADMIN), true, testResource::backendFrontendUsersAllowed);
        testCallInRoles(Collections.emptyList(), false, testResource::backendFrontendUsersAllowed);
    }

    protected void testCallInRole(TestPrincipalRole role, boolean expectedAuthResult, Runnable call) {
        testCallInRoles(Collections.singletonList(role), expectedAuthResult, call);
    }

    protected void testCallInRoles(List<TestPrincipalRole> roles, boolean expectedAuthResult, Runnable call) {
        TestPrincipal tester = new TestPrincipal();
        tester.setRoles(roles);
        tester.setId(1L);
        testLoggedUser = tester;

        if (expectedAuthResult) {
            call.run();
        } else {
            try {
                call.run();
                fail("The call '" + call + "' should fail for value " + rolesToString(roles) + ", but it was allowed");
            } catch (PermissionDeniedException e) {
                // this is ok
                System.out.println("Expected failure: " + e);
            }
        }
    }

    private String rolesToString(Collection<? extends PrincipalRole> roles) {
        if (roles == null || roles.isEmpty()) return "No value";

        return StringUtils.join(roles, ",");
    }

}

