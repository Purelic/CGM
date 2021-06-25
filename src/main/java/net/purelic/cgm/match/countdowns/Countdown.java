package net.purelic.cgm.match.countdowns;

import net.md_5.bungee.api.ChatColor;
import net.purelic.cgm.utils.PlayerUtils;
import net.purelic.cgm.utils.SoundUtils;
import net.purelic.commons.utils.ChatUtils;
import org.bukkit.scheduler.BukkitRunnable;

public abstract class Countdown extends BukkitRunnable {

    private final Countdown countdown;
    private boolean canceled;
    private int seconds;
    private String actionPrefix;

    public Countdown(int seconds, String actionPrefix) {
        this.countdown = this;
        this.canceled = false;
        this.seconds = seconds;
        this.actionPrefix = actionPrefix;
    }

    public void tick() {

    }

    public void complete() {

    }

    @Override
    public void run() {
        this.tick();

        if (this.canceled) {
            return;
        }

        if (this.seconds <= 0) {
            this.cancel();
            this.complete();
            return;
        }

        SoundUtils.playCountdownNote(this.seconds);
        PlayerUtils.setLevelAll(this.seconds);
        ChatUtils.broadcastActionBar(
            this.actionPrefix + " in " + ChatColor.AQUA + this.seconds + ChatColor.RESET + " second" + ((this.seconds == 1) ? "!" : "s!"),
            this.seconds % 5 == 0
        );

        this.seconds--;
    }

    public void cancel() {
        this.canceled = true;
        PlayerUtils.setLevelAll(0);
        ChatUtils.clearActionBarAll();
        super.cancel();
    }

    public int getSeconds() {
        return this.seconds;
    }

    public void setSeconds(int seconds) {
        this.seconds = seconds;
    }

    public void setActionPrefix(String actionPrefix) {
        this.actionPrefix = actionPrefix;
    }

    public Countdown getCountdown() {
        return this.countdown;
    }

}
