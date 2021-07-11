package net.purelic.cgm.listeners;

import net.purelic.cgm.utils.PlayerUtils;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityTargetEvent;

public class EntityTarget implements Listener {

    // prevents entities like mobs and xp orbs from tracking observing players
    @EventHandler
    public void onEntityTarget(EntityTargetEvent event) {
        Entity target = event.getTarget();

        if (target instanceof Player && PlayerUtils.isObserving((Player) target)) {
            event.setCancelled(true);
        }
    }

}
