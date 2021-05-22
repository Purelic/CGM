package net.purelic.cgm.core.maps.flag.runnables;

import net.purelic.cgm.core.gamemodes.NumberSetting;
import net.purelic.cgm.core.gamemodes.ToggleSetting;
import net.purelic.cgm.core.maps.flag.Flag;
import net.purelic.cgm.core.maps.flag.constants.FlagState;
import net.purelic.cgm.utils.FlagUtils;
import org.bukkit.scheduler.BukkitRunnable;

public class FlagResetCountdown extends BukkitRunnable {

    private Flag flag;
    private int seconds;

    public FlagResetCountdown(Flag flag) {
        this.flag = flag;
        this.seconds = NumberSetting.FLAG_RESET_DELAY.value();
    }

    @Override
    public void run() {
        if (!this.flag.isState(FlagState.DROPPED)) {
            this.cancel();
        } else if (this.seconds == 0) {
            this.updateRespawnLocation();
            this.flag.setState(FlagState.RESPAWNING);
            this.cancel();
        } else {
            this.flag.updateScoreboard();
            this.seconds--;
        }
    }

    public int getSeconds() {
        return this.seconds;
    }

    private void updateRespawnLocation() {
        if (ToggleSetting.MOVING_FLAG.isEnabled() && ToggleSetting.NEUTRAL_FLAGS.isEnabled()) {
            Flag random = FlagUtils.getRandomFlag();

            if (random != this.flag) {
                // Reset current flag
                this.flag.setActive(false);

                // Activate random flag
                this.flag = random;
                this.flag.setActive(true);
            }
        }
    }

}
