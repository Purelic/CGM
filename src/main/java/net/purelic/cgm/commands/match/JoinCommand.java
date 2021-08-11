package net.purelic.cgm.commands.match;

import cloud.commandframework.Command;
import cloud.commandframework.arguments.standard.StringArgument;
import cloud.commandframework.bukkit.BukkitCommandManager;
import net.purelic.cgm.core.constants.JoinState;
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
import net.purelic.cgm.league.LeagueModule;
import net.purelic.cgm.listeners.modules.GracePeriodModule;
import net.purelic.cgm.utils.BedUtils;
import net.purelic.cgm.utils.MatchUtils;
import net.purelic.commons.Commons;
import net.purelic.commons.commands.parsers.CustomCommand;
import net.purelic.commons.commands.parsers.Permission;
import net.purelic.commons.utils.CommandUtils;
import net.purelic.commons.utils.ServerUtils;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Optional;

public class JoinCommand implements CustomCommand {

    @Override
    public Command.Builder<CommandSender> getCommandBuilder(BukkitCommandManager<CommandSender> mgr) {
        return mgr.commandBuilder("join", "play")
            .senderType(Player.class)
            .argument(StringArgument.optional("team", StringArgument.StringMode.GREEDY))
            .handler(c -> {
                Player player = (Player) c.getSender();
                Optional<String> teamArg = c.getOptional("team");

                // Don't allow players to join when there's no active match
                if (!MatchState.isActive()) {
                    CommandUtils.sendErrorMessage(player, "You cannot join the match right now!");
                    return;
                }

                // Don't allow players to join ranked matches if they aren't participating
                if (ServerUtils.isRanked() && !LeagueModule.get().isPlaying(player)) {
                    CommandUtils.sendErrorMessage(player, "You can only spectate this match!");
                    return;
                }

                // Don't allow joining the last round of elimination game modes
                if (MatchUtils.isElimination()
                    && MatchState.isState(MatchState.STARTED)
                    && MatchManager.getRound() == NumberSetting.ROUNDS.value() // last round
                    && !EnumSetting.GAME_TYPE.is(GameType.BED_WARS)
                    && !GracePeriodModule.isActive()) {
                    CommandUtils.sendErrorMessage(player, "It's too late to join this elimination match!");
                    return;
                }

                // Disallow joining when not a donor and auto-joining is on/join state is locked
                if (JoinState.isState(JoinState.LOCKED) && !Commons.getProfile(player).isDonator()) {
                    CommandUtils.sendAlertMessage(player, "You will automatically join when the match starts");
                    return;
                }

                // Start logic for picking the team to join
                MatchTeam currentTeam = MatchTeam.getTeam(player);
                MatchTeam teamToJoin = null;

                // Player provides the team they want to join
                if (teamArg.isPresent()) {
                    String teamStr = teamArg.get();

                    if (!ServerUtils.isPrivate()
                        && !ServerUtils.isRanked()
                        && Permission.notPremium(c, "Only premium players can pick their teams!")) {
                        return;
                    }

                    // Attempt to get the team if they specified one
                    teamToJoin = MatchTeam.getTeamFromString(teamStr);

                    if (teamToJoin == null) {
                        // Could not find the team they specified
                        CommandUtils.sendNotFoundMessage(player, "team", teamStr);
                        return;
                    }

                    if (teamToJoin == MatchTeam.OBS) {
                        if (currentTeam == MatchTeam.OBS) {
                            CommandUtils.sendErrorMessage(player, "You're already spectating!");
                        } else {
                            Commons.callEvent(new MatchQuitEvent(player));
                        }

                        return;
                    }

                    if (currentTeam != MatchTeam.OBS) {
                        CommandUtils.sendErrorMessage(player, "You've already joined a team! Use /quit to leave");
                        return;
                    }
                }

                TeamType teamType = EnumSetting.TEAM_TYPE.get();
                MatchTeam allowedTeam = MatchTeam.getAllowedTeam(player);

                if (teamToJoin != null) {
                    if (!teamType.getTeams().contains(teamToJoin)) {
                        CommandUtils.sendErrorMessage(player, teamToJoin.getName() + " is not a valid team for this game mode!");
                        return;
                    }
                } else {
                    if (currentTeam != MatchTeam.OBS) {
                        CommandUtils.sendErrorMessage(player, "You're already playing! Use /quit to leave");
                        return;
                    } else {
                        if (!ToggleSetting.TEAM_SWITCHING.isEnabled() && !MatchState.isState(MatchState.PRE_GAME, MatchState.STARTING)) {
                            teamToJoin = allowedTeam;
                        }

                        if (teamToJoin == null) { // team switching either enabled or they haven't been assigned a team yet
                            teamToJoin = MatchTeam.getSmallestTeam(teamType, false);
                        }
                    }
                }

                if (teamToJoin == null) {
                    CommandUtils.sendErrorMessage(player, "All teams are currently full!");
                    return;
                }

                if (teamToJoin == currentTeam) {
                    CommandUtils.sendErrorMessage(player, "You've already joined this team!");
                    return;
                }

                if (!ToggleSetting.TEAM_SWITCHING.isEnabled() && !MatchState.isState(MatchState.PRE_GAME, MatchState.STARTING)) {
                    if (allowedTeam != null && teamToJoin != allowedTeam) {
                        CommandUtils.sendErrorMessage(player, "This game mode doesn't allow switching teams. You can only join " + allowedTeam.getColoredName());
                        return;
                    }
                }

                if (ToggleSetting.TEAM_SWITCHING.isEnabled() || allowedTeam == null) {
                    if (!JoinState.isState(JoinState.PARTY_PRIORITY) && teamToJoin.isStacked(teamType, currentTeam) && !ServerUtils.isRanked()) {
                        CommandUtils.sendErrorMessage(player, "You cannot stack this team!");
                        return;
                    }
                }

                if (EnumSetting.GAME_TYPE.is(GameType.BED_WARS) && BedUtils.isBedDestroyed(teamToJoin)) {
                    CommandUtils.sendErrorMessage(player, "You can't join this team right now - their bed has been destroyed!");
                    return;
                }

                if (teamToJoin.isFull()) {
                    CommandUtils.sendErrorMessage(player, "This team is currently full!");
                    return;
                }

                Commons.callEvent(new MatchJoinEvent(player, teamToJoin, false, allowedTeam == null));
            });
    }

}
