package net.purelic.cgm.core.managers;

import net.md_5.bungee.api.ChatColor;
import net.purelic.cgm.core.damage.DamageTick;
import net.purelic.cgm.core.damage.KillAssist;
import net.purelic.cgm.core.damage.PlayerDamageTick;
import net.purelic.cgm.core.match.Participant;
import org.apache.commons.lang.WordUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.text.DecimalFormat;
import java.util.*;

/******************************************************************************
 * Copyright (c) 2016.  Written by Devon "Turqmelon": http://turqmelon.com    *
 * For more information, see LICENSE.TXT.                                     *
 ******************************************************************************/

public class DamageManger {

    public static final long DAMAGE_TIMEOUT = 7000;
    public static final int ASSIST_PERCENTAGE_THRESHOLD = 0;
    public static final ChatColor BASE_COLOR = ChatColor.GRAY;
    public static final ChatColor ACCENT_COLOR = ChatColor.DARK_RED;
    public static final ChatColor PUNCTUATION_COLOR = ChatColor.DARK_GRAY;
    private static final Map<UUID, List<DamageTick>> DAMAGE_TICKS = new HashMap<>();

    public static void dump(UUID uuid) {
        getDamageTicks().remove(uuid);
    }

    public static void dumpAll() {
        getDamageTicks().clear();
    }

    public static List<KillAssist> getPossibleAssists(List<DamageTick> ticks) {
        if (ticks.size() == 0) return new ArrayList<>();

        List<KillAssist> assists = new ArrayList<>();
        List<PlayerDamageTick> playerDamage = new ArrayList<>();

        PlayerDamageTick killingTick = null;
        DamageTick lastTick = ticks.get(ticks.size() - 1);

        if (lastTick instanceof PlayerDamageTick) {
            killingTick = (PlayerDamageTick) lastTick;
        }

        for (DamageTick tick : ticks) {
            if (tick instanceof PlayerDamageTick) {
                PlayerDamageTick pt = (PlayerDamageTick) tick;
                playerDamage.add(pt);
                killingTick = pt;
            }
        }

        double totalDmg = 0;

        for (PlayerDamageTick tick : playerDamage) {
            totalDmg += tick.getDamage();
        }

        for (PlayerDamageTick tick : playerDamage) {
            double dmg = tick.getDamage();
            int percent = (int) ((dmg / totalDmg) * 100);
            if (percent >= DamageManger.ASSIST_PERCENTAGE_THRESHOLD) {
                boolean killer = tick.getPlayer() == killingTick.getPlayer();
                KillAssist killAssist = new KillAssist(tick.getPlayer(), dmg, percent, killer);
                assists.add(killAssist);
            }
        }

        Collections.sort(assists);
        return assists;
    }

    public static List<String> getDamageSummary(List<DamageTick> ticks) {
        List<String> messages = new ArrayList<>();
        DecimalFormat df = new DecimalFormat("#.#");

        for (DamageTick tick : ticks) {
            messages.add(DamageManger.PUNCTUATION_COLOR + " - " + DamageManger.ACCENT_COLOR + ChatColor.BOLD + df.format(tick.getDamage()) + " DMG" +
                DamageManger.PUNCTUATION_COLOR + ": " + tick.getSingleLineSummary() + DamageManger.PUNCTUATION_COLOR + " (" + tick.timeDiff() + ")");
        }

        return messages;
    }

    public static void logTick(UUID uuid, DamageTick tick) {
        DamageTick logged = getLoggedTick(uuid, tick);

        if (logged != null) {
            logged.setDamage(logged.getDamage() + tick.getDamage());
            logged.setTimestamp(System.currentTimeMillis());
        } else {
            List<DamageTick> ticks = getLoggedTicks(uuid);
            ticks.add(tick);
            getDamageTicks().put(uuid, ticks);
        }

        if (tick instanceof PlayerDamageTick) {
            double damage = tick.getDamage();

            Participant attacked = MatchManager.getParticipant(Bukkit.getPlayer(uuid));
            attacked.getStats().addDamageReceived(damage);

            Participant attacker = MatchManager.getParticipant(((PlayerDamageTick) tick).getPlayer());
            attacker.getStats().addDamageDealt(damage);
        }
    }

    public static DamageTick getLoggedTick(UUID uuid, DamageTick newTick) {
        for (DamageTick tick : getLoggedTicks(uuid)) {
            if (tick.getCause() == newTick.getCause() && tick.matches(newTick)) {
                return tick;
            }
        }
        return null;
    }

    public static List<DamageTick> getLoggedTicks(UUID uuid) {
        return getDamageTicks().containsKey(uuid) ?
            cleanup(getDamageTicks().get(uuid)) :
            new ArrayList<>();
    }

    public static DamageTick getLastTick(Player player) {
        List<DamageTick> ticks = getLoggedTicks(player.getUniqueId());
        return ticks.get(ticks.size() - 1);
    }

    private static List<DamageTick> cleanup(List<DamageTick> ticks) {
        for (int i = 0; i < ticks.size(); i++) {
            DamageTick tick = ticks.get(i);
            if (System.currentTimeMillis() - tick.getTimestamp() > DamageManger.DAMAGE_TIMEOUT) {
                ticks.remove(tick);
            }
        }
        Collections.sort(ticks);
        return ticks;
    }

    private static Map<UUID, List<DamageTick>> getDamageTicks() {
        return DAMAGE_TICKS;
    }

    public static String getEntityName(Entity entity) {
        if (entity.getCustomName() != null) {
            return entity.getCustomName();
        } else {
            String name = entity.getType().name();
            name = name.replace("_", " ");
            return WordUtils.capitalizeFully(name);
        }
    }

    public static boolean samePlace(Location loc1, Location loc2) {
        return loc1.getWorld().getName().equals(loc2.getWorld().getName()) &&
            loc1.getBlockX() == loc2.getBlockX() &&
            loc1.getBlockY() == loc2.getBlockY() &&
            loc1.getBlockZ() == loc2.getBlockZ();
    }

}