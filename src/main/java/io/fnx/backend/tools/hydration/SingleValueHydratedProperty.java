package io.fnx.backend.tools.hydration;

import com.googlecode.objectify.Key;

/**
 * Descriptor of the property which should be hydrated
 */
public interface SingleValueHydratedProperty<H, P> extends HydratedProperty<H, P> {

    /**
     * @param object the target entity we are hydrating
     * @return a key (or null) of the entity we want to load,
     * to finish hydration of described property
     */
    Key<P> getKey(H object);

}
