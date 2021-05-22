package net.purelic.cgm.listeners.modules;

import net.purelic.cgm.core.gamemodes.ToggleSetting;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityRegainHealthEvent;

public class RegenerationModule implements Listener {

    @EventHandler
    public void onEntityRegainHealth(EntityRegainHealthEvent event) {
        if (!(event.getEntity() instanceof Player)
                || event.getRegainReason() != EntityRegainHealthEvent.RegainReason.SATIATED) {
            return;
        }

        if (!ToggleSetting.PLAYER_NATURAL_REGEN.isEnabled()) {
            event.setCancelled(true);
        }
    }

}
