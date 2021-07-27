package net.purelic.cgm.listeners.match;

import net.purelic.cgm.CGM;
import net.purelic.cgm.core.constants.MatchState;
import net.purelic.cgm.core.constants.MatchTeam;
import net.purelic.cgm.core.damage.DamageTick;
import net.purelic.cgm.core.damage.KillAssist;
import net.purelic.cgm.core.managers.DamageManger;
import net.purelic.cgm.core.managers.ScoreboardManager;
import net.purelic.cgm.events.match.MatchQuitEvent;
import net.purelic.cgm.events.match.RoundEndEvent;
import net.purelic.cgm.events.participant.MatchTeamEliminateEvent;
import net.purelic.cgm.utils.MatchUtils;
import net.purelic.commons.Commons;
import net.purelic.commons.utils.CommandUtils;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class MatchQuit implements Listener {

    @EventHandler
    public void onMatchQuit(MatchQuitEvent event) {
        Player player = event.getPlayer();
        MatchTeam team = MatchTeam.getTeam(player);

        List<DamageTick> ticks = DamageManger.getLoggedTicks(player.getUniqueId());
        List<KillAssist> assists = DamageManger.getPossibleAssists(ticks);

        if (assists.size() > 0) {
            // Combat log
            Commons.callEvent(new PlayerDeathEvent(
                player,
                Arrays.stream(player.getInventory().getContents().clone()).filter(Objects::nonNull).collect(Collectors.toList()),
                player.getTotalExperience(),
                "combat log"));
        } else if (MatchUtils.isElimination() && MatchUtils.getAlive(team) <= 1) {
            Commons.callEvent(new MatchTeamEliminateEvent(team));
        }

        CGM.get().getMatchManager().removeParticipant(player);

        if (!event.isDisconnected()) {
            CommandUtils.sendSuccessMessage(player, "You joined the " + MatchTeam.OBS.getColoredName() + ChatColor.GREEN + "!");
            MatchTeam.OBS.addPlayer(player);
        } else {
            MatchTeam.removePlayer(player);
        }

        ScoreboardManager.updateSoloBoard();

        if (MatchState.isState(MatchState.STARTED) && CGM.get().getMatchManager().allEliminated()) {
            Commons.callEvent(new RoundEndEvent());
        }
    }

}
