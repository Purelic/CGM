package net.purelic.cgm.utils;

import net.md_5.bungee.api.ChatColor;
import net.purelic.commons.utils.NickUtils;
import org.apache.commons.lang3.text.WordUtils;
import org.bukkit.entity.*;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;

public class DeathMessageUtils {

    public static String getDeathMessage(Player player, Player killer, ChatColor chatColor) {
        EntityDamageEvent.DamageCause cause = player.getLastDamageCause().getCause();

        switch (cause) {
            case BLOCK_EXPLOSION:
            case ENTITY_EXPLOSION:
                return NickUtils.getDisplayName(player) + chatColor + " was blown up" + ((killer != null) ? (" by " + NickUtils.getDisplayName(killer)) : "");
            case CONTACT:
                return NickUtils.getDisplayName(player) + chatColor + " was pricked to death" + ((killer != null) ? (" whilst fighting " + NickUtils.getDisplayName(killer)) : "");
            case DROWNING:
                return NickUtils.getDisplayName(player) + chatColor + " forgot how to swim" + ((killer != null) ? (" whilst fighting " + NickUtils.getDisplayName(killer)) : "");
            case ENTITY_ATTACK:
                Entity entity = ((EntityDamageByEntityEvent) player.getLastDamageCause()).getDamager();
                if (entity instanceof Wolf && ((Wolf) entity).getOwner() != null) return NickUtils.getDisplayName(player) + chatColor + " was slain by " + NickUtils.getDisplayName((Player) ((Wolf) entity).getOwner()) + chatColor + "'s wolf";
                else if (!(entity instanceof Player) && killer != null) return NickUtils.getDisplayName(player) + chatColor + " was slain by " + getMonster(entity) + chatColor + " whilst fighting " + NickUtils.getDisplayName(killer);
                else if (entity instanceof Player) return NickUtils.getDisplayName(player) + chatColor + " was killed" + (killer != null ? " by " + NickUtils.getDisplayName(killer) : "");
                else return NickUtils.getDisplayName(player) + chatColor + " was slain by " + getMonster(entity);
            case FALL:
                if (killer != null) return NickUtils.getDisplayName(player) + chatColor + " knocked off a high place (" + (int) (player.getFallDistance() + 0.5D) + " blocks) by " + NickUtils.getDisplayName(killer);
                else return NickUtils.getDisplayName(player) + chatColor + " fell" + " (" + (int) (player.getFallDistance() + 0.5D) + " blocks) " + "off a high place";
            case FALLING_BLOCK:
                return NickUtils.getDisplayName(player) + chatColor + " was crushed to death" + ((killer != null) ? (" whilst fighting " + NickUtils.getDisplayName(killer)) : "");
            case FIRE:
                return NickUtils.getDisplayName(player) + chatColor + " ran into a fire" + ((killer != null) ? (" whilst fighting " + NickUtils.getDisplayName(killer)) : "");
            case FIRE_TICK:
                return NickUtils.getDisplayName(player) + chatColor + " burned to death" + ((killer != null) ? (" whilst fighting " + NickUtils.getDisplayName(killer)) : "");
            case LAVA:
                return NickUtils.getDisplayName(player) + chatColor + " fell in lava" + ((killer != null) ? (" whilst fighting " + NickUtils.getDisplayName(killer)) : "");
            case LIGHTNING:
                return NickUtils.getDisplayName(player) + chatColor + " was struck by lightning" + ((killer != null) ? (" whilst fighting " + NickUtils.getDisplayName(killer)) : "");
            case MAGIC:
                return NickUtils.getDisplayName(player) + chatColor + " was killed by magic" + ((killer != null) ? (" whilst fighting " + NickUtils.getDisplayName(killer)) : "");
            case PROJECTILE:
                Projectile proj = (Projectile) ((EntityDamageByEntityEvent) player.getLastDamageCause()).getDamager();
                if (proj.getShooter() instanceof Player) {
                    killer = (Player) proj.getShooter();
                    int dist = (int) (player.getLocation().distance(killer.getLocation()) + 0.5D);
                    if (proj instanceof Fireball) return NickUtils.getDisplayName(player) + chatColor + " was fireballed (" + dist + " block" + (dist == 1 ? "" : "s") + ") by " + NickUtils.getDisplayName(killer);
                    else return NickUtils.getDisplayName(player) + chatColor + " was shot (" + dist + " block" + (dist == 1 ? "" : "s") + ") by " + NickUtils.getDisplayName(killer);
                }
                int dist = (int) (player.getLocation().distance(killer.getLocation()) + 0.5D);
                Entity mob = (Entity) proj.getShooter();
                return NickUtils.getDisplayName(player) + chatColor + " was shot (" + dist + " block" + (dist == 1 ? "" : "s") + ") by " + getMonster(mob);
            case STARVATION:
                return NickUtils.getDisplayName(player) + chatColor + " starved to death" + ((killer != null) ? (" whilst fighting " + NickUtils.getDisplayName(killer)) : "");
            case SUFFOCATION:
                return NickUtils.getDisplayName(player) + chatColor + " got stuck in the geometry" + ((killer != null) ? (" whilst fighting " + NickUtils.getDisplayName(killer)) : "");
            case THORNS:
                if (killer != null) return NickUtils.getDisplayName(player) + chatColor + " was killed by " + NickUtils.getDisplayName(killer) + chatColor + "'s thorns";
                else return NickUtils.getDisplayName(player) + chatColor + " was thorn'd to death";
            case VOID:
                if (killer != null) return NickUtils.getDisplayName(player) + chatColor + " was voided by " + NickUtils.getDisplayName(killer);
                else return NickUtils.getDisplayName(player) + chatColor + " fell out of the world";
            case WITHER:
                return NickUtils.getDisplayName(player) + chatColor + " withered away" + ((killer != null) ? (" whilst fighting " + NickUtils.getDisplayName(killer)) : "");
        }
        return NickUtils.getDisplayName(player) + chatColor + " died to unknown causes" + ((killer != null) ? (" whilst fighting " + NickUtils.getDisplayName(killer)) : "");
    }

