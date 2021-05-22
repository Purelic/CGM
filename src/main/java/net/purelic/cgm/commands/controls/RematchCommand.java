package net.purelic.cgm.commands.controls;

import cloud.commandframework.Command;
import cloud.commandframework.bukkit.BukkitCommandManager;
import net.purelic.cgm.core.managers.MatchManager;
import net.purelic.commons.commands.parsers.CustomCommand;
import net.purelic.commons.commands.parsers.Permission;
import net.purelic.commons.utils.CommandUtils;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class RematchCommand implements CustomCommand {

    @Override
    public Command.Builder<CommandSender> getCommandBuilder(BukkitCommandManager<CommandSender> mgr) {
        return mgr.commandBuilder("rematch")
            .senderType(Player.class)
            .permission(Permission.isMapDev(true))
            .handler(c -> {
                Player player = (Player) c.getSender();

                if (MatchManager.getCurrentMap() == null || MatchManager.getCurrentGameMode() == null) {
                    CommandUtils.sendErrorMessage(player, "There is no match currently playing!");
                    return;
                }

                MatchManager.setNext(MatchManager.getCurrentMap(), MatchManager.getCurrentGameMode());
            });
    }

}
