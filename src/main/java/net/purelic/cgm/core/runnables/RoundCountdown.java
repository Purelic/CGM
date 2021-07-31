package net.purelic.cgm.core.runnables;

import net.md_5.bungee.api.ChatColor;
import net.purelic.cgm.core.constants.MatchState;
import net.purelic.cgm.events.match.RoundStartEvent;
import net.purelic.cgm.utils.PlayerUtils;
import net.purelic.cgm.utils.SoundUtils;
import net.purelic.commons.Commons;
import net.purelic.commons.utils.ChatUtils;
import net.purelic.commons.utils.VersionUtils;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitRunnable;

public class RoundCountdown extends BukkitRunnable {

    private static BukkitRunnable countdown;
    private int seconds;

    public RoundCountdown() {
        countdown = this;
        this.seconds = 10;
    }

    @Override
    public void run() {
        if (!MatchState.isState(MatchState.STARTED)) {
            this.cancel();
            return;
        }

        if (this.seconds <= 0) {
            PlayerUtils.setLevelAll(0);
            this.cancel();
            Commons.callEvent(new RoundStartEvent(MatchCountdown.isForced()));
            return;
        }

        String message = "Next round starts in " + ChatColor.AQUA + this.seconds +
            ChatColor.RESET + " second" + ((this.seconds == 1) ? "!" : "s!");
        ChatUtils.broadcastActionBar(message);
        SoundUtils.playCountdownNote(this.seconds);

        PlayerUtils.setLevelAll(seconds);

        if (seconds % 5 == 0) {
            Bukkit.getOnlinePlayers().stream().filter(VersionUtils::isLegacy).forEach(player -> player.sendMessage(message));
        }

        this.seconds--;
    }

    public static BukkitRunnable getCountdown() {
        return countdown;
    }

}
