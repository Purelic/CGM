package net.purelic.cgm.listeners.modules;

import net.purelic.commons.modules.Module;

public interface DynamicModule extends Module {

    default boolean isValid() {
        return true;
    }

}
