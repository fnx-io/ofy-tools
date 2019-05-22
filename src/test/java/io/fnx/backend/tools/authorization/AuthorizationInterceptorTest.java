package io.fnx.backend.tools.authorization;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import org.junit.Before;
import org.junit.Test;

import java.util.Collections;

import static org.junit.Assert.fail;

public class AuthorizationInterceptorTest {

    protected Injector injector;

    @Before
    public void createInjector() {
        System.out.println("Creating test injector...");
        injector = Guice.createInjector((Module) binder -> {
            binder.bind(TestResource.class).to(TestResourceImpl.class);
            binder.install(new TestAuthorizationModule());
        });
    }

    @Test
    public void testNotAnnotated() {
        TestResource testResource = injector.getInstance(TestResource.class);
        testCallInRole(TestPrincipalRole.ADMIN, false, testResource::notAnnotated);
        testCallInRole(TestPrincipalRole.USER, false, testResource::notAnnotated);
        testCallInRole(TestPrincipalRole.ANONYMOUS, false, testResource::notAnnotated);
    }


    @Test
    public void testAdminAllowed() {
        TestResource testResource = injector.getInstance(TestResource.class);
        testCallInRole(TestPrincipalRole.ADMIN, true, testResource::adminAllowed);
        testCallInRole(TestPrincipalRole.USER, false, testResource::adminAllowed);
        testCallInRole(TestPrincipalRole.ANONYMOUS, false, testResource::adminAllowed);
    }


    void testCallInRole(TestPrincipalRole role, boolean expectedAuthResult, Runnable call) {
        TestPrincipal tester = new TestPrincipal();
        tester.setRoles(Collections.singletonList(role));
        tester.setId(1L);
//        cc().setLoggedUser(tester);

        if (expectedAuthResult) {
            call.run();
        } else {
            try {
                call.run();
                fail("This call should fail for role "+role+", but it was allowed");
            } catch (PermissionDeniedException e) {
                // this is ok
            }
        }
    }

}

