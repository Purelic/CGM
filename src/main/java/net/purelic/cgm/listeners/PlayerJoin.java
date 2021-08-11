package net.purelic.cgm.listeners;

import net.md_5.bungee.api.ChatColor;
import net.purelic.cgm.core.constants.MatchState;
import net.purelic.cgm.core.constants.MatchTeam;
import net.purelic.cgm.core.managers.MatchManager;
import net.purelic.cgm.core.managers.ScoreboardManager;
import net.purelic.cgm.core.managers.TabManager;
import net.purelic.cgm.core.match.Participant;
import net.purelic.cgm.league.LeagueModule;
import net.purelic.commons.utils.ChatUtils;
import net.purelic.commons.utils.PlayerUtils;
import net.purelic.commons.utils.ServerUtils;
import net.purelic.commons.utils.TaskUtils;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class PlayerJoin implements Listener {

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        ScoreboardManager.setScoreboard(player);

        // specifically flag first joins when adding a a player to obs because
        // we put the player in spectator mode, then hide them from participants,
        // then put them in adventure mode with a 1 tick delay (see below)
        MatchTeam.OBS.addPlayer(player, true);

        if (ServerUtils.isRanked() && LeagueModule.get().isPlaying(player)) {
            MatchTeam team = LeagueModule.get().getTeam(player);
            PlayerUtils.performCommand(player, "join " + team.getName());
        }

        if (MatchState.isActive()) {
            player.performCommand("match");
        }

        // TODO replace with "isMatchJoinable" that also takes into consideration if teams are full too
        if (MatchTeam.getTeam(player) == MatchTeam.OBS && MatchState.isState(MatchState.STARTED) && !ServerUtils.isRanked()) {
            ChatUtils.sendTitle(
                player,
                ChatColor.GOLD + MatchManager.getCurrentGameMode().getName(),
                ChatColor.AQUA + "/join" + ChatColor.WHITE + " to play!",
                60
            );
        }

        for (Participant participant : MatchManager.getParticipants()) {
            if (participant.isDead()) {
                Player dead = participant.getPlayer();
                player.hidePlayer(dead);
            }
        }

        TabManager.enable(player);

        // 1 tick after joining hide them from participants and set their game mode back to adventure
        TaskUtils.runLater(() -> {
            if (MatchState.isState(MatchState.STARTED) && MatchTeam.getTeam(player) == MatchTeam.OBS) {
                for (Participant participant : MatchManager.getParticipants()) {
                    Player online = participant.getPlayer();
                    online.hidePlayer(player);
                }
            }

            player.setGameMode(GameMode.ADVENTURE);
            player.setAllowFlight(!MatchState.isState(MatchState.WAITING, MatchState.VOTING));
        }, 1L);
    }

}
