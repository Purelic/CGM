package net.purelic.cgm.core.maps.flag.runnables;

import net.purelic.cgm.core.maps.flag.Flag;
import net.purelic.cgm.core.maps.flag.constants.FlagState;
import net.purelic.cgm.utils.FlagUtils;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class FlagTracker extends BukkitRunnable {

    private final Flag flag;
    private final Player player;

    public FlagTracker(Flag flag) {
        this.flag = flag;
        this.player = flag.getCarrier().getPlayer();
    }

    @Override
    public void run() {
        if (!this.flag.isState(FlagState.TAKEN)) {
            this.cancel();
            return;
        }

        Location lastLocation = FlagUtils.getDropLocation(this.flag, this.player.getLocation(), false);
        if (lastLocation != null) this.flag.setLastLocation(lastLocation);
    }

}
