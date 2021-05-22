package net.purelic.cgm.commands.communication;

import cloud.commandframework.Command;
import cloud.commandframework.arguments.standard.StringArgument;
import cloud.commandframework.bukkit.BukkitCommandManager;
import net.purelic.cgm.events.modules.ChatEvent;
import net.purelic.commons.commands.parsers.CustomCommand;
import net.purelic.commons.Commons;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class GlobalCommand implements CustomCommand {

    @Override
    public Command.Builder<CommandSender> getCommandBuilder(BukkitCommandManager<CommandSender> mgr) {
        return mgr.commandBuilder("global", "g", "shout")
                .senderType(Player.class)
                .argument(StringArgument.greedy("message"))
                .handler(c -> {
                    Player player = (Player) c.getSender();
                    String message = c.get("message");
                    Commons.callEvent(new ChatEvent(player, message, true));
                });
    }

}
