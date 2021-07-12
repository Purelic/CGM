package net.purelic.cgm.utils;

import net.purelic.cgm.commands.toggles.ToggleSpectatorsCommand;
import net.purelic.cgm.core.constants.MatchState;
import net.purelic.cgm.core.constants.MatchTeam;
import net.purelic.cgm.core.gamemodes.EnumSetting;
import net.purelic.cgm.core.gamemodes.NumberSetting;
import net.purelic.cgm.core.gamemodes.ToggleSetting;
import net.purelic.cgm.core.gamemodes.constants.TeamType;
import net.purelic.cgm.core.managers.MatchManager;
import net.purelic.cgm.core.match.Participant;
import net.purelic.cgm.core.match.constants.ParticipantState;
import net.purelic.commons.Commons;
import net.purelic.commons.profile.Preference;
import net.purelic.commons.profile.Profile;
import net.purelic.commons.utils.TaskUtils;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class PlayerUtils {

    private static final HashMap<Player, Location> AFK_LOCATIONS = new HashMap<>();
    private static final List<Player> AFK_PLAYERS = new ArrayList<>();

    public static void reset(Player player, GameMode gameMode) {
        boolean fly = !MatchState.isState(MatchState.WAITING, MatchState.VOTING);
        TaskUtils.runLater(() -> {
            player.setAllowFlight(fly);
            player.setFlying(fly);
        }, 10);
        player.setGameMode(gameMode);
        player.setExp(0);
        player.setLevel(0);
        player.setMaxHealth(20);
        player.setHealth(20);
        player.setSaturation(20);
        player.setFoodLevel(20);
        player.setFireTicks(0);
        player.setSneaking(false);
        player.setSprinting(false);
        player.spigot().setAffectsSpawning(false);
        player.spigot().setCollidesWithEntities(false);
        clearInventory(player);
        PlayerUtils.clearEffects(player);
    }

    /* Showing and hiding players can actually cause small amounts of lag,
       so bulk showing/hiding players can cause lag spikes.

       By only performing the show and hide methods when necessary we can
       reduce the possible amount of lag when updating player visibility. */

    public static void showPlayer(Player player, Player other) {
        // Only show the other player if they're hidden
        if (!player.canSee(other)) player.showPlayer(other);
    }

    public static void hidePlayer(Player player, Player other) {
        // Only hide the other player if they're shown
        if (player.canSee(other)) player.hidePlayer(other);
    }

    public static void showEveryone() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            for (Player online : Bukkit.getOnlinePlayers()) {
                if (online == player) continue;
                showPlayer(player, online);
                showPlayer(online, player);
            }
        }
    }

    public static void hideFromAll(Player player) {
        for (Player online : Bukkit.getOnlinePlayers()) {
            if (online == player) continue;
            hidePlayer(online, player);
        }
    }

    public static void hideObs() {
        for (Participant participant : MatchManager.getParticipants()) {
            for (Player obs : MatchTeam.OBS.getPlayers()) {
                hidePlayer(participant.getPlayer(), obs);
            }
        }
    }

    public static void updateVisibility(Player player) {
        for (Player other : Bukkit.getOnlinePlayers()) {
            if (other == player) continue;
            updateVisibility(player, other);
        }
    }

    private static void updateVisibility(Player player, Player other) {
        if (!MatchState.isActive()) {
            showPlayer(player, other);
            showPlayer(other, player);
            return;
        }

        boolean bothSpectating = isObserving(player) && isObserving(other);
        boolean bothPlaying = !isObserving(player) && !isObserving(other);

        if (bothSpectating) {
            if (ToggleSpectatorsCommand.hideSpectators(player)) {
                hidePlayer(player, other);
            } else {
                showPlayer(player, other);
            }

            if (ToggleSpectatorsCommand.hideSpectators(other)) {
                hidePlayer(other, player);
            } else {
                showPlayer(other, player);
            }
        } else if (bothPlaying) {
            // Hide the other player if they're respawning, otherwise show them
            if (MatchManager.getParticipant(other).isState(ParticipantState.RESPAWNING))  {
                hidePlayer(player, other);
            } else {
                showPlayer(player, other);
            }

            if (MatchManager.getParticipant(player).isState(ParticipantState.RESPAWNING))  {
                hidePlayer(other, player);
            } else {
                showPlayer(other, player);
            }
        } else if (isObserving(player) && !isObserving(other)) {
            hidePlayer(other, player);

            // Hide the other player if they're respawning, otherwise show them
            if (MatchManager.getParticipant(other).isState(ParticipantState.RESPAWNING))  {
                hidePlayer(player, other);
            } else {
                showPlayer(player, other);
            }
        } else { // player is playing and other is observing
            showPlayer(other, player);
            hidePlayer(player, other);
        }
    }

    public static boolean isObserving(Player player) {
        if (!MatchState.isState(MatchState.STARTED)) return true;
        if (MatchTeam.getTeam(player) == MatchTeam.OBS) return true;
        return MatchManager.getParticipant(player).isState(ParticipantState.ELIMINATED, ParticipantState.QUEUED);
    }

    public static Color getColorPreference(Participant participant) {
        return PlayerUtils.getColorPreference(participant.getPlayer());
    }

    public static Color getColorPreference(Player player) {
        MatchTeam team = MatchTeam.getTeam(player);
        Color color = ColorConverter.convert(team.getColor());

        if (EnumSetting.TEAM_TYPE.is(TeamType.SOLO)) {
            Profile profile = Commons.getProfile(player);
            Object colorPref = profile.getPreference(Preference.ARMOR_COLOR);
            if (colorPref != null) color = Color.fromRGB(((Long) colorPref).intValue());
        }

        return color;
    }

    public static void clearInventory(Player player) {
        clearInventory(player.getInventory());
        // clearItems(player.getInventory().getArmorContents()); // TODO will this work?

        if (player.getItemOnCursor().getType() != Material.AIR) {
            player.setItemOnCursor(null);
        }
//
//        for (ItemStack item : player.getInventory().getContents()) {
//            if (item != null) {
//                player.getInventory().clear();
//                break;
//            }
//        }

        for (ItemStack item : player.getInventory().getArmorContents()) {
            if (item.getType() != Material.AIR) {
                player.getInventory().setArmorContents(null);
                break;
            }
        }

        if (player.getOpenInventory() != null) {
            clearInventory(player.getOpenInventory().getTopInventory());
            // player.getOpenInventory().getTopInventory().clear();
        }
    }

    private static void clearInventory(Inventory inventory) {
        List<ItemStack> remove = new ArrayList<>();
        
        for (ItemStack item : inventory.getContents()) {
            if (item != null && item.getType() != Material.AIR) {
                remove.add(item);
            }
        }

        ItemStack[] items = new ItemStack[remove.size()];
        remove.toArray(items);
        inventory.removeItem(items);
    }

    private static void clearItems(ItemStack[] items) {
        for (ItemStack item : items) {
            if (item != null && item.getType() != Material.AIR) {
                item.setType(Material.AIR);
            }
        }
    }

    public static void clearEffectsAll() {
        Bukkit.getOnlinePlayers().forEach(PlayerUtils::clearEffects);
    }

    public static void clearEffects(Player player) {
        player.getActivePotionEffects().forEach(effect -> player.removePotionEffect(effect.getType()));
    }

    public static void addPermanentEffect(Player player, PotionEffectType type) {
        PlayerUtils.addPermanentEffect(player, type, 0);
    }

    public static void addPermanentEffect(Player player, PotionEffectType type, int amplifier) {
        if (amplifier < 0) return;
        player.removePotionEffect(type);
        player.addPotionEffect(new PotionEffect(type, Integer.MAX_VALUE, amplifier, true, false));
    }

    public static void applyDefaultEffects(Player player) {
        addPermanentEffect(player, PotionEffectType.SPEED, NumberSetting.PLAYER_SPEED.value() - 1);
        addPermanentEffect(player, PotionEffectType.JUMP, NumberSetting.PLAYER_JUMP_BOOST.value() - 1);
        addPermanentEffect(player, PotionEffectType.DAMAGE_RESISTANCE, NumberSetting.PLAYER_RESISTANCE.value() - 1);
        addPermanentEffect(player, PotionEffectType.FAST_DIGGING, NumberSetting.PLAYER_HASTE.value() - 1);
        addPermanentEffect(player, PotionEffectType.INCREASE_DAMAGE, NumberSetting.PLAYER_STRENGTH.value() - 1);
        if (ToggleSetting.PLAYER_INVISIBILITY.isEnabled()) addPermanentEffect(player, PotionEffectType.INVISIBILITY);
        if (ToggleSetting.PLAYER_FIRE_RESISTANCE.isEnabled()) addPermanentEffect(player, PotionEffectType.FIRE_RESISTANCE);
        if (ToggleSetting.PLAYER_BLINDNESS.isEnabled()) addPermanentEffect(player, PotionEffectType.BLINDNESS);
        if (MatchManager.getCurrentMap() != null && MatchManager.getCurrentMap().getYaml().hasNightVision()) PlayerUtils.addPermanentEffect(player, PotionEffectType.NIGHT_VISION);
    }

    public static void logRespawnLocation(Player player) {
        AFK_LOCATIONS.put(player, player.getLocation().clone());
    }

    public static void logDeathLocation(Player player) {
        Location respawnLoc = AFK_LOCATIONS.get(player);
        Location deathLoc = player.getLocation().clone();

        if ((int) respawnLoc.getYaw() == (int) deathLoc.getYaw()
            && respawnLoc.getPitch() == deathLoc.getPitch()) {
            if (AFK_PLAYERS.contains(player)) {
                player.kickPlayer("You were removed for being AFK!");
            } else {
                AFK_PLAYERS.add(player);
            }
        } else {
            AFK_PLAYERS.remove(player);
        }

        AFK_LOCATIONS.remove(player);
    }

    public static Player getClosestEnemy(MatchTeam team) {
        return getClosestEnemy(team.getPlayers().get(0));
    }

    public static Player getClosestEnemy(Player tracker) {
        Player target = null;
        double dist = Double.MAX_VALUE;

        for (Participant participant : MatchManager.getParticipants()) {
            Player player = participant.getPlayer();

            if (MatchTeam.isSameTeam(player, tracker)
                    || player == tracker
                    || participant.isDead()
                    || participant.isEliminated()) {
                continue;
            }

            double tempDist = player.getLocation().distance(tracker.getLocation());

            if (tempDist > dist) continue;

            dist = tempDist;
            target = player;
        }

        return target;
    }

    public static void setLevelAll(int level) {
        setLevelAll(level, 0F);
    }

    public static void setLevelAll(int level, float exp) {
        for (Player player : Bukkit.getOnlinePlayers()) {
            setLevel(player, level, exp);
        }
    }

    public static void setLevel(Player player, int level) {
        setLevel(player, level, 0F);
    }

    public static void setLevel(Player player, int level, float exp) {
        player.setLevel(level);
        player.setExp(exp);
    }

}
