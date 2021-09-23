package net.purelic.cgm.commands.match;

import cloud.commandframework.Command;
import cloud.commandframework.arguments.standard.StringArgument;
import cloud.commandframework.bukkit.BukkitCommandManager;
import net.purelic.cgm.core.constants.MatchState;
import net.purelic.cgm.core.constants.MatchTeam;
import net.purelic.cgm.core.gamemodes.EnumSetting;
import net.purelic.cgm.core.gamemodes.NumberSetting;
import net.purelic.cgm.core.gamemodes.ToggleSetting;
import net.purelic.cgm.core.gamemodes.constants.GameType;
import net.purelic.cgm.core.gamemodes.constants.TeamType;
import net.purelic.cgm.core.managers.MatchManager;
import net.purelic.cgm.events.match.MatchJoinEvent;
import net.purelic.cgm.events.match.MatchQuitEvent;
import net.purelic.cgm.listeners.modules.GracePeriodModule;
import net.purelic.cgm.utils.BedUtils;
import net.purelic.cgm.utils.MatchUtils;
import net.purelic.commons.Commons;
import net.purelic.commons.commands.parsers.CustomCommand;
import net.purelic.commons.commands.parsers.Permission;
import net.purelic.commons.commands.parsers.PlayerArgument;
import net.purelic.commons.utils.CommandUtils;
import net.purelic.commons.utils.NickUtils;
import net.purelic.commons.utils.ServerUtils;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Optional;

public class ForceCommand implements CustomCommand {

    @Override
    public Command.Builder<CommandSender> getCommandBuilder(BukkitCommandManager<CommandSender> mgr) {
        return mgr.commandBuilder("force")
            .senderType(Player.class)
            .permission(Permission.isMapDev(true))
            .argument(PlayerArgument.of("player"))
            .argument(StringArgument.optional("team", StringArgument.StringMode.GREEDY))
            .handler(c -> {
                Player sender = (Player) c.getSender();
                Player p = c.get("player");
                Optional<String> teamArg = c.getOptional("team");

                // Don't allow players to join when there's no active match
                if (!MatchState.isActive()) {
                    CommandUtils.sendErrorMessage(sender, "The match cannot be joined right now!");
                    return;
                }

                // Don't allow players to join ranked matches if they aren't participating
                if (ServerUtils.isRanked()) {
                    CommandUtils.sendErrorMessage(sender, "You can't force join players in ranked matches!");
                    return;
                }

                // Don't allow joining the last round of elimination game modes
                if (MatchUtils.isElimination()
                    && MatchState.isState(MatchState.STARTED)
                    && MatchManager.getRound() == NumberSetting.ROUNDS.value() // last round
                    && !EnumSetting.GAME_TYPE.is(GameType.BED_WARS)
                    && !GracePeriodModule.isActive()) {
                    CommandUtils.sendErrorMessage(sender, "It's too late to force a player to join this elimination match!");
                    return;
                }

                // Start logic for picking the team to join
                MatchTeam currentTeam = MatchTeam.getTeam(p);
                MatchTeam teamToJoin = null;

                // Player provides the team they want to join
                if (teamArg.isPresent()) {
                    String teamStr = teamArg.get();

                    // Attempt to get the team if they specified one
                    teamToJoin = MatchTeam.getTeamFromString(teamStr);

                    if (teamToJoin == null) {
                        // Could not find the team they specified
                        CommandUtils.sendNotFoundMessage(sender, "team", teamStr);
                        return;
                    }

                    if (teamToJoin == MatchTeam.OBS) {
                        if (currentTeam == MatchTeam.OBS) {
                            CommandUtils.sendErrorMessage(sender, "That player is already spectating!");
                        } else {
                            Commons.callEvent(new MatchQuitEvent(p));
                        }

                        return;
                    }

                    if (currentTeam != MatchTeam.OBS) {
                        CommandUtils.sendErrorMessage(sender, "They're already on a team! Please force them to Spectators first.");
                        return;
                    }
                }

                TeamType teamType = EnumSetting.TEAM_TYPE.get();
                MatchTeam allowedTeam = MatchTeam.getAllowedTeam(p);

                if (teamToJoin != null) {
                    if (!teamType.getTeams().contains(teamToJoin)) {
                        CommandUtils.sendErrorMessage(sender, teamToJoin.getName() + " is not a valid team for this game mode!");
                        return;
                    }
                } else {
                    if (currentTeam != MatchTeam.OBS) {
                        CommandUtils.sendErrorMessage(sender, "They're already on a team! Please force them to Spectators first.");
                        return;
                    } else {
                        if (!ToggleSetting.TEAM_SWITCHING.isEnabled() && !MatchState.isState(MatchState.PRE_GAME, MatchState.STARTING)) {
                            teamToJoin = allowedTeam;
                        }

                        if (teamToJoin == null) { // team switching either enabled or they haven't been assigned a team yet
                            teamToJoin = MatchTeam.getSmallestTeam(teamType, true);
                        }
                    }
                }

                if (teamToJoin == currentTeam) {
                    CommandUtils.sendErrorMessage(sender, "They've already joined that team!");
                    return;
                }

                if (!ToggleSetting.TEAM_SWITCHING.isEnabled() && !MatchState.isState(MatchState.PRE_GAME, MatchState.STARTING)) {
                    if (allowedTeam != null && teamToJoin != allowedTeam) {
                        CommandUtils.sendErrorMessage(sender, "This game mode doesn't allow switching teams. You can only force join them to " + allowedTeam.getColoredName());
                        return;
                    }
                }

                if (EnumSetting.GAME_TYPE.is(GameType.BED_WARS) && BedUtils.isBedDestroyed(teamToJoin)) {
                    CommandUtils.sendErrorMessage(sender, "They can't join this team right now - their bed has been destroyed!");
                    return;
                }

                CommandUtils.sendSuccessMessage(sender, "You forced " + NickUtils.getNick(p) + " to join " + teamToJoin.getName() + "!");
                CommandUtils.sendAlertMessage(p, NickUtils.getNick(sender) + " forced you to join " + teamToJoin.getColoredName());
                Commons.callEvent(new MatchJoinEvent(p, teamToJoin, true, allowedTeam == null));
            });
    }

}
