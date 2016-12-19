package io.fnx.backend.tools.hydration;

public interface HydratedProperty<H, P> {
    /**
     * The entity this property is bound had been loaded (or null).
     * Now set it into the hydration target entity.
     *
     * @param object the target entity the result should be set to
     * @param entity the entity as loaded from datastore
     */
    void setProperty(H object, P entity);
}
