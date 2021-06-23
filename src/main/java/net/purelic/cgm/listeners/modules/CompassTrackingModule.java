package net.purelic.cgm.listeners.modules;

import net.md_5.bungee.api.ChatColor;
import net.purelic.cgm.CGM;
import net.purelic.cgm.core.gamemodes.EnumSetting;
import net.purelic.cgm.core.gamemodes.ToggleSetting;
import net.purelic.cgm.core.gamemodes.constants.CompassTrackingType;
import net.purelic.cgm.core.gamemodes.constants.GameType;
import net.purelic.cgm.core.maps.flag.Flag;
import net.purelic.cgm.core.maps.hill.Hill;
import net.purelic.cgm.core.runnables.ObjectiveTracker;
import net.purelic.cgm.core.runnables.PlayerTracker;
import net.purelic.cgm.events.match.RoundEndEvent;
import net.purelic.cgm.events.match.RoundStartEvent;
import net.purelic.cgm.utils.BedUtils;
import net.purelic.cgm.utils.FlagUtils;
import net.purelic.cgm.utils.HillUtils;
import net.purelic.cgm.utils.PlayerUtils;
import net.purelic.commons.utils.ChatUtils;
import net.purelic.commons.utils.NickUtils;
import net.purelic.commons.utils.TaskUtils;
import net.purelic.commons.utils.VersionUtils;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.text.DecimalFormat;

public class CompassTrackingModule implements Listener {

    private BukkitRunnable tracker;

    @EventHandler
    public void onRoundStart(RoundStartEvent event) {
        if (TaskUtils.isRunning(this.tracker)) {
            this.tracker.cancel();
        }

        if (!ToggleSetting.PLAYER_COMPASS_ENABLED.isEnabled()) return;

        if (EnumSetting.PLAYER_COMPASS_TYPE.is(CompassTrackingType.PLAYER)) {
            this.tracker = new PlayerTracker();
        } else if (EnumSetting.PLAYER_COMPASS_TYPE.is(CompassTrackingType.OBJECTIVE)) {
            this.tracker = new ObjectiveTracker();
        }

        if (this.tracker == null) return;

        this.tracker.runTaskTimerAsynchronously(CGM.get(), 0L, 2L);
    }

    @EventHandler
    public void onRoundEnd(RoundEndEvent event) {
        if (TaskUtils.isRunning(tracker)) {
            this.tracker.cancel();
        }

        this.tracker = null;
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (!event.getAction().name().contains("RIGHT_CLICK")) return;

        Player player = event.getPlayer();

        if (!ToggleSetting.PLAYER_COMPASS_ENABLED.isEnabled()
                || !isHoldingCompass(player)) return;

        String trackingMessage = "";

        if (ToggleSetting.PLAYER_COMPASS_DISPLAY.isEnabled()) {
            trackingMessage = getTrackingMessage(player);
        } else {
            trackingMessage = getNoDisplayMessage();
        }

        if (trackingMessage.isEmpty()) return;

        if (VersionUtils.isLegacy(player)) {
            player.sendMessage(trackingMessage);
        } else {
            ChatUtils.sendActionBar(player, trackingMessage);
        }
    }

    public static boolean isHoldingCompass(Player player) {
        ItemStack item = player.getItemInHand();
        return item != null && item.getType() == Material.COMPASS;
    }

    public static String getTrackingMessage(Player tracker) {
        if (EnumSetting.PLAYER_COMPASS_TYPE.is(CompassTrackingType.PLAYER)) {
            return getPlayerTrackingMessage(tracker, PlayerUtils.getClosestEnemy(tracker));
        } else if (EnumSetting.PLAYER_COMPASS_TYPE.is(CompassTrackingType.OBJECTIVE)) {
            GameType gameType = EnumSetting.GAME_TYPE.get();

            if (gameType == GameType.CAPTURE_THE_FLAG) {
                Flag flag = FlagUtils.getClosestFlag(tracker);
                return getFlagTrackingMessage(tracker, flag);
            } else if (gameType == GameType.KING_OF_THE_HILL) {
                Hill hill = HillUtils.getClosestHill(tracker);
                return getHillTrackingMessage(tracker, hill);
            } else if (gameType == GameType.HEAD_HUNTER) {
                Hill hill = HillUtils.getClosestHill(tracker);
                return getGoalTrackingMessage(tracker, hill);
            }
        }

        return "";
    }

