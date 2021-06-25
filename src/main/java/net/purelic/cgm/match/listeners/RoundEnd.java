package net.purelic.cgm.match.listeners;

import net.purelic.cgm.events.match.RoundEndEvent;
import net.purelic.cgm.match.MatchRound;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class RoundEnd implements Listener {

    @EventHandler
    public void onRoundEnd(RoundEndEvent event) {
        MatchRound round = null; // event.getRound();

        // if match is over dont send results
        round.sendResults();
    }

}
