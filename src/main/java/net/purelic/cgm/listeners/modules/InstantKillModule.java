package net.purelic.cgm.listeners.modules;

import net.purelic.cgm.core.gamemodes.ToggleSetting;
import net.purelic.commons.utils.ItemCrafter;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

public class InstantKillModule implements Listener {

    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (event.isCancelled() || !(event.getEntity() instanceof Player)) return;

        if (event.getDamager() instanceof Player) {
            Player damager = (Player) event.getDamager();
            if (damager.getItemInHand() != null
                && new ItemCrafter(damager.getItemInHand()).hasTag("one_hit_kill")) {
                event.setDamage(200);
            }
        } else if (event.getDamager() instanceof Projectile
            && ToggleSetting.PLAYER_BOW_INSTANT_KILL.isEnabled()) {
            event.setDamage(200);
        }
    }

}
