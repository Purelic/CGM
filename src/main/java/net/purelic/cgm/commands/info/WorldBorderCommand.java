package net.purelic.cgm.commands.info;

import cloud.commandframework.Command;
import cloud.commandframework.bukkit.BukkitCommandManager;
import net.purelic.cgm.core.constants.MatchState;
import net.purelic.cgm.core.gamemodes.NumberSetting;
import net.purelic.commons.commands.parsers.CustomCommand;
import net.purelic.commons.utils.CommandUtils;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.WorldBorder;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class WorldBorderCommand implements CustomCommand {

    @Override
    public Command.Builder<CommandSender> getCommandBuilder(BukkitCommandManager<CommandSender> mgr) {
        return mgr.commandBuilder("wb", "border", "worldborder")
            .senderType(Player.class)
            .handler(c -> {
                Player player = (Player) c.getSender();

                if (!MatchState.isState(MatchState.STARTED)) {
                    CommandUtils.sendErrorMessage(player, "You can't use this command right now");
                    return;
                }

                WorldBorder border = player.getWorld().getWorldBorder();

                double size = border.getSize();
                double maxSize = NumberSetting.WB_MAX_SIZE.value();
                double minSize = NumberSetting.WB_MIN_SIZE.value();

                maxSize = maxSize % 2 == 0 ? maxSize + 1 : maxSize;
                minSize = minSize % 2 == 0 ? minSize + 1 : minSize;

                if (size == maxSize || size == minSize) {
                    CommandUtils.sendErrorMessage(player, "The border isn't currently moving");
                    return;
                }

                Location location = player.getLocation();
                int loc = (int) Math.max(Math.abs(location.getX()), Math.abs(location.getZ()));
                int dist = (int) size / 2 - loc;

                CommandUtils.sendAlertMessage(player, "The border is currently " + ChatColor.AQUA + Math.abs(dist) +
                    ChatColor.RESET + " block" + (Math.abs(dist) == 1 ? "" : "s") + " away");
            });
    }

}
