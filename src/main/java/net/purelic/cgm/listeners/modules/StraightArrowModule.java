package net.purelic.cgm.listeners.modules;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.projectiles.ProjectileSource;

public class StraightArrowModule implements Listener {

    @EventHandler
    public void onProjectileLaunch(ProjectileLaunchEvent event) {
        ProjectileSource shooter = event.getEntity().getShooter();

        if (!(shooter instanceof Player)) return;

        Player player = (Player) shooter;
        Entity entity = event.getEntity();

        entity.setVelocity(player.getLocation().getDirection().normalize().multiply(entity.getVelocity().length()));
    }

}
