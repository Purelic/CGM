package net.purelic.cgm.listeners.match;

import net.purelic.cgm.events.match.MatchCycleEvent;
import net.purelic.cgm.utils.PlayerUtils;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class MatchCycle implements Listener {

    @EventHandler
    public void onMatchCycle(MatchCycleEvent event) {
        if (!event.hasMap()) PlayerUtils.showEveryone();
    }

}
