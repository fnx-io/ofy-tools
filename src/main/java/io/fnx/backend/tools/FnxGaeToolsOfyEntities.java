package io.fnx.backend.tools;

import com.googlecode.objectify.ObjectifyService;
import com.googlecode.objectify.impl.translate.opt.joda.JodaTimeTranslators;
import io.fnx.backend.domain.AuthTokenEntity;
import io.fnx.backend.domain.UniqueIndexEntity;

public class FnxGaeToolsOfyEntities {

    static {
        JodaTimeTranslators.add(ObjectifyService.factory());
    }

    private FnxGaeToolsOfyEntities() {}

    public static void registerEntities() {
        ObjectifyService.register(AuthTokenEntity.class);
        ObjectifyService.register(UniqueIndexEntity.class);
    }
}
