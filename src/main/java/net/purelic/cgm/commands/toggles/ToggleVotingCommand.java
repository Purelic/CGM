package net.purelic.cgm.commands.toggles;

import cloud.commandframework.Command;
import cloud.commandframework.bukkit.BukkitCommandManager;
import net.purelic.commons.commands.parsers.CustomCommand;
import net.purelic.commons.commands.parsers.Permission;
import net.purelic.commons.utils.CommandUtils;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ToggleVotingCommand implements CustomCommand {

    public static boolean voting = true;

    @Override
    public Command.Builder<CommandSender> getCommandBuilder(BukkitCommandManager<CommandSender> mgr) {
        return mgr.commandBuilder("toggle")
            .literal("voting")
            .senderType(Player.class)
            .permission(Permission.isMapDev(true))
            .handler(c -> {
                voting = !voting;
                CommandUtils.broadcastAlertMessage("Voting is now " + TogglesCommand.getStatus(voting));
            });
    }

}
