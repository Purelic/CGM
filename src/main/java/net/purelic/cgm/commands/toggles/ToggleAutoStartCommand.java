package net.purelic.cgm.commands.toggles;

import cloud.commandframework.Command;
import cloud.commandframework.bukkit.BukkitCommandManager;
import net.purelic.commons.commands.parsers.CustomCommand;
import net.purelic.commons.commands.parsers.Permission;
import net.purelic.commons.utils.CommandUtils;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ToggleAutoStartCommand implements CustomCommand {

    public static boolean autostart = true;

    @Override
    public Command.Builder<CommandSender> getCommandBuilder(BukkitCommandManager<CommandSender> mgr) {
        return mgr.commandBuilder("toggle")
            .literal("autostart")
            .senderType(Player.class)
            .permission(Permission.isMapDev(true))
            .handler(c -> {
                autostart = !autostart;
                CommandUtils.broadcastAlertMessage("Auto-Start is now " + TogglesCommand.getStatus(autostart));
            });
    }

}
