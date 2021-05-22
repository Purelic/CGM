package net.purelic.cgm.listeners.match;

import net.purelic.cgm.CGM;
import net.purelic.cgm.commands.toggles.ToggleAutoStartCommand;
import net.purelic.cgm.core.constants.MatchState;
import net.purelic.cgm.core.managers.MatchManager;
import net.purelic.cgm.core.managers.ScoreboardManager;
import net.purelic.cgm.core.managers.VoteManager;
import net.purelic.cgm.core.runnables.StartCountdown;
import net.purelic.cgm.core.runnables.VotingCountdown;
import net.purelic.cgm.events.match.MatchEndEvent;
import net.purelic.cgm.events.match.MatchStartEvent;
import net.purelic.cgm.events.match.MatchStateChangeEvent;
import net.purelic.commons.Commons;
import net.purelic.commons.utils.ServerUtils;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class MatchStateChange implements Listener {

    private final VoteManager voteManager;
    private final ScoreboardManager scoreboardManager;
    private final MatchManager matchManager;

    public MatchStateChange() {
        this.voteManager = CGM.getPlugin().getVoteManager();
        this.scoreboardManager = CGM.getPlugin().getScoreboardManager();
        this.matchManager = CGM.getPlugin().getMatchManager();
    }

    @EventHandler
    public void onMatchStateChange(MatchStateChangeEvent event) {
        MatchState newState = event.getNewState();
        boolean forced = event.isForced();
        int seconds = event.getSeconds();

        if (VotingCountdown.getCountdown() != null) {
            VotingCountdown.getCountdown().cancel();
            VotingCountdown.clearVotingItems();
        }

        if (StartCountdown.getCountdown() != null) {
            StartCountdown.getCountdown().cancel();
        }

        if (newState == MatchState.WAITING) {
            ScoreboardManager.resetScores(0);
            this.scoreboardManager.updateWaitingSidebar(false);
        } else if (newState == MatchState.VOTING) {
            this.voteManager.startVoting(seconds, forced);
        } else if (newState == MatchState.STARTING) {
            if (ToggleAutoStartCommand.autostart || forced) {
                new StartCountdown(ServerUtils.isRanked() ? 60 : seconds, forced).runTaskTimer(CGM.getPlugin(), 0, 20);
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
