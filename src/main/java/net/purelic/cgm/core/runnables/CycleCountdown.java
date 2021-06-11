package net.purelic.cgm.core.runnables;

import net.md_5.bungee.api.ChatColor;
import net.purelic.cgm.CGM;
import net.purelic.cgm.core.gamemodes.CustomGameMode;
import net.purelic.cgm.core.managers.MatchManager;
import net.purelic.cgm.core.maps.CustomMap;
import net.purelic.cgm.utils.PlayerUtils;
import net.purelic.commons.utils.ChatUtils;
import net.purelic.commons.utils.ServerUtils;
import net.purelic.commons.utils.VersionUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.github.paperspigot.Title;

public class CycleCountdown extends BukkitRunnable {

    private static final int RESTART_THRESHOLD = 50;

    private static BukkitRunnable countdown;
    private static int seconds;
    private static CustomMap map;
    private static CustomGameMode gameMode;
    private static boolean restarting;

    public CycleCountdown() {
        this(10, MatchManager.getNextMap(), CGM.getPlugin().getMatchManager().getNextGameMode());
    }

    public CycleCountdown(int seconds, CustomMap map, CustomGameMode gameMode) {
        countdown = this;
        CycleCountdown.seconds = seconds;
        CycleCountdown.map = map;
        CycleCountdown.gameMode = gameMode;
        restarting = MatchManager.getMatches() >= RESTART_THRESHOLD && seconds != 0 && (map == null || gameMode == null) && !ServerUtils.isPrivate();

        if (restarting) {
            for (Player player : Bukkit.getOnlinePlayers()) {
                ChatUtils.sendTitle(
                    player,
                    "",
                    ChatColor.AQUA + "Server Restarting...",
                    100
                );
            }
        }
    }

    @Override
    public void run() {
        if (seconds == 0) {
            ChatUtils.broadcastActionBar("");
            PlayerUtils.setLevelAll(0);
            this.cancel();

            if (restarting) {
//                Commons.setRestarting(true);
//                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "restart");
                Bukkit.getServer().shutdown();
            } else {
                MatchManager.cycle();
            }

            return;
        }

        String message;

        if (restarting) {
            message = "Restarting server in " + ChatColor.AQUA +
                seconds + ChatColor.RESET + " second" + ((seconds == 1) ? "!" : "s!");
        } else if (map == null || gameMode == null) {
            message = "Returning to lobby in " + ChatColor.AQUA +
                seconds + ChatColor.RESET + " second" + ((seconds == 1) ? "!" : "s!");
        } else {
            message = "Cycling to " + gameMode.getColoredName() + ChatColor.RESET +
                " on " + map.getColoredName() + " in " + ChatColor.AQUA + seconds +
                ChatColor.RESET + " second" + ((seconds == 1) ? "!" : "s!");
        }

        PlayerUtils.setLevelAll(seconds);
        ChatUtils.broadcastActionBar(message);

        if (seconds % 10 == 0) {
            Bukkit.getOnlinePlayers().stream().filter(VersionUtils::isLegacy).forEach(player -> player.sendMessage(message));
        }

        seconds--;
    }

    public static BukkitRunnable getCountdown() {
        return countdown;
    }

    public static void setCountdown(int seconds) {
        CycleCountdown.seconds = seconds;
    }

    public static void setGameMode(CustomGameMode gameMode) {
        CycleCountdown.gameMode = gameMode;
    }

    public static void setMap(CustomMap map) {
        CycleCountdown.map = map;
    }

}
