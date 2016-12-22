package io.fnx.backend.manager;

import com.googlecode.objectify.Key;
import com.googlecode.objectify.Objectify;
import com.googlecode.objectify.VoidWork;
import io.fnx.backend.domain.AuthTokenEntity;
import io.fnx.backend.tools.auth.AuthTokenMapper;
import io.fnx.backend.tools.auth.Principal;
import io.fnx.backend.tools.ofy.OfyProvider;
import io.fnx.backend.tools.random.Randomizer;
import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;

import static io.fnx.backend.tools.ofy.OfyUtils.loadKey;
import static java.lang.String.format;

/**
 * AuthTokenManager generates and handles authentication tokens for users.
 *
 * Auth token is a random string which has relation to an user and also validity period.
 *
 * After the validity period expires the token cannot be used again.
 * Tokens are automatically renewed when used via {@link #useToken(String)}
 *
 * @param <T> concrete principal type
 */
public class AuthTokenManager<T extends Principal> implements AuthTokenMapper<T> {

    private Logger log = LoggerFactory.getLogger(AuthTokenMapper.class);

    private Duration tokenValidDuration = Duration.standardDays(21);

    private OfyProvider ofyProvider;

    private Randomizer randomizer;

    private short tokenLength = 22; // +- 128bits in base64 encoding

    @Override
    public T useToken(String token) {
        final AuthTokenEntity entity = getAuthToken(token);
        if (entity == null || entity.getLastTouch() == null || entity.getOwner() == null) {
            log.info(format("Auth token [%s] is invalid", token));
            return null;
        }
        final DateTime validityBoundary = DateTime.now().minus(getTokenValidDuration());
        if (entity.getLastTouch().isBefore(validityBoundary)) {
            log.info(format("Auth token [%s] had expired at [%s]", token, entity.getLastTouch().plus(getTokenValidDuration())));
            return null;
        }
        touchToken(entity);
        @SuppressWarnings("unchecked")
        final T principal = (T) loadKey(entity.getOwner());
        return principal;
    }

    /**
     * Changes the {@link AuthTokenEntity#lastTouch} property to current time.
     * It does so only every so often to save writes (this is called during each request normally).
     * So last touch should change only once a day and then it will be skipped for every successive
     * request for next 24hours
     *
     * @param entity the auth token to update
     * @return true if the time had been updated or false when it was not necessary
     */
    protected boolean touchToken(AuthTokenEntity entity) {
        if (entity == null || entity.getLastTouch() == null) return false;

        final DateTime now = DateTime.now();
        final DateTime yesterday = now.minusDays(1);
        if (entity.getLastTouch().isBefore(yesterday)) {
            entity.setLastTouch(now);
            // do not wait for data store to finish
            // it is not that important operation
            ofy().save().entity(entity);
            return true;
        } else {
            return false;
        }
    }

    /**
     * Creates new authentication token for given user
     *
     * @param user the user to create token for
     * @return created token
     */
    public String newAuthTokenFor(T user) {
        if (user == null) throw new NullPointerException("User must not be empty!");

        final Key<? extends Principal> principalKey = user.getPrincipalKey();
        final String token = randomizer.randomBase64(getTokenLength());

        final AuthTokenEntity entity = new AuthTokenEntity();
        entity.setId(token);
        entity.setLastTouch(DateTime.now());
        entity.setOwner(principalKey);

        ofy().transact(new VoidWork() {
            @Override
            public void vrun() {
                ofy().save().entity(entity).now();
            }
        });
        return token;
    }

    /**
     * Finds the appropriate auth token entity for given token
     *
     * @param token the token to load
     * @return entity for given token, or null if no such entity exists
     */
    public AuthTokenEntity getAuthToken(String token) {
        if (token == null || token.isEmpty()) return null;
        return ofy().load().key(AuthTokenEntity.createKey(token)).now();
    }

    /**
     * Completely destroy given token
     *
     * @param token the token to be destroyed
     */
    public void destroyToken(String token) {
        if (token == null) return;

        final Key<AuthTokenEntity> authTokenKey = AuthTokenEntity.createKey(token);
        ofy().transact(new VoidWork() {
            @Override
            public void vrun() {
                ofy().delete().key(authTokenKey).now();
            }
        });
        log.info(format("Auth token [%s] has been deleted", authTokenKey.getName()));
    }

    protected Objectify ofy() {
        return ofyProvider.get();
    }

    public Duration getTokenValidDuration() {
        return tokenValidDuration;
    }

    public short getTokenLength() {
        return tokenLength;
    }

    @Inject
    public void setOfyProvider(OfyProvider ofyProvider) {
        this.ofyProvider = ofyProvider;
    }

    @Inject
    public void setRandomizer(Randomizer randomizer) {
        this.randomizer = randomizer;
    }

    /**
     * Allows to parameterize the token length.
     * Tokens are base64bit (6bits per character) and should be at least 128 bits long
     *
     * @param tokenLength new token length to use
     */
    public void setTokenLength(short tokenLength) {
        this.tokenLength = tokenLength;
    }

    /**
     * The duration for which the token is considered valid after its last use
     *
     * @param tokenValidDuration validity duration of any given token
     */
    public void setTokenValidDuration(Duration tokenValidDuration) {
        this.tokenValidDuration = tokenValidDuration;
    }
}
