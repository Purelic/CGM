package net.purelic.cgm.commands.info;

import cloud.commandframework.Command;
import cloud.commandframework.bukkit.BukkitCommandManager;
import net.md_5.bungee.api.ChatColor;
import net.purelic.cgm.core.constants.MatchState;
import net.purelic.cgm.core.gamemodes.NumberSetting;
import net.purelic.cgm.core.managers.TabManager;
import net.purelic.commons.commands.parsers.CustomCommand;
import net.purelic.commons.utils.CommandUtils;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class RoundsCommand implements CustomCommand {

    @Override
    public Command.Builder<CommandSender> getCommandBuilder(BukkitCommandManager<CommandSender> mgr) {
        return mgr.commandBuilder("rounds")
            .senderType(Player.class)
            .handler(c -> {
                Player player = (Player) c.getSender();

                if (MatchState.isActive() || MatchState.isState(MatchState.ENDED)) {
                    if (NumberSetting.ROUNDS.value() == 1) {
                        CommandUtils.sendAlertMessage(player, "This game mode only has " + ChatColor.AQUA + "1" + ChatColor.RESET + " round");
                    } else {
                        player.sendMessage(TabManager.getRounds(false));
                    }
                } else {
                    CommandUtils.sendErrorMessage(player, "You can't view the round status right now!");
                }
            });
    }

}
