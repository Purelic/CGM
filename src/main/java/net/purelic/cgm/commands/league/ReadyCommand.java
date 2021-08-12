package net.purelic.cgm.commands.league;

import cloud.commandframework.Command;
import cloud.commandframework.bukkit.BukkitCommandManager;
import net.md_5.bungee.api.ChatColor;
import net.purelic.cgm.core.constants.MatchState;
import net.purelic.cgm.core.constants.MatchTeam;
import net.purelic.cgm.core.runnables.StartCountdown;
import net.purelic.cgm.league.LeagueModule;
import net.purelic.commons.commands.parsers.CustomCommand;
import net.purelic.commons.utils.CommandUtils;
import net.purelic.commons.utils.NickUtils;
import net.purelic.commons.utils.ServerUtils;
import net.purelic.commons.utils.TaskUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class ReadyCommand implements CustomCommand {

    public static final List<MatchTeam> TEAMS_READY = new ArrayList<>();
    public static final List<Player> PLAYERS_READY = new ArrayList<>();

    @Override
    public Command.Builder<CommandSender> getCommandBuilder(BukkitCommandManager<CommandSender> mgr) {
        return mgr.commandBuilder("ready")
            .senderType(Player.class)
            .handler(c -> {
                Player player = (Player) c.getSender();
                MatchTeam team = MatchTeam.getTeam(player);

                if (!ServerUtils.isRanked()) {
                    CommandUtils.sendErrorMessage(player, "This command is only available on ranked servers!");
                    return;
                }

                if (!MatchState.isState(MatchState.STARTING)) {
                    CommandUtils.sendErrorMessage(player, "You can't use this command right now!");
                    return;
                }

                if (!LeagueModule.get().isPlaying(player)) {
                    CommandUtils.sendErrorMessage(player, "Spectators can't use this command!");
                    return;
                }

                if (TaskUtils.isRunning(StartCountdown.getCountdown()) && StartCountdown.getSeconds() <= 5) {
                    CommandUtils.sendErrorMessage(player, "The start countdown is already below 5 seconds!");
                    return;
                }

                int totalPlaces = LeagueModule.get().getTotalPlaces();
                int majority = totalPlaces == 2 ? 2 : (int) (totalPlaces * 0.75);
                boolean start;

                if (team == MatchTeam.SOLO) {
                    if (PLAYERS_READY.contains(player)) {
                        PLAYERS_READY.remove(player);
                        String progress = ChatColor.GRAY + " (" + PLAYERS_READY.size() + "/" + majority + ")";
                        Bukkit.broadcastMessage(NickUtils.getDisplayName(player) + " is no longer /ready!" + progress);
                    } else {
                        PLAYERS_READY.add(player);
                        String progress = ChatColor.GRAY + " (" + PLAYERS_READY.size() + "/" + majority + ")";
                        Bukkit.broadcastMessage(NickUtils.getDisplayName(player) + " is /ready!" + progress);
                    }

                    start = PLAYERS_READY.size() >= majority;
                } else {
                    if (TEAMS_READY.contains(team)) {
                        TEAMS_READY.remove(team);
                        String progress = ChatColor.GRAY + " (" + TEAMS_READY.size() + "/" + majority + ")";
                        Bukkit.broadcastMessage(team.getColoredName() + " is no longer /ready!" + progress);
                    } else {
                        TEAMS_READY.add(team);
                        String progress = ChatColor.GRAY + " (" + TEAMS_READY.size() + "/" + majority + ")";
                        Bukkit.broadcastMessage(team.getColoredName() + " is /ready!" + progress);
                    }

                    start = TEAMS_READY.size() >= majority;
                }

                if (start) {
                    String prefix = (totalPlaces == 2 ? "Both" : "Most") + (team == MatchTeam.SOLO ? " players " : " teams ");
                    CommandUtils.broadcastAlertMessage(prefix + "are now ready! Start countdown updated to 5 seconds.");
                    StartCountdown.setCountdown(5);
                }
            });
    }

}
