package io.fnx.backend.domain;

import com.googlecode.objectify.Key;
import com.googlecode.objectify.annotation.Cache;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Unindex;

import static io.fnx.backend.tools.ofy.OfyUtils.nameToKey;
import static java.lang.String.format;

@Entity
@Unindex
@Cache(expirationSeconds = 120 * 60)
public class UniqueIndexEntity {

    @Id
    private String uniqueKey;

    private Key<?> ownerId;

    public static <T extends Enum<T>> Key<UniqueIndexEntity> createKey(Enum<T> property, String uniqueKey) {
        return nameToKey(UniqueIndexEntity.class, format("%s#%s", property, uniqueKey));
    }

    @Override
    public String toString() {
        return "UniqueIndexEntity{" +
                "uniqueKey='" + uniqueKey + '\'' +
                ", ownerId=" + ownerId +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof UniqueIndexEntity)) return false;

        UniqueIndexEntity that = (UniqueIndexEntity) o;

        if (uniqueKey != null ? !uniqueKey.equals(that.uniqueKey) : that.uniqueKey != null) return false;
        return ownerId != null ? ownerId.equals(that.ownerId) : that.ownerId == null;
    }

    @Override
    public int hashCode() {
        int result = uniqueKey != null ? uniqueKey.hashCode() : 0;
        result = 31 * result + (ownerId != null ? ownerId.hashCode() : 0);
        return result;
    }

    public String getUniqueKey() {
        return uniqueKey;
    }

    public void setUniqueKey(String uniqueKey) {
        this.uniqueKey = uniqueKey;
    }

    public Key<?> getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(Key<?> ownerId) {
        this.ownerId = ownerId;
    }
}
