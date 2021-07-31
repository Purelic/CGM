package net.purelic.cgm.commands.league;

import cloud.commandframework.Command;
import cloud.commandframework.bukkit.BukkitCommandManager;
import net.md_5.bungee.api.ChatColor;
import net.purelic.cgm.core.constants.MatchState;
import net.purelic.cgm.core.constants.MatchTeam;
import net.purelic.cgm.core.gamemodes.EnumSetting;
import net.purelic.cgm.core.gamemodes.constants.TeamType;
import net.purelic.cgm.core.managers.LeagueManager;
import net.purelic.cgm.core.runnables.StartCountdown;
import net.purelic.commons.commands.parsers.CustomCommand;
import net.purelic.commons.utils.CommandUtils;
import net.purelic.commons.utils.ServerUtils;
import net.purelic.commons.utils.TaskUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.Set;

public class ReadyCommand implements CustomCommand {

    public static final Set<MatchTeam> READY = new HashSet<>();

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

                if (!LeagueManager.isPlaying(player)) {
                    CommandUtils.sendErrorMessage(player, "Spectators can't use this command!");
                    return;
                }

                if (TaskUtils.isRunning(StartCountdown.getCountdown()) && StartCountdown.getSeconds() <= 10) {
                    CommandUtils.sendErrorMessage(player, "The start countdown is already below 10 seconds!");
                    return;
                }

                if (ReadyCommand.READY.contains(team)) {
                    ReadyCommand.READY.remove(team);
                    Bukkit.broadcastMessage(team.getColoredName() + " is no longer ready!");
                } else {
                    ReadyCommand.READY.add(team);
                    Bukkit.broadcastMessage(team.getColoredName() + " is ready! " + ChatColor.GRAY + "(/ready)");
                }

                TeamType teamType = EnumSetting.TEAM_TYPE.get();

                for (MatchTeam matchTeam : teamType.getTeams()) {
                    if (!READY.contains(matchTeam)) {
                        return;
                    }
                }

                CommandUtils.broadcastAlertMessage("All teams are now ready! Start countdown updated to 10 seconds");
                StartCountdown.setCountdown(10);
            });
    }

}
