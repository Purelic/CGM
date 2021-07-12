package net.purelic.cgm.listeners.modules;

import net.purelic.cgm.events.match.MatchEndEvent;
import net.purelic.cgm.events.match.MatchStartEvent;
import net.purelic.commons.modules.Module;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;

import java.util.ArrayList;
import java.util.List;

public class DynamicModuleModule implements Module {

    private static final List<DynamicModule> MODULES = new ArrayList<>();

    @EventHandler (priority = EventPriority.LOWEST)
    public void onMatchStart(MatchStartEvent event) {
        for (DynamicModule module : MODULES) {
            if (module.isValid()) {
                module.register();
            }
        }
    }

    @EventHandler (priority = EventPriority.LOWEST)
    public void onMatchEnd(MatchEndEvent event) {
        for (DynamicModule module : MODULES) {
            module.unregister();
        }
    }

    public static void add(DynamicModule module) {
        MODULES.add(module);
    }

}
