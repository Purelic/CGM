package net.purelic.cgm.listeners.modules.bedwars;

import net.purelic.cgm.CGM;
import net.purelic.cgm.core.gamemodes.EnumSetting;
import net.purelic.cgm.core.gamemodes.constants.GameType;
import net.purelic.cgm.events.match.MatchEndEvent;
import net.purelic.cgm.events.match.MatchStartEvent;
import net.purelic.cgm.utils.ParticleUtils;
import net.purelic.commons.utils.TaskUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitRunnable;

public class TNTWaypointModule implements Listener {

    private BukkitRunnable runnable = null;

    @EventHandler
    public void onMatchStart(MatchStartEvent event) {
        if (!EnumSetting.GAME_TYPE.is(GameType.BED_WARS)) return;
        if (TaskUtils.isRunning(this.runnable)) this.runnable.cancel();
        this.runnable = this.getRunnable();
        this.runnable.runTaskTimerAsynchronously(CGM.get(), 0L, 2L);
    }

    @EventHandler
    public void onMatchEnd(MatchEndEvent event) {
        if (TaskUtils.isRunning(this.runnable)) this.runnable.cancel();
    }

    private BukkitRunnable getRunnable() {
        return new BukkitRunnable() {
            @Override
            public void run() {
                Bukkit.getOnlinePlayers().stream()
                        .filter(player -> player.getInventory().contains(Material.TNT))
                        .forEach(player -> ParticleUtils.spawnColoredParticle(
                                player,
                                player.getLocation().clone().add(0, 2.5, 0),
                                true));
            }
        };
    }

}
