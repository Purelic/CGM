package net.purelic.cgm.commands.controls;

import cloud.commandframework.Command;
import cloud.commandframework.bukkit.BukkitCommandManager;
import net.md_5.bungee.api.chat.TextComponent;
import net.purelic.cgm.core.constants.MatchState;
import net.purelic.commons.commands.parsers.CustomCommand;
import net.purelic.commons.commands.parsers.Permission;
import net.purelic.commons.utils.ChatUtils;
import net.purelic.commons.utils.CommandUtils;
import net.purelic.commons.utils.Fetcher;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class EndCommand implements CustomCommand {

    @Override
    public Command.Builder<CommandSender> getCommandBuilder(BukkitCommandManager<CommandSender> mgr) {
        return mgr.commandBuilder("end")
            .senderType(Player.class)
            .permission(Permission.isMapDev(true))
            .handler(c -> {
                Player player = (Player) c.getSender();

                if (MatchState.isState(MatchState.STARTED)) {
                    CommandUtils.broadcastAlertMessage(
                        Fetcher.getFancyName(player),
                        new TextComponent(" force ended the match")
                    );
                    MatchState.setState(MatchState.ENDED, true, 10);
                } else {
                    CommandUtils.sendErrorMessage(player, "You can't end the game right now!");
                    return;
                }

                ChatUtils.broadcastActionBar("");
            });
    }

}
