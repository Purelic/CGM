package net.purelic.cgm.listeners.match;

import net.purelic.cgm.core.constants.MatchState;
import net.purelic.cgm.core.managers.MatchManager;
import net.purelic.cgm.core.maps.CustomMap;
import net.purelic.commons.events.MapLoadEvent;
import net.purelic.commons.utils.ServerUtils;
import net.purelic.commons.utils.TaskUtils;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class MapLoad implements Listener {

    @EventHandler
    public void onMapLoad(MapLoadEvent event) {
        World world = event.getWorld();
        CustomMap map = MatchManager.getNextMap();

        world.setGameRuleValue("randomTickSpeed", String.valueOf(map.getYaml().getTickSpeed()));
        world.setStorm(false);
        map.setNextWorld(world);

        // Auto-cycle if they're in the waiting game state
        if (MatchState.isState(MatchState.WAITING)
            || (ServerUtils.isRanked() && !MatchState.isState(MatchState.STARTED))) {
            TaskUtils.runLater(MatchManager::cycle, 20L);
        }
    }

}