    public static String getNoDisplayMessage() {
        if (EnumSetting.PLAYER_COMPASS_TYPE.is(CompassTrackingType.PLAYER)) {
            return "" + ChatColor.RED + ChatColor.BOLD + "Tracking Closest Player";
        } else if (EnumSetting.PLAYER_COMPASS_TYPE.is(CompassTrackingType.OBJECTIVE)) {
            GameType gameType = EnumSetting.GAME_TYPE.get();

            if (gameType == GameType.CAPTURE_THE_FLAG) {
                return "" + ChatColor.RED + ChatColor.BOLD + "Tracking Closest Flag";
            } else if (gameType == GameType.KING_OF_THE_HILL) {
                return "" + ChatColor.RED + ChatColor.BOLD + "Tracking Closest Hill";
            } else if (gameType == GameType.HEAD_HUNTER) {
                return "" + ChatColor.RED + ChatColor.BOLD + "Tracking Closest Goal";
            }
        }

        return "";
    }

    public static String getPlayerTrackingMessage(Player tracker, Player target) {
        if (EnumSetting.GAME_TYPE.is(GameType.BED_WARS) && !BedUtils.canUseTracker(tracker)) {
            return "" + ChatColor.RED + ChatColor.BOLD + "Beds Not Broken Yet";
        }

        if (target == null) return "" + ChatColor.RED + ChatColor.BOLD + "No Target Found";

        double dist = tracker.getLocation().distance(target.getLocation());

        if (dist > 5.0) {
            String distValue = (new DecimalFormat("#.#").format(dist));

            return "" + ChatColor.WHITE + ChatColor.BOLD + "Tracking: " +
                    ChatColor.RESET + NickUtils.getDisplayName(target) +
                    ChatColor.WHITE + ChatColor.BOLD + "  Distance: " +
                    ChatColor.RESET + ChatColor.GREEN + ChatColor.BOLD +
                    distValue + (distValue.contains(".") ? "" : ".0");
        } else {
            return "" + ChatColor.WHITE + ChatColor.BOLD + "Tracking: " +
                    ChatColor.RESET + NickUtils.getDisplayName(target) +
                    ChatColor.WHITE + ChatColor.BOLD + "  Distance: " +
                    ChatColor.RESET + ChatColor.GREEN + ChatColor.BOLD +
                    "< 5 Blocks";
        }
    }

    public static String getFlagTrackingMessage(Player tracker, Flag flag) {
        if (flag == null) return "" + ChatColor.RED + ChatColor.BOLD + "No Flag Found";

        Location flagLoc = FlagUtils.getCurrentFlagLocation(flag);

        String display = flag.hasCarrier()
                ? NickUtils.getDisplayName(flag.getCarrier().getPlayer())
                : flag.getTitle();

        return getObjectiveTrackingMessage(tracker, display, flagLoc);
    }

    public static String getHillTrackingMessage(Player tracker, Hill hill) {
        if (hill == null) return "" + ChatColor.RED + ChatColor.BOLD + "No Hill Found";
        return getObjectiveTrackingMessage(tracker, hill.getTitle(), hill.getCenter());
    }

    public static String getGoalTrackingMessage(Player tracker, Hill hill) {
        if (hill == null) return "" + ChatColor.RED + ChatColor.BOLD + "No Goal Found";
        return getObjectiveTrackingMessage(tracker, hill.getTitle(), hill.getCenter());
    }

    public static String getObjectiveTrackingMessage(Player tracker, String display, Location location) {
        double dist = tracker.getLocation().distance(location);

        if (dist > 1.0) {
            String distValue = (new DecimalFormat("#.#").format(dist));

            return "" + ChatColor.WHITE + ChatColor.BOLD + "Tracking: " +
                    ChatColor.RESET + display +
                    ChatColor.WHITE + ChatColor.BOLD + "  Distance: " +
                    ChatColor.RESET + ChatColor.GREEN + ChatColor.BOLD +
                    distValue + (distValue.contains(".") ? "" : ".0");
        } else {
            return "" + ChatColor.WHITE + ChatColor.BOLD + "Tracking: " +
                    ChatColor.RESET + display +
                    ChatColor.WHITE + ChatColor.BOLD + "  Distance: " +
                    ChatColor.RESET + ChatColor.GREEN + ChatColor.BOLD +
                    "< 1 Block";
        }
    }

}
