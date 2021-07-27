package net.purelic.cgm.listeners.modules;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.purelic.cgm.CGM;
import net.purelic.cgm.core.constants.MatchState;
import net.purelic.cgm.core.gamemodes.NumberSetting;
import net.purelic.cgm.core.managers.MatchManager;
import net.purelic.cgm.core.managers.ScoreboardManager;
import net.purelic.cgm.core.maps.CustomMap;
import net.purelic.cgm.events.match.*;
import net.purelic.cgm.scoreboards.ScoreboardTimer;
import net.purelic.commons.utils.ChatUtils;
import net.purelic.commons.utils.TaskUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.WorldBorder;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitRunnable;

public class WorldBorderModule implements Listener {

    private BukkitRunnable borderRunnable;
    private BukkitRunnable updaterRunnable;

    @EventHandler
    public void onMatchCycle(MatchCycleEvent event) {
        if (!event.hasMap()) return;
        this.resetWorldBorder();
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onRoundStart(RoundStartEvent event) {
        this.resetWorldBorder();

        WorldBorder border = MatchManager.getCurrentMap().getWorld().getWorldBorder();
        if (NumberSetting.WB_SHRINK_SPEED.value() > 0) this.shrinkBorder(border);
    }

    @EventHandler
    public void onRoundEnd(RoundEndEvent event) {
        TaskUtils.cancelIfRunning(this.borderRunnable);
        TaskUtils.cancelIfRunning(this.updaterRunnable);

        WorldBorder border = MatchManager.getCurrentMap().getWorld().getWorldBorder();
        border.setSize(border.getSize());

        if (CGM.getPlaylist().isUHC()) {
            World nether = Bukkit.getWorld("uhc_nether");
            border = nether.getWorldBorder();
            border.setSize(border.getSize());
        }
    }

    private void resetWorldBorder() {
        CustomMap map = MatchManager.getCurrentMap();
        World world = map.getWorld();
        WorldBorder border = world.getWorldBorder();
        int maxSize = NumberSetting.WB_MAX_SIZE.value();

        border.setCenter(map.getYaml().getObsSpawn().getLocation(world));
        border.setSize(maxSize % 2 == 0 ? maxSize + 1 : maxSize);
        border.setDamageBuffer(0);

        // Updates the world border timer to have the correct border size
        ScoreboardManager.getMatchScoreboard().updateTimers();

        if (CGM.getPlaylist().isUHC()) {
            world = Bukkit.getWorld("uhc_nether");
            border = world.getWorldBorder();
            maxSize = maxSize / 2;

            border.setCenter(new Location(world, 0, 0, 0));
            border.setSize(maxSize % 2 == 0 ? maxSize + 1 : maxSize);
            border.setDamageBuffer(0);
        }
    }

    private void shrinkBorder(WorldBorder border) {
        int delay = NumberSetting.WB_SHRINK_DELAY.value();
        int maxSize = NumberSetting.WB_MAX_SIZE.value();
        int minSize = NumberSetting.WB_MIN_SIZE.value();
        int speed = NumberSetting.WB_SHRINK_SPEED.value();

        if (delay > 0) {
            ChatUtils.sendMessageAll(
                new ComponentBuilder("\n")
                    .append(" WORLD BORDER » ").color(ChatColor.DARK_AQUA).bold(true)
                    .append("The border is currently " + ChatColor.AQUA + maxSize + "x" + maxSize + ChatColor.RESET +
                        " and will start shrinking in " + ChatColor.AQUA + delay + ChatColor.RESET +
                        " minute" + (delay == 1 ? "" : "s")).reset()
                    .append("\n").reset()
            );
        }

        this.updaterRunnable = new BukkitRunnable() {
            @Override
            public void run() {
                if (!MatchState.isState(MatchState.STARTED)) {
                    this.cancel();
                    return;
                }

                int border = (int) (MatchManager.getCurrentMap().getWorld().getWorldBorder().getSize() / 2);
                String score = ChatColor.DARK_AQUA + "Border: " + ChatColor.WHITE + "±" + border;
                ScoreboardManager.setScore(ScoreboardManager.getMatchScoreboard().getTimerSlot(ScoreboardTimer.BORDER), score);

                if (border == NumberSetting.WB_MIN_SIZE.value()) {
                    this.cancel();
                }
            }
        };

        this.borderRunnable = new BukkitRunnable() {
            @Override
            public void run() {
                if (!MatchState.isState(MatchState.STARTED)) {
                    this.cancel();
                    return;
                }

                TaskUtils.runTimerAsync(updaterRunnable, 100L);

                ChatUtils.sendMessageAll(
                    new ComponentBuilder("\n")
                        .append(" WORLD BORDER » ").color(ChatColor.DARK_AQUA).bold(true)
                        .append("The border has started shrinking! It will reach " + ChatColor.AQUA + minSize + "x" + minSize + ChatColor.RESET +
                            " in " + ChatColor.AQUA + speed + ChatColor.RESET + " minute" + (speed == 1 ? "" : "s")).reset()
                        .append("\n").reset()
                );

                border.setSize(minSize % 2 == 0 ? minSize + 1 : minSize, speed * 60L);

                if (CGM.getPlaylist().isUHC()) {
                    World nether = Bukkit.getWorld("uhc_nether");
                    WorldBorder netherBorder = nether.getWorldBorder();
                    int netherSize = minSize / 2;
                    netherBorder.setSize(netherSize % 2 == 0 ? netherSize + 1 : netherSize, speed * 60L);
                }
            }
        };

        this.borderRunnable.runTaskLaterAsynchronously(CGM.get(), delay * 60L * 20L);

    }

}
