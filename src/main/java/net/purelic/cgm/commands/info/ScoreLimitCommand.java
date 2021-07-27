package net.purelic.cgm.commands.info;

import cloud.commandframework.Command;
import cloud.commandframework.bukkit.BukkitCommandManager;
import net.purelic.cgm.core.constants.MatchState;
import net.purelic.cgm.utils.MatchUtils;
import net.purelic.commons.commands.parsers.CustomCommand;
import net.purelic.commons.utils.CommandUtils;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ScoreLimitCommand implements CustomCommand {

    @Override
    public Command.Builder<CommandSender> getCommandBuilder(BukkitCommandManager<CommandSender> mgr) {
        return mgr.commandBuilder("scorelimit", "sl", "objective")
            .senderType(Player.class)
            .handler(c -> {
                Player player = (Player) c.getSender();

                if (MatchState.isActive() || MatchState.isState(MatchState.ENDED)) {
                    player.sendMessage(MatchUtils.getObjectiveString());
                } else {
                    CommandUtils.sendErrorMessage(player, "You can't view the score limit right now!");
                }
            });
    }

}
