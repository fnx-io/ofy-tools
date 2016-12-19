package io.fnx.backend.tools.hydration;

import com.googlecode.objectify.Key;

import java.util.Collection;
import java.util.List;

public interface CollectionHydratedProperty<H, P> extends HydratedProperty<H, List<P>> {

    Collection<Key<P>> getKeys(H object);
}
