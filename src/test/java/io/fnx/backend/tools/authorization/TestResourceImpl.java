package io.fnx.backend.tools.authorization;

class TestResourceImpl implements TestResource {

    @Override
    public void notAnnotated() { }

    @Override
    @AllowedForAdmins
    public void adminAllowed() { }

}
