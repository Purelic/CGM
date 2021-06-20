package net.purelic.cgm.listeners;

import net.purelic.cgm.CGM;
import net.purelic.cgm.core.constants.MatchState;
import net.purelic.cgm.core.constants.MatchTeam;
import net.purelic.cgm.core.managers.MatchManager;
import net.purelic.cgm.core.managers.TabManager;
import net.purelic.cgm.events.match.MatchQuitEvent;
import net.purelic.commons.Commons;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerQuit implements Listener {

    @EventHandler (priority = EventPriority.LOWEST)
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        CGM.getPlugin().getScoreboardManager().removePlayer(player);

        if (player.getVehicle() != null) {
            player.getVehicle().eject();
        }

        if (MatchManager.getParticipant(player) != null || MatchTeam.getTeam(player) != MatchTeam.OBS) {
            Commons.callEvent(new MatchQuitEvent(player, true));
        } else {
            MatchTeam.removePlayer(player);
        }

        TabManager.destroy(player);
    }

}
