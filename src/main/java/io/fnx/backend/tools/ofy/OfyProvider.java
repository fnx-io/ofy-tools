package io.fnx.backend.tools.ofy;

import com.googlecode.objectify.Objectify;
import com.googlecode.objectify.ObjectifyService;

import javax.inject.Provider;

/**
 * Provides access to objectify.
 */
public class OfyProvider implements Provider<Objectify> {

    /**
     * @return {@link Objectify} for the current context
     */
    public Objectify get() {
        return ObjectifyService.ofy();
    }
}
