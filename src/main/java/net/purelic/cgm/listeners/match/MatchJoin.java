package net.purelic.cgm.listeners.match;

import net.purelic.cgm.core.constants.MatchState;
import net.purelic.cgm.core.constants.MatchTeam;
import net.purelic.cgm.core.managers.MatchManager;
import net.purelic.cgm.core.managers.ScoreboardManager;
import net.purelic.cgm.events.match.MatchJoinEvent;
import net.purelic.cgm.listeners.modules.stats.MatchStatsModule;
import net.purelic.commons.utils.CommandUtils;
import net.purelic.commons.utils.ServerUtils;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class MatchJoin implements Listener {

    private static boolean addParticipants = true;

    @EventHandler
    public void onMatchJoin(MatchJoinEvent event) {
        Player player = event.getPlayer();
        MatchTeam team = event.getTeam();

        team.addPlayer(player);
        CommandUtils.sendSuccessMessage(player, "You joined " + team.getColoredName() + ChatColor.GREEN + "!");

        if (ServerUtils.isRanked()) {
            MatchStatsModule.getStats(player);
        }

        if (MatchState.isState(MatchState.STARTED) && addParticipants) {
            MatchManager.addParticipant(player, false, event.isForced());
        }

        // update solo scoreboards (could display previous scores or # of alive players)
        ScoreboardManager.updateSoloBoard();
    }

    public static void setAddParticipants(boolean addParticipants) {
        MatchJoin.addParticipants = addParticipants;
    }

}
