package net.purelic.cgm.listeners.match;

import net.purelic.cgm.core.managers.MatchManager;
import net.purelic.commons.events.MapLoadEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class MapLoad implements Listener {

    @EventHandler
    public void onMapLoad(MapLoadEvent event) {
        MatchManager.getNextMap().setNextWorld(event.getWorld());
    }

}
