package net.purelic.cgm.listeners.flag;

import net.purelic.cgm.core.gamemodes.NumberSetting;
import net.purelic.cgm.core.maps.flag.Flag;
import net.purelic.cgm.core.maps.flag.events.FlagRespawnEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class FlagRespawn implements Listener {

    @EventHandler
    public void onFlagRespawn(FlagRespawnEvent event) {
        Flag flag = event.getFlag();

        flag.resetBase();
        flag.clearCarrier();
        flag.startRespawn();

        if (NumberSetting.FLAG_COLLECTION_INTERVAL.value() == 0) { // don't broadcast when flags are collected
            event.broadcast();
        }
    }

}
