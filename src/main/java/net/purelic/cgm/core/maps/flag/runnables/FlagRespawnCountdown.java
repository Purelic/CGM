package net.purelic.cgm.core.maps.flag.runnables;

import net.purelic.cgm.core.maps.flag.Flag;
import net.purelic.cgm.core.maps.flag.constants.FlagState;
import org.bukkit.scheduler.BukkitRunnable;

public class FlagRespawnCountdown extends BukkitRunnable {

    private final Flag flag;
    private int seconds;

    public FlagRespawnCountdown(Flag flag, int delay) {
        this.flag = flag;
        this.seconds = delay;
    }

    @Override
    public void run() {
        if (this.seconds == 0) {
            this.flag.setState(FlagState.RETURNED);
            this.cancel();
        } else {
            this.flag.updateScoreboard();
            this.seconds--;
        }
    }

    public int getSeconds() {
        return this.seconds;
    }

}
