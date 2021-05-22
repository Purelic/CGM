package net.purelic.cgm.listeners.modules;

import net.purelic.cgm.core.damage.DamageTick;
import net.purelic.cgm.core.damage.FallDamageTick;
import net.purelic.cgm.core.damage.OtherDamageTick;
import net.purelic.cgm.core.damage.PlayerDamageTick;
import net.purelic.cgm.core.gamemodes.ToggleSetting;
import net.purelic.cgm.core.managers.DamageManger;
import net.purelic.cgm.events.match.MatchEndEvent;
import net.purelic.cgm.events.match.RoundEndEvent;
import net.purelic.cgm.events.match.RoundStartEvent;
import org.apache.commons.lang.WordUtils;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;

public class DamageTrackerModule implements Listener {

    @EventHandler(priority = EventPriority.MONITOR)
    public void onDamage(EntityDamageEvent event) {
        Entity entity = event.getEntity();
        EntityDamageEvent.DamageCause cause = event.getCause();

        if (!(entity instanceof Player)) return;

        DamageTick tick = null;
        double dmg = event.getDamage(EntityDamageEvent.DamageModifier.BASE);

        if (event.isCancelled() || dmg == 0 || cause == EntityDamageEvent.DamageCause.VOID) return;

        Player damaged = (Player) entity;

        if (event instanceof EntityDamageByEntityEvent) {
            Entity damager = ((EntityDamageByEntityEvent) event).getDamager();

            Player attacker = null;
            double distance = 0;

            if (damager instanceof Player) {
                if (ToggleSetting.PLAYER_SWORD_INSTANT_KILL.isEnabled()) return;
                else attacker = (Player) damager;
            } else if (damager instanceof Projectile) {
                if (ToggleSetting.PLAYER_BOW_INSTANT_KILL.isEnabled()) return;

                Projectile projectile = (Projectile) damager;
                if (projectile.getShooter() instanceof Player) {
                    attacker = (Player) projectile.getShooter();
                    distance = attacker.getLocation().distance(damaged.getLocation());
                }
            }

            if (attacker != null && attacker != damaged) {
                if (distance > 0) {
                    tick = new PlayerDamageTick(dmg, "PVP", System.currentTimeMillis(), attacker, distance);
                } else {
                    tick = new PlayerDamageTick(dmg, "PVP", System.currentTimeMillis(), attacker);
                }
            }
        } else {
            if (event.getCause() == EntityDamageEvent.DamageCause.FALL) {
                tick = new FallDamageTick(dmg, "Fall", System.currentTimeMillis(), entity.getFallDistance());
            } else {
                String name = event.getCause().name();
                name = WordUtils.capitalizeFully(name).replace("_", " ");
                tick = new OtherDamageTick(dmg, event.getCause(), name, System.currentTimeMillis());
            }
        }

        if (tick != null) {
            DamageManger.logTick(entity.getUniqueId(), tick);
        }
    }

    @EventHandler (priority = EventPriority.MONITOR)
    public void onRoundStart(RoundStartEvent event) {
        DamageManger.dumpAll();
    }

    @EventHandler (priority = EventPriority.MONITOR)
    public void onRoundEnd(RoundEndEvent event) {
        DamageManger.dumpAll();
    }

    @EventHandler (priority = EventPriority.MONITOR)
    public void onMatchEnd(MatchEndEvent event) {
        DamageManger.dumpAll();
    }

}
