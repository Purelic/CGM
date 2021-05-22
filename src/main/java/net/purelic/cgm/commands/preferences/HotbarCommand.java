package net.purelic.cgm.commands.preferences;

import cloud.commandframework.Command;
import cloud.commandframework.bukkit.BukkitCommandManager;
import net.purelic.cgm.listeners.modules.HotbarModule;
import net.purelic.commons.commands.parsers.CustomCommand;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class HotbarCommand implements CustomCommand {

    @Override
    public Command.Builder<CommandSender> getCommandBuilder(BukkitCommandManager<CommandSender> mgr) {
        return mgr.commandBuilder("hotbar")
                .senderType(Player.class)
                .handler(c -> {
                    Player player = (Player) c.getSender();
                    HotbarModule.openHotbarMenu(player);
                });
    }

}
