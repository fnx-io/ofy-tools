package io.fnx.backend.tools;

import com.google.inject.AbstractModule;
import io.fnx.backend.manager.UniqueIndexManager;
import io.fnx.backend.tools.ofy.OfyProvider;
import io.fnx.backend.tools.random.Randomizer;
import io.fnx.backend.tools.random.SecureRandomizer;

import javax.inject.Singleton;

/**
 * Guice module which provides different services
 */
public class FnxGaeToolsModule extends AbstractModule {

    static {
        FnxGaeToolsOfyEntities.registerEntities();
    }

    @Override
    protected void configure() {
        bind(Randomizer.class).to(SecureRandomizer.class).in(Singleton.class);
        bind(UniqueIndexManager.class).in(Singleton.class);
        bind(OfyProvider.class).in(Singleton.class);
    }
}
