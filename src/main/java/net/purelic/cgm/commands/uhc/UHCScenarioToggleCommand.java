package net.purelic.cgm.commands.uhc;

import cloud.commandframework.Command;
import cloud.commandframework.arguments.standard.EnumArgument;
import cloud.commandframework.bukkit.BukkitCommandManager;
import net.purelic.cgm.core.constants.MatchState;
import net.purelic.cgm.server.Playlist;
import net.purelic.cgm.uhc.UHCScenario;
import net.purelic.commons.commands.parsers.CustomCommand;
import net.purelic.commons.commands.parsers.Permission;
import net.purelic.commons.utils.CommandUtils;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class UHCScenarioToggleCommand implements CustomCommand {

    @Override
    public Command.Builder<CommandSender> getCommandBuilder(BukkitCommandManager<CommandSender> mgr) {
        return mgr.commandBuilder("scenario")
            .senderType(Player.class)
            .permission(Permission.isMod(true))
            .literal("toggle")
            .argument(EnumArgument.of(UHCScenario.class, "scenario"))
            .handler(c -> {
                Player player = (Player) c.getSender();
                UHCScenario scenario = c.get("scenario");

                if (!Playlist.isUHC()) {
                    CommandUtils.sendErrorMessage(player, "You can only use this command on UHC servers!");
                    return;
                }

                if (MatchState.isState(MatchState.STARTED)) {
                    CommandUtils.sendErrorMessage(player, "You can't change UHC scenarios during a match!");
                    return;
                }

                scenario.toggle();
                CommandUtils.broadcastAlertMessage(ChatColor.AQUA + scenario.getName() + ChatColor.WHITE + " scenario is now " +
                    (scenario.isEnabled() ? ChatColor.GREEN + "enabled" : ChatColor.RED + "disabled"));
            });
    }

}
