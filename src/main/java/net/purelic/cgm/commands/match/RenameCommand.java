package net.purelic.cgm.commands.match;

import cloud.commandframework.Command;
import cloud.commandframework.arguments.standard.EnumArgument;
import cloud.commandframework.arguments.standard.StringArgument;
import cloud.commandframework.bukkit.BukkitCommandManager;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.TextComponent;
import net.purelic.cgm.core.constants.MatchState;
import net.purelic.cgm.core.constants.MatchTeam;
import net.purelic.cgm.core.gamemodes.EnumSetting;
import net.purelic.cgm.core.gamemodes.constants.TeamType;
import net.purelic.cgm.core.managers.ScoreboardManager;
import net.purelic.cgm.core.managers.TabManager;
import net.purelic.commons.commands.parsers.CustomCommand;
import net.purelic.commons.commands.parsers.Permission;
import net.purelic.commons.utils.CommandUtils;
import net.purelic.commons.utils.Fetcher;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.regex.Pattern;

public class RenameCommand implements CustomCommand {

    private static final String REGEX = "^[a-zA-Z0-9 -]+$";
    private static final Pattern PATTERN = Pattern.compile(REGEX);

    @Override
    public Command.Builder<CommandSender> getCommandBuilder(BukkitCommandManager<CommandSender> mgr) {
        return mgr.commandBuilder("rename")
            .senderType(Player.class)
            .permission(Permission.isMapDev(true))
            .argument(EnumArgument.of(MatchTeam.class, "team"))
            .argument(StringArgument.greedy("name"))
            .handler(c -> {
                Player player = (Player) c.getSender();
                MatchTeam team = c.get("team");
                String name = c.get("name");

                if (!MatchState.isActive()) {
                    CommandUtils.sendErrorMessage(player, "You can't change team names right now!");
                    return;
                }

                TeamType teamType = EnumSetting.TEAM_TYPE.get();

                if (!teamType.getTeams().contains(team)) {
                    CommandUtils.sendErrorMessage(player, "That is not a valid team for this game mode!");
                    return;
                }

                if (name.length() > 16) {
                    CommandUtils.sendErrorMessage(player, "Team names can't be more than 16 letters!");
                    return;
                }

                if (!PATTERN.matcher(name).matches()) {
                    CommandUtils.sendErrorMessage(player, "Team names can only contain alphanumeric values!");
                    return;
                }

                team.setName(name);
                ScoreboardManager.updateTeamBoard();
                TabManager.updateTeam(team, true);

                CommandUtils.broadcastAlertMessage(
                    Fetcher.getFancyName(player),
                    new TextComponent(" renamed " + team.getColor() + team.getDefaultName() + ChatColor.RESET + " to " + team.getColoredName())
                );
            });
    }

}