    public static String getShortDeathMessage(Player player, Player killer) {
        return getShortDeathMessage(player, killer, ChatColor.WHITE);
    }

    public static String getShortDeathMessage(Player player, Player killer, ChatColor chatColor) {
        EntityDamageEvent.DamageCause dc = player.getLastDamageCause().getCause();

        switch (dc) {
            case BLOCK_EXPLOSION:
            case ENTITY_EXPLOSION:
                return chatColor + "Blew up" + ((killer != null) ? (" by " + NickUtils.getDisplayName(killer)) : "");
            case CONTACT:
                return chatColor + "Pricked to death" + ((killer != null) ? (" whilst fighting " + NickUtils.getDisplayName(killer)) : "");
            case DROWNING:
                return chatColor + "Drowned" + ((killer != null) ? (" whilst fighting " + NickUtils.getDisplayName(killer)) : "");
            case ENTITY_ATTACK:
                Entity entity = ((EntityDamageByEntityEvent) player.getLastDamageCause()).getDamager();
                if (entity instanceof Wolf && ((Wolf) entity).getOwner() != null) return chatColor + "Slain by " + NickUtils.getDisplayName((Player) ((Wolf) entity).getOwner()) + chatColor + "'s wolf";
                else if (!(entity instanceof Player) && killer != null) return chatColor + "Slain by " + getMonster(entity) + chatColor + " whilst fighting " + NickUtils.getDisplayName(killer);
                else if (entity instanceof Player) return chatColor + "Killed by " + NickUtils.getDisplayName(killer);
                else return chatColor + "Killed by " + getMonster(entity);
            case FALL:
                if (killer != null) return chatColor + "Knocked off a high place by " + NickUtils.getDisplayName(killer);
                else return chatColor + "Fell off a high place";
            case FALLING_BLOCK:
                return chatColor + "Crushed to death" + ((killer != null) ? (" whilst fighting " + NickUtils.getDisplayName(killer)) : "");
            case FIRE:
                return chatColor + "Ran into a fire" + ((killer != null) ? (" whilst fighting " + NickUtils.getDisplayName(killer)) : "");
            case FIRE_TICK:
                return chatColor + "Burned to death" + ((killer != null) ? (" whilst fighting " + NickUtils.getDisplayName(killer)) : "");
            case LAVA:
                return chatColor + "Fell in lava" + ((killer != null) ? (" whilst fighting " + NickUtils.getDisplayName(killer)) : "");
            case LIGHTNING:
                return chatColor + "Struck by lightning" + ((killer != null) ? (" whilst fighting " + NickUtils.getDisplayName(killer)) : "");
            case MAGIC:
                return chatColor + "Killed with magic" + ((killer != null) ? (" whilst fighting " + NickUtils.getDisplayName(killer)) : "");
            case PROJECTILE:
                Projectile proj = (Projectile) ((EntityDamageByEntityEvent) player.getLastDamageCause()).getDamager();
                if (proj.getShooter() instanceof Player) {
                    killer = (Player) proj.getShooter();
                    if (proj instanceof Fireball) return chatColor + "Fireballed by " + NickUtils.getDisplayName(killer);
                    else return chatColor + "Shot by " + NickUtils.getDisplayName(killer);
                }
                Entity mob = (Entity) proj.getShooter();
                return chatColor + "Shot by " + getMonster(mob);
            case STARVATION:
                return chatColor + "Starved to death" + ((killer != null) ? (" whilst fighting " + NickUtils.getDisplayName(killer)) : "");
            case SUFFOCATION:
                return chatColor + "Suffocated" + ((killer != null) ? (" whilst fighting " + NickUtils.getDisplayName(killer)) : "");
            case THORNS:
                if (killer != null) return chatColor + "Killed by " + NickUtils.getDisplayName(killer) + chatColor + "'s thorns.";
                else return chatColor + "Thorn'd to death";
            case VOID:
                if (killer != null) return chatColor + "Voided by " + NickUtils.getDisplayName(killer);
                else return chatColor + "Fell out of the world";
            case WITHER:
                return chatColor + "Withered away" + ((killer != null) ? (" whilst fighting " + NickUtils.getDisplayName(killer)) : "");
        }

        return chatColor + "Unknown causes" + ((killer != null) ? (" whilst fighting " + NickUtils.getDisplayName(killer)) : "");
    }

    private static String getMonster(Entity mob) {
        String name = WordUtils.capitalizeFully(mob.getType().name().replaceAll("_", ""));
        char letter = name.charAt(0);

        if (isVowel(letter)) return "an " + name;
        else return "a " + name;
    }

    private static boolean isVowel(char character) {
        return character == 'A' || character == 'E' || character == 'I' || character == 'O' || character == 'U' || character == 'a' || character == 'e' || character == 'i' || character == 'o' || character == 'u';
    }

}
