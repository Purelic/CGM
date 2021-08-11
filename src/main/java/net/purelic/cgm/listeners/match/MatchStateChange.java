package net.purelic.cgm.listeners.match;

import net.purelic.cgm.CGM;
import net.purelic.cgm.commands.toggles.ToggleAutoStartCommand;
import net.purelic.cgm.core.constants.MatchState;
import net.purelic.cgm.core.managers.MatchManager;
import net.purelic.cgm.core.managers.ScoreboardManager;
import net.purelic.cgm.core.runnables.StartCountdown;
import net.purelic.cgm.events.match.MatchEndEvent;
import net.purelic.cgm.events.match.MatchStartEvent;
import net.purelic.cgm.events.match.MatchStateChangeEvent;
import net.purelic.commons.Commons;
import net.purelic.commons.utils.ServerUtils;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class MatchStateChange implements Listener {

    private final ScoreboardManager scoreboardManager;
    private final MatchManager matchManager;

    public MatchStateChange() {
        this.scoreboardManager = CGM.get().getScoreboardManager();
        this.matchManager = CGM.get().getMatchManager();
    }

    @EventHandler
    public void onMatchStateChange(MatchStateChangeEvent event) {
        MatchState newState = event.getNewState();
        boolean forced = event.isForced();
        int seconds = event.getSeconds();

        if (StartCountdown.getCountdown() != null) {
            StartCountdown.getCountdown().cancel();
        }

        if (newState == MatchState.WAITING) {
            ScoreboardManager.resetScores(0);
            ScoreboardManager.updateWaitingSidebar(false);
        } else if (newState == MatchState.STARTING) {
            if (ToggleAutoStartCommand.autostart || forced) {
                if (ServerUtils.isRanked()) seconds = 60;

                new StartCountdown(seconds, forced).runTaskTimer(CGM.get(), 0, 20);
            } else {
                MatchState.setState(MatchState.PRE_GAME);
            }
        } else if (newState == MatchState.STARTED) {
            Commons.callEvent(new MatchStartEvent(MatchManager.getCurrentMap(), MatchManager.getCurrentGameMode(), event.isForced()));
        } else if (newState == MatchState.ENDED) {
            Commons.callEvent(new MatchEndEvent(MatchManager.getCurrentMap(), MatchManager.getCurrentGameMode(), event.isForced()));
        }
    }

}
