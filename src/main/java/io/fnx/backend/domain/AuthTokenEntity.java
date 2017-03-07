package io.fnx.backend.domain;

import com.googlecode.objectify.Key;
import com.googlecode.objectify.annotation.*;
import io.fnx.backend.tools.auth.Principal;
import io.fnx.backend.tools.authorization.OwnedEntity;
import org.joda.time.DateTime;

import static io.fnx.backend.tools.ofy.OfyUtils.nameToKey;

@Cache
@Entity
@Unindex
public class AuthTokenEntity implements OwnedEntity<Principal> {

    @Id
    private String id;

    @Index
    private DateTime lastTouch;

    @Index
    private Key<? extends Principal> owner;

    public Key<AuthTokenEntity> createKey() {
        return createKey(id);
    }

    public static Key<AuthTokenEntity> createKey(String id) {
        return nameToKey(AuthTokenEntity.class, id);
    }

    @Override
    @SuppressWarnings("unchecked")
    public Key<Principal> getOwnerKey() {
        return (Key<Principal>) owner;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public DateTime getLastTouch() {
        return lastTouch;
    }

    public void setLastTouch(DateTime lastTouch) {
        this.lastTouch = lastTouch;
    }

    public Key<? extends Principal> getOwner() {
        return owner;
    }

    public void setOwner(Key<? extends Principal> owner) {
        this.owner = owner;
    }
}
