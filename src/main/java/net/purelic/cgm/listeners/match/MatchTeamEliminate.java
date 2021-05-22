package net.purelic.cgm.listeners.match;

import net.purelic.cgm.events.participant.MatchTeamEliminateEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class MatchTeamEliminate implements Listener {

    @EventHandler
    public void onMatchTeamEliminate(MatchTeamEliminateEvent event) {
        event.broadcast();
    }

}
