package net.purelic.cgm.commands.match;

import cloud.commandframework.Command;
import cloud.commandframework.bukkit.BukkitCommandManager;
import net.purelic.commons.commands.parsers.CustomCommand;
import net.purelic.commons.utils.CommandUtils;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.Set;

public class SpectateCommand implements CustomCommand {

    public static final Set<Player> SPECTATORS = new HashSet<>();

    @Override
    public Command.Builder<CommandSender> getCommandBuilder(BukkitCommandManager<CommandSender> mgr) {
        return mgr.commandBuilder("spectate", "spec")
            .senderType(Player.class)
            .handler(c -> {
                Player player = (Player) c.getSender();

                if (SpectateCommand.SPECTATORS.contains(player)) {
                    SpectateCommand.SPECTATORS.remove(player);
                    CommandUtils.sendSuccessMessage(player, "You will now auto-join matches when they start!");
                } else {
                    SpectateCommand.SPECTATORS.add(player);
                    CommandUtils.sendSuccessMessage(player, "You will no longer auto-join matches when they start!");
                }
            });
    }

}
