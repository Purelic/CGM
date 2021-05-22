package net.purelic.cgm.listeners.flag;

import net.purelic.cgm.core.constants.MatchState;
import net.purelic.cgm.core.gamemodes.NumberSetting;
import net.purelic.cgm.core.maps.flag.events.FlagReturnEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class FlagReturn implements Listener {

    @EventHandler
    public void onFlagReturn(FlagReturnEvent event) {
        if (MatchState.isState(MatchState.STARTED)
                && NumberSetting.FLAG_COLLECTION_INTERVAL.value() == 0) {
            event.broadcast(); // don't broadcast when flags are collected
        }

        event.getFlag().returnFlag();
    }

}
