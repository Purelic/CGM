package net.purelic.cgm.listeners.modules.stats;

import net.purelic.cgm.core.managers.MatchManager;
import net.purelic.cgm.core.match.Participant;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.projectiles.ProjectileSource;

public class ArrowStatsModule implements Listener {

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onProjectileLaunch(ProjectileLaunchEvent event) {
        Entity entity = event.getEntity();

        if (!(entity instanceof Arrow)) return;

        ProjectileSource shooter = ((Arrow) entity).getShooter();

        if (!(shooter instanceof Player)) return;

        Participant participant = MatchManager.getParticipant((Player) shooter);
        participant.getStats().addArrowShot();
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        Entity entity = event.getDamager();

        if (!(entity instanceof Arrow)) return;

        Arrow arrow = (Arrow) entity;
        ProjectileSource projSource = arrow.getShooter();

        if (!(projSource instanceof Player)) return;

        Player shooter = (Player) projSource;
        Entity damaged = event.getEntity();

        if (!(damaged instanceof Player)) return;

        Participant participant = MatchManager.getParticipant(shooter);
        participant.getStats().addArrowHit(damaged.getLocation().distance(shooter.getLocation()));
    }

}
