package net.purelic.cgm.listeners.modules;

import net.purelic.cgm.core.constants.MatchState;
import net.purelic.cgm.core.runnables.RoundCountdown;
import net.purelic.commons.utils.TaskUtils;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.SpawnerSpawnEvent;

public class SpawnerModule implements Listener {

    @EventHandler
    public void onSpawnerSpawn(SpawnerSpawnEvent event) {
        if (event.isCancelled()) return;

        if (!MatchState.isState(MatchState.STARTED)
            || TaskUtils.isRunning(RoundCountdown.getCountdown())) {
            event.setCancelled(true);
            return;
        }

        Location spawnerLoc = event.getSpawner().getLocation();
        Location newLoc = spawnerLoc.add(0.5, 1, 0.5);
        event.getEntity().teleport(newLoc);
    }

}
