package net.purelic.cgm.listeners.match;

import net.purelic.cgm.CGM;
import net.purelic.cgm.core.constants.MatchState;
import net.purelic.cgm.events.match.MatchVoteEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class MatchVote implements Listener {

    @EventHandler
    public void onMatchVote(MatchVoteEvent event) {
        if (MatchState.isState(MatchState.VOTING)) {
            CGM.getPlugin().getVoteManager().vote(event.getPlayer(), event.getMap());
        }
    }

}
