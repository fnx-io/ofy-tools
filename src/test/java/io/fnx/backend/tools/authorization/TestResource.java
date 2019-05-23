package io.fnx.backend.tools.authorization;

interface TestResource {

    void notAnnotated();

    void allAllowed();

    void adminAllowed();

    void authenticatedAllowed();

    void backendFrontendUsersAllowed();

}
