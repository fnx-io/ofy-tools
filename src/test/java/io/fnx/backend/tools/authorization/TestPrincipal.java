package io.fnx.backend.tools.authorization;

import com.googlecode.objectify.Key;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Index;
import io.fnx.backend.tools.auth.Principal;
import io.fnx.backend.tools.auth.PrincipalRole;
import io.fnx.backend.tools.ofy.OfyUtils;

import java.util.List;

public class TestPrincipal implements Principal {

    @Id
    private Long id;

    @Index
    private String email;

    private String firstName;

    @Index
    private String lastName;

    private List<PrincipalRole> roles;


    @Override
    public Key<? extends Principal> getPrincipalKey() {
        return OfyUtils.idToKey(TestPrincipal.class, id);
    }

    @Override
    public List<PrincipalRole> getUserRoles() {
        return roles;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public List<PrincipalRole> getRoles() {
        return roles;
    }

    public void setRoles(List<PrincipalRole> roles) {
        this.roles = roles;
    }
}
