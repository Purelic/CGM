package net.purelic.cgm.listeners.modules.stats;

import net.purelic.cgm.CGM;
import net.purelic.cgm.tab.TabManager;
import net.purelic.cgm.core.stats.MatchStats;
import net.purelic.cgm.core.stats.PlayerStats;
import net.purelic.cgm.events.match.MatchEndEvent;
import net.purelic.cgm.events.match.MatchStartEvent;
import net.purelic.commons.utils.ServerUtils;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;

public class MatchStatsModule implements Listener {

    private static final List<MatchStats> STATS = new ArrayList<>();
    private static MatchStats current = null;

    @EventHandler (priority = EventPriority.MONITOR)
    public void onMatchStart(MatchStartEvent event) {
        if (current == null) current = new MatchStats(event.getMap(), event.getGameMode());
        TabManager.updateStatsAll();
    }

    @EventHandler (priority = EventPriority.MONITOR)
    public void onMatchEnd(MatchEndEvent event) {
        if (event.isForced()) {
            current = null;
            return;
        }

        STATS.add(current);

        new BukkitRunnable() {
            @Override
            public void run() {
                if (!ServerUtils.isPrivate()) current.save();
                current = null;
            }
        }.runTaskAsynchronously(CGM.get());
    }

    public static void setCurrent(MatchStats matchStats) {
        current = matchStats;
    }

    public static MatchStats getCurrent() {
        return current;
    }

    public static PlayerStats getStats(Player player) {
        if (current == null) return null;
        return current.getStats(player);
    }

    public static boolean hasStats(Player player) {
        return current != null && current.hasStats(player);
    }

}
