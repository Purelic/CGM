package net.purelic.cgm.commands.league;

import cloud.commandframework.Command;
import cloud.commandframework.bukkit.BukkitCommandManager;
import net.md_5.bungee.api.ChatColor;
import net.purelic.cgm.core.constants.MatchTeam;
import net.purelic.cgm.core.gamemodes.EnumSetting;
import net.purelic.cgm.core.gamemodes.constants.TeamType;
import net.purelic.cgm.core.managers.LeagueManager;
import net.purelic.commons.Commons;
import net.purelic.commons.commands.parsers.CustomCommand;
import net.purelic.commons.utils.CommandUtils;
import net.purelic.commons.utils.ServerUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.Set;

public class NoSpectatorsCommand implements CustomCommand {

    public static final Set<MatchTeam> VOTED = new HashSet<>();

    @Override
    public Command.Builder<CommandSender> getCommandBuilder(BukkitCommandManager<CommandSender> mgr) {
        return mgr.commandBuilder("nospectators")
            .senderType(Player.class)
            .handler(c -> {
                Player player = (Player) c.getSender();
                MatchTeam team = MatchTeam.getTeam(player);

                if (!ServerUtils.isRanked()) {
                    CommandUtils.sendErrorMessage(player, "This command is only available on ranked servers!");
                    return;
                }

                if (!LeagueManager.isPlaying(player)) {
                    CommandUtils.sendErrorMessage(player, "Spectators can't use this command!");
                    return;
                }

                if (VOTED.contains(team)) {
                    CommandUtils.sendErrorMessage(player, "Your team can only vote to kick spectators once!");
                    return;
                }

                VOTED.add(team);
                Bukkit.broadcastMessage(team.getColoredName() + " has voted to kick spectators for this match! " + ChatColor.GRAY + "(/nospectators)");

                TeamType teamType = EnumSetting.TEAM_TYPE.get();

                for (MatchTeam matchTeam : teamType.getTeams()) {
                    if (!VOTED.contains(matchTeam)) {
                        return;
                    }
                }

                CommandUtils.broadcastAlertMessage("All teams voted to kick spectators! Removing spectators and turning on whitelist...");
                ServerUtils.setWhitelisted(true);
                LeagueManager.getPlayers().keySet().forEach(uuid -> Bukkit.getOfflinePlayer(uuid).setWhitelisted(true));
                Bukkit.getOnlinePlayers().forEach(pl -> {
                    if (!pl.isWhitelisted() && !Commons.getProfile(pl).isStaff()) {
                        pl.kickPlayer("This server is now whitelisted!");
                    }
                });
            });
    }

}
