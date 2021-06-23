package net.purelic.cgm.voting;

import net.md_5.bungee.api.ChatColor;
import net.purelic.cgm.CGM;
import net.purelic.cgm.core.constants.MatchState;
import net.purelic.cgm.core.managers.MatchManager;
import net.purelic.cgm.core.runnables.CycleCountdown;
import net.purelic.cgm.utils.PlayerUtils;
import net.purelic.cgm.utils.SoundUtils;
import net.purelic.commons.utils.ChatUtils;
import net.purelic.commons.utils.CommandUtils;
import net.purelic.commons.utils.TaskUtils;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitRunnable;

class VotingCountdown extends BukkitRunnable {

    private final VotingManager votingManager;
    private final VotingSettings settings;
    private int seconds;
    private final boolean forced;

    public VotingCountdown(VotingManager votingManager, int seconds, boolean forced) {
        this.votingManager = votingManager;
        this.settings = votingManager.getSettings();
        this.seconds = seconds;
        this.forced = forced;
    }

    @Override
    public void run() {
        if (Bukkit.getOnlinePlayers().size() <= (this.settings.getMinPlayers() / 2) && !this.forced) {
            this.cancel();
            CommandUtils.broadcastErrorMessage("Vote canceled! Not enough players to continue.");
            MatchState.setState(MatchState.WAITING);
            return;
        }

        if (this.seconds <= 0) {
            this.cancel();
            VotingOption option = this.votingManager.getMostVotedOption();
            MatchManager.setNext(option);
            TaskUtils.runTimer(new CycleCountdown(this.settings.getCycleDuration(), option));
            return;
        }

        SoundUtils.playCountdownNote(this.seconds);
        PlayerUtils.setLevelAll(this.seconds);
        ChatUtils.broadcastActionBar(
            "Voting ends in " + ChatColor.AQUA + this.seconds + ChatColor.RESET + " second" + ((seconds == 1) ? "!" : "s!"),
            this.seconds % 5 == 0
        );

        this.seconds--;
    }

    public void cancel() {
        this.votingManager.clearVotingItems();
        PlayerUtils.setLevelAll(0);
        ChatUtils.clearActionBarAll();
        super.cancel();
    }

    public void setSeconds(int seconds) {
        this.seconds = seconds;
    }

}
