package net.purelic.cgm.core.stats.constants;

import org.bukkit.entity.Fireball;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;

public enum KillType {

    MELEE,
    BOW,
    VOID,
    FALL,
    EXPLOSION,
    OTHER,
    ;

    public static KillType getKillType(Player killed) {
        if (killed == null || killed.getLastDamageCause() == null) {
            return OTHER;
        }

        EntityDamageEvent.DamageCause cause = killed.getLastDamageCause().getCause();

        if (cause == EntityDamageEvent.DamageCause.ENTITY_ATTACK) {
            return MELEE;
        } else if (cause == EntityDamageEvent.DamageCause.PROJECTILE) {
            Projectile proj = (Projectile) ((EntityDamageByEntityEvent) killed.getLastDamageCause()).getDamager();

            if (proj instanceof Fireball) return EXPLOSION;
            else return BOW;
        } else if (cause == EntityDamageEvent.DamageCause.BLOCK_EXPLOSION || cause == EntityDamageEvent.DamageCause.ENTITY_EXPLOSION) {
            return EXPLOSION;
        } else if (cause == EntityDamageEvent.DamageCause.VOID) {
            return VOID;
        } else if (cause == EntityDamageEvent.DamageCause.FALL) {
            return FALL;
        } else {
            return OTHER;
        }
    }

}

