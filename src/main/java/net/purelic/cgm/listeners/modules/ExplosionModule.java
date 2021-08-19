package net.purelic.cgm.listeners.modules;

import net.purelic.cgm.core.constants.MatchTeam;
import net.purelic.cgm.core.damage.PlayerDamageTick;
import net.purelic.cgm.core.gamemodes.ToggleSetting;
import net.purelic.cgm.core.managers.DamageManger;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByBlockEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.projectiles.ProjectileSource;
import org.bukkit.util.Vector;

public class ExplosionModule implements Listener {

    private Entity explosion;

    @EventHandler
    public void onEntityExplode(EntityExplodeEvent event) {
        Entity entity = event.getEntity();

        if (entity instanceof TNTPrimed) {
            event.setYield(4F);
        } else if (entity instanceof Fireball) {
            event.setYield(3F);
        }

        this.explosion = entity;
        event.getLocation().getWorld().createExplosion(event.getLocation(), event.getYield(), false);
        this.explosion = null;
    }

    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (ToggleSetting.INSTANT_TNT.isEnabled() && event.getCause() == EntityDamageEvent.DamageCause.ENTITY_EXPLOSION) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEntityDamageByBlock(EntityDamageByBlockEvent event) {
        Entity entity = event.getEntity();

        if (!(entity instanceof Player)) return;

        Player player = (Player) entity;
        Entity damager = null;
        Player damagerPlayer = null;

        if (this.explosion == null) return;

        if (this.explosion instanceof Projectile) {
            ProjectileSource source = ((Projectile) this.explosion).getShooter();
            damager = source == null ? null : (Entity) source;
        } else if (this.explosion instanceof TNTPrimed) {
            TNTPrimed tnt = (TNTPrimed) this.explosion;
            damager = tnt.getSource();
        }

        if (damager instanceof Player) {
            damagerPlayer = (Player) damager;
        }

        if (damagerPlayer != null && damagerPlayer != player && MatchTeam.isSameTeam(player, damagerPlayer)) {
            event.setCancelled(true);
            return;
        }

        if (ToggleSetting.INSTANT_TNT.isEnabled()) {
            if (this.explosion instanceof Fireball) {
                event.setDamage(event.getDamage() * 0.1);
                Vector impulse = player.getVelocity();
                float power = 1.5F;
                impulse.setY(0.75 + Math.abs(impulse.getY()) * 0.5);
                impulse.multiply(power / 3f);
                player.setVelocity(impulse);
            } else if (this.explosion instanceof TNTPrimed) {
                event.setDamage(event.getDamage() * 0.2);
                Vector impulse = player.getLocation().subtract(this.explosion.getLocation()).toVector().normalize();
                float power = 1.25F;
                impulse.setY(0.75 + Math.abs(impulse.getY()));
                impulse.setX(impulse.getX() * 2);
                impulse.setZ(impulse.getZ() * 2);
                impulse.multiply(power / 3f);
                player.setVelocity(impulse);
            }
        }

        if (damagerPlayer == null || (player == damagerPlayer)) return;

        String damageName;

        if (this.explosion instanceof Fireball) damageName = "Fireball";
        else if (this.explosion instanceof TNTPrimed) damageName = "TNT";
        else damageName = "Explosion";

        double distance = damager.getLocation().distance(player.getLocation());
        PlayerDamageTick tick = new PlayerDamageTick(event.getDamage(), damageName, System.currentTimeMillis(), (Player) damager, distance);
        DamageManger.logTick(player.getUniqueId(), tick);
    }

}
