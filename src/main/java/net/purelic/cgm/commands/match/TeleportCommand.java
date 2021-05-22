package net.purelic.cgm.commands.match;

import cloud.commandframework.Command;
import cloud.commandframework.bukkit.BukkitCommandManager;
import net.purelic.cgm.core.constants.MatchState;
import net.purelic.cgm.core.managers.MatchManager;
import net.purelic.cgm.core.match.Participant;
import net.purelic.commons.commands.parsers.CustomCommand;
import net.purelic.commons.commands.parsers.PlayerArgument;
import net.purelic.commons.utils.CommandUtils;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class TeleportCommand implements CustomCommand {

    @Override
    public Command.Builder<CommandSender> getCommandBuilder(BukkitCommandManager<CommandSender> mgr) {
        return mgr.commandBuilder("tp", "teleport")
                .senderType(Player.class)
                .argument(PlayerArgument.of("player"))
                .handler(c -> {
                    Player player = (Player) c.getSender();
                    Player target = c.get("player");

                    if (!(MatchState.isActive() || MatchState.isState(MatchState.ENDED))) {
                        CommandUtils.sendErrorMessage(player, "You cannot teleport to other players right now!");
                        return;
                    }

                    if (MatchManager.isPlaying(player)) {
                       Participant participant = MatchManager.getParticipant(player);

                       if (!participant.isEliminated()) {
                           CommandUtils.sendErrorMessage(player, "You cannot teleport to other players right now!");
                           return;
                       }
                    }

                    player.teleport(target);
                });
    }

}
