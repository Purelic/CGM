package net.purelic.cgm.listeners.match;

import net.purelic.cgm.core.managers.MatchManager;
import net.purelic.cgm.core.maps.CustomMap;
import net.purelic.commons.events.MapLoadEvent;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class MapLoad implements Listener {

    @EventHandler
    public void onMapLoad(MapLoadEvent event) {
        World world = event.getWorld();
        CustomMap map = MatchManager.getNextMap();

        world.setGameRuleValue("randomTickSpeed", String.valueOf(map.getYaml().getTickSpeed()));
        map.setNextWorld(world);
    }

}
