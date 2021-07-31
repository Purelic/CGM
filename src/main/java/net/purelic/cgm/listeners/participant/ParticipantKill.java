package net.purelic.cgm.listeners.participant;

import net.md_5.bungee.api.ChatColor;
import net.purelic.cgm.core.constants.MatchState;
import net.purelic.cgm.core.constants.MatchTeam;
import net.purelic.cgm.core.damage.KillAssist;
import net.purelic.cgm.core.gamemodes.EnumSetting;
import net.purelic.cgm.core.gamemodes.NumberSetting;
import net.purelic.cgm.core.gamemodes.constants.GameType;
import net.purelic.cgm.core.managers.MatchManager;
import net.purelic.cgm.core.maps.hill.Hill;
import net.purelic.cgm.core.match.Participant;
import net.purelic.cgm.core.rewards.Medal;
import net.purelic.cgm.core.rewards.RewardBuilder;
import net.purelic.cgm.core.runnables.MatchCountdown;
import net.purelic.cgm.core.runnables.RoundCountdown;
import net.purelic.cgm.events.participant.ParticipantKillEvent;
import net.purelic.cgm.listeners.modules.HeadModule;
import net.purelic.cgm.utils.EntityUtils;
import net.purelic.cgm.utils.FlagUtils;
import net.purelic.commons.utils.ItemCrafter;
import net.purelic.commons.utils.NickUtils;
import net.purelic.commons.utils.TaskUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Fireball;
import org.bukkit.entity.Player;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ParticipantKill implements Listener {

    public static boolean firstBlood = true;

    @EventHandler
    public void onParticipantKill(ParticipantKillEvent event) {
        Participant participant = event.getParticipant();
        Player player = participant.getPlayer();
        int points = 0;

        if (event.isGameTypeWithScoring()) {
            points = event.isBetrayal() ? NumberSetting.DEATHMATCH_BETRAYAL_POINTS.value() : NumberSetting.DEATHMATCH_KILL_POINTS.value();
        }

        if (participant == event.getKilled()) {
            return;
        }

        participant.getStats().addKill(event.getKilled(), player.getItemInHand(), event.isElimination());
        participant.addKill();
        participant.addScore(points);

        this.awardMedals(participant, event.getKilled(), points, event.getAssist(), event.isBetrayal());

        if (!MatchState.isState(MatchState.STARTED)
            || TaskUtils.isRunning(RoundCountdown.getCountdown())
            || participant.isDead()) return;

        int gapples = NumberSetting.KILL_REWARD_GAPPLES.value();
        if (gapples > 0) {
            player.getInventory().addItem(new ItemCrafter(Material.GOLDEN_APPLE).amount(gapples).craft());
        }

        int arrows = NumberSetting.KILL_REWARD_ARROWS.value();
        if (arrows > 0) {
            player.getInventory().addItem(new ItemCrafter(Material.ARROW).amount(arrows).craft());
        }

        int emeralds = NumberSetting.KILL_REWARD_EMERALDS.value();
        if (emeralds > 0) {
            player.getInventory().addItem(new ItemCrafter(Material.EMERALD).amount(emeralds).craft());
        }

        int pearls = NumberSetting.KILL_REWARD_PEARLS.value();
        if (pearls > 0) {
            player.getInventory().addItem(new ItemCrafter(Material.ENDER_PEARL).amount(pearls).craft());
        }
    }

    private void awardMedals(Participant killer, Participant killed, int points, KillAssist assist, boolean betrayal) {
        new RewardBuilder(
            killer.getPlayer(), points == 0 ? 1 : points
            , points == 0 ? "Kill" : "Point"
            , this.getKillMedal(killed, betrayal) + (assist == null || assist.getPercentage() == 100 ? "" : ChatColor.GRAY + " (" + assist.getPercentage() + "%)"))
            .addMedal(this.getMultiKillMedal(killer))
            .addMedal(this.getKillStreakMedal(killer))
            .addMedals(this.getStyleMedals(killer, killed))
            .reward();
    }

    private String getKillMedal(Participant killed, boolean betrayal) {
        Player player = killed.getPlayer();

        if (betrayal) {
            return "Betrayed " + NickUtils.getDisplayName(player);
        } else if (player.getLastDamageCause().getCause() == EntityDamageEvent.DamageCause.PROJECTILE) {
            return "Bow Kill";
        } else if (player.getLastDamageCause().getCause() == EntityDamageEvent.DamageCause.ENTITY_ATTACK) {
            return "Melee Kill";
        } else {
            return "Killed " + NickUtils.getDisplayName(player);
        }
    }

    private Medal getMultiKillMedal(Participant participant) {
        int multiKill = participant.getMultiKill();

        if (multiKill < 2) return null;

        switch (participant.getMultiKill()) {
            case 2:
                return Medal.DOUBLE_KILL;
            case 3:
                return Medal.TRIPLE_KILL;
            case 4:
                return Medal.TETRAKILL;
            case 5:
                return Medal.PENTAKILL;
            case 6:
                return Medal.HEXAKILL;
            case 7:
                return Medal.HEPTAKILL;
            case 8:
                return Medal.OCTOKILL;
            case 9:
                return Medal.ENNEAKILL;
            case 10:
                return Medal.DECAKILL;
        }

        return Medal.GAME_BREAKER;
    }

    private Medal getKillStreakMedal(Participant participant) {
        Player player = participant.getPlayer();
        int killstreak = participant.getKillstreak();

        if (killstreak < 5 || killstreak % 5 != 0) return null;

        switch (killstreak) {
            case 5:
                Bukkit.broadcastMessage(NickUtils.getDisplayName(player) + ChatColor.WHITE + " is on a " + ChatColor.GREEN + "Killing Spree" + ChatColor.WHITE + "!" + ChatColor.GRAY + " (" + killstreak + " Kill Streak)");
                return Medal.KILLING_SPREE;
            case 10:
                Bukkit.broadcastMessage(NickUtils.getDisplayName(player) + ChatColor.WHITE + " is on a " + ChatColor.GREEN + "Rampage" + ChatColor.WHITE + "!" + ChatColor.GRAY + " (" + killstreak + " Kill Streak)");
                return Medal.RAMPAGE;
            case 15:
                Bukkit.broadcastMessage(NickUtils.getDisplayName(player) + ChatColor.WHITE + " is " + ChatColor.GREEN + "Dominating" + ChatColor.WHITE + "!" + ChatColor.GRAY + " (" + killstreak + " Kill Streak)");
                return Medal.DOMINATING;
            case 20:
                Bukkit.broadcastMessage(NickUtils.getDisplayName(player) + ChatColor.WHITE + " is " + ChatColor.GREEN + "Unstoppable" + ChatColor.WHITE + "!" + ChatColor.GRAY + " (" + killstreak + " Kill Streak)");
                return Medal.UNSTOPPABLE;
            case 25:
                Bukkit.broadcastMessage(NickUtils.getDisplayName(player) + ChatColor.WHITE + " is " + ChatColor.GREEN + "Godlike" + ChatColor.WHITE + "!" + ChatColor.GRAY + " (" + killstreak + " Kill Streak)");
                return Medal.GODLIKE;
        }

        Bukkit.broadcastMessage(NickUtils.getDisplayName(player) + ChatColor.WHITE + " is on a " + ChatColor.GREEN + "Massacre" + ChatColor.WHITE + "!" + ChatColor.GRAY + " (" + killstreak + " Kill Streak)");
        return Medal.MASSACRE;
    }

    private List<Medal> getStyleMedals(Participant killer, Participant killed) {
        List<Medal> medals = new ArrayList<>();
        Player pKiller = killer.getPlayer();
        Player pKilled = killed.getPlayer();
        MatchTeam killerTeam = MatchTeam.getTeam(pKiller);
        MatchTeam killedTeam = MatchTeam.getTeam(pKilled);
        boolean shot = pKilled.getLastDamageCause().getCause() == EntityDamageEvent.DamageCause.PROJECTILE;
        double dist = pKilled.getLocation().distance(pKiller.getLocation());
        boolean lowHeath = pKiller.getHealth() <= 3;

        if (lowHeath) {
            medals.add(Medal.CLOSE_CALL);
        }

        if (killer.isDead()) {
            medals.add(Medal.AFTERLIFE);
        }

        if (firstBlood) {
            medals.add(Medal.FIRST_BLOOD);
            firstBlood = false;
        }

        if ((NumberSetting.TIME_LIMIT.value() * 60) - MatchCountdown.getSeconds() <= 20) {
            medals.add(Medal.QUICK_KILL);
        }

        if (MatchCountdown.getSeconds() <= 15) {
            medals.add(Medal.CLUTCH_KILL);
        }

        if (shot) {
            killer.getStats().setLongestKill(dist);
            killer.addShot(dist);

            if (dist >= 35) {
                medals.add(Medal.LONG_SHOT);
            }

            if (Arrays.stream(pKiller.getInventory().getContents()).filter(item -> item != null && item.getType().equals(Material.ARROW)).toArray().length == 0) {
                medals.add(Medal.MAKE_IT_COUNT);
            }

            if (EntityUtils.getDistanceFromGround(pKilled) > 2 && !EntityUtils.isOnGround(pKilled)) {
                medals.add(Medal.PRECISION_SHOT);
            }

            if (EntityUtils.getDistanceFromGround(pKiller) > 2 && !EntityUtils.isOnGround(pKiller)) {
                medals.add(Medal.MLG);
            }

            if (((EntityDamageByEntityEvent) pKilled.getLastDamageCause()).getDamager() instanceof Fireball) {
                medals.add(Medal.GREAT_BALLS_OF_FIRE);
            }

            if (((EntityDamageByEntityEvent) pKilled.getLastDamageCause()).getDamager() instanceof TNTPrimed) {
                medals.add(Medal.GREAT_BALLS_OF_FIRE);
            }
        }

        if (killed.getKillstreak() >= 5) {
            medals.add(Medal.STREAK_STRIKER);
        }

        if ((killed.getKillstreak() + 1) % 5 == 0) {
            medals.add(Medal.BUZZ_KILL);
        }

        if (EnumSetting.GAME_TYPE.is(GameType.KING_OF_THE_HILL)) {
            for (Hill hill : MatchManager.getCurrentMap().getYaml().getHills()) {
                if (!hill.isActive()) continue;

                if (hill.isInside(pKiller.getLocation())) {
                    if (hill.isCaptured() && hill.getCapturedBy() == killerTeam
                        || (!hill.isNeutral() && hill.getOwner() == killerTeam)) {
                        // kill enemy while on own or captured hill
                        medals.add(Medal.DEFENDER);
                    } else {
                        // kill enemy while on neutral or enemy hill
                        medals.add(Medal.CONQUEROR);
                    }
                    continue;
                }

                if (hill.isInside(pKilled.getLocation())) {
                    if (hill.isCaptured() && hill.getCapturedBy() == killerTeam
                        || (!hill.isNeutral() && hill.getOwner() == killerTeam)) {
                        // kill enemy that's on own or captured hill
                        medals.add(Medal.WATCH_DOG);
                    } else {
                        // kill enemy that's on neutral or enemy hill
                        medals.add(Medal.TAKEDOWN);
                    }
                }
            }
        }

        if (EnumSetting.GAME_TYPE.is(GameType.HEAD_HUNTER)) {
            int skulls = HeadModule.getTotalSkulls(pKilled);
            if (skulls >= 5) medals.add(Medal.SO_NO_HEAD);
        }

        if (EnumSetting.GAME_TYPE.is(GameType.CAPTURE_THE_FLAG)) {
            boolean killerIsCarrier = FlagUtils.isCarrier(killer);
            boolean killedIsCarrier = FlagUtils.isCarrier(killed);

            if (killerIsCarrier) {
                if (shot) medals.add(Medal.FLAG_SNIPE);
                else medals.add(Medal.FLAG_KILL);
            } else if (killedIsCarrier) {
                if (shot) medals.add(Medal.CARRIER_SNIPE);
                else medals.add(Medal.CARRIER_KILL);
            }

            if (killerIsCarrier && killedIsCarrier) {
                medals.add(Medal.FLAG_JOUST);
            }

            if (lowHeath && killerIsCarrier) {
                medals.add(Medal.CLOSE_DROP);
            }

            if (FlagUtils.inFlagProximity(killerTeam, pKilled, 10)) {
                medals.add(Medal.FLAG_DEFENSE);
            }

            if (killedIsCarrier && FlagUtils.inGoalProximity(killedTeam, pKilled, 15)) {
                medals.add(Medal.STOPPED_SHORT);
            }
        }

        return medals;
    }

}
