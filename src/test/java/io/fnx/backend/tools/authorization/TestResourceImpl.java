package io.fnx.backend.tools.authorization;

class TestResourceImpl implements TestResource {

    @Override
    public void notAnnotated() {
    }

    @Override
    @AllAllowed
    public void allAllowed() {
    }

    @Override
    @AllowedForAdmins
    public void adminAllowed() {
    }

    @Override
    @AllowedForAuthenticated
    public void authenticatedAllowed() {
    }

    @Override
    @AllowedForRoles({TestPrincipalRole.BACKEND_USER, TestPrincipalRole.FRONTEND_USER})
    public void backendFrontendUsersAllowed() {
    }

}
