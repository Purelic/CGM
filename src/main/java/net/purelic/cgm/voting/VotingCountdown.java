package net.purelic.cgm.voting;

import net.purelic.cgm.core.constants.MatchState;
import net.purelic.cgm.core.managers.MatchManager;
import net.purelic.cgm.core.runnables.CycleCountdown;
import net.purelic.cgm.match.countdowns.Countdown;
import net.purelic.commons.utils.CommandUtils;
import net.purelic.commons.utils.TaskUtils;
import org.bukkit.Bukkit;

public class VotingCountdown extends Countdown {

    private final VotingManager votingManager;
    private final VotingSettings settings;
    private final boolean forced;

    public VotingCountdown(VotingManager votingManager, int seconds, boolean forced) {
        super(seconds, "Voting ends");
        this.votingManager = votingManager;
        this.settings = votingManager.getSettings();
        this.forced = forced;
    }

    @Override
    public void tick() {
        if (Bukkit.getOnlinePlayers().size() <= (this.settings.getMinPlayers() / 2) && !this.forced) {
            this.cancel();
            CommandUtils.broadcastErrorMessage("Vote canceled! Not enough players to continue.");
            MatchState.setState(MatchState.WAITING);
        }
    }

    @Override
    public void complete() {
        VotingOption option = this.votingManager.getMostVotedOption();
        MatchManager.setNext(option);
        TaskUtils.runTimer(new CycleCountdown(this.settings.getCycleDuration(), option));
    }

    @Override
    public void cancel() {
        this.votingManager.clearVotingItems();
        super.cancel();
    }

}
