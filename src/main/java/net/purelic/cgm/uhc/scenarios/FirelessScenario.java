package net.purelic.cgm.uhc.scenarios;

import net.purelic.commons.modules.Module;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageEvent;

public class FirelessScenario implements Module {

    @EventHandler
    public void onPlayerDamage(EntityDamageEvent event) {
        if (event.getEntity() instanceof Player) {
            EntityDamageEvent.DamageCause cause = event.getCause();

            if (cause.equals(EntityDamageEvent.DamageCause.FIRE)
                || cause.equals(EntityDamageEvent.DamageCause.FIRE_TICK)
                || cause.equals(EntityDamageEvent.DamageCause.LAVA)) {
                event.setCancelled(true);
            }
        }
    }

}
