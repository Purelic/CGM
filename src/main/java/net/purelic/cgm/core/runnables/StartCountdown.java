package net.purelic.cgm.core.runnables;

import net.md_5.bungee.api.ChatColor;
import net.purelic.cgm.core.constants.MatchState;
import net.purelic.cgm.utils.PlayerUtils;
import net.purelic.cgm.utils.SoundUtils;
import net.purelic.commons.utils.ChatUtils;
import net.purelic.commons.utils.VersionUtils;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitRunnable;

public class StartCountdown extends BukkitRunnable {

    private static BukkitRunnable countdown;
    private static int seconds;
    private static boolean forced;

    public StartCountdown(int seconds, boolean forced) {
        countdown = this;
        StartCountdown.seconds = seconds;
        StartCountdown.forced = forced;
    }

    @Override
    public void run() {
//        if ((MatchTeam.totalPlaying() < 2 || !MatchTeam.hasMinPlayers()) && !forced) {
//            CommandUtils.broadcastErrorMessage("Not enough players! Canceling the countdown.");
//            MatchState.setState(MatchState.PRE_GAME);
//            this.cancel();
//            return;
//        }

        if (seconds <= 0) {
            MatchState.setState(MatchState.STARTED, forced);
            PlayerUtils.setLevelAll(0);
            this.cancel();
            return;
        }

        String message = "Match starts in " + ChatColor.AQUA + seconds +
                ChatColor.RESET + " second" + ((seconds == 1) ? "!" : "s!");

        ChatUtils.broadcastActionBar(message);
        SoundUtils.playCountdownNote(seconds);
        PlayerUtils.setLevelAll(seconds);

        if (seconds % 10 == 0 || seconds <= 5) {
            Bukkit.getOnlinePlayers().stream().filter(VersionUtils::isLegacy).forEach(player -> player.sendMessage(message));
        }

        seconds--;
    }

    public static BukkitRunnable getCountdown() {
        return countdown;
    }

    public static void setForced() {
        forced = true;
    }

    public static void setCountdown(int seconds) {
        StartCountdown.seconds = seconds;
    }

    public static int getSeconds() {
        return seconds;
    }

}
