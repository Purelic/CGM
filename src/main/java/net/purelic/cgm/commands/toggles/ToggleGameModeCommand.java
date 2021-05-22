package net.purelic.cgm.commands.toggles;

import cloud.commandframework.Command;
import cloud.commandframework.bukkit.BukkitCommandManager;
import net.purelic.cgm.core.constants.MatchState;
import net.purelic.cgm.utils.PlayerUtils;
import net.purelic.commons.commands.parsers.CustomCommand;
import net.purelic.commons.utils.CommandUtils;
import org.bukkit.GameMode;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ToggleGameModeCommand implements CustomCommand {

    @Override
    public Command.Builder<CommandSender> getCommandBuilder(BukkitCommandManager<CommandSender> mgr) {
        return mgr.commandBuilder("toggle")
                .literal("gamemode", "gm")
                .senderType(Player.class)
                .handler(c -> {
                    Player player = (Player) c.getSender();

                    if (MatchState.isState(MatchState.WAITING, MatchState.VOTING)) {
                        CommandUtils.sendErrorMessage(player, "You can't toggle your game mode right now!");
                        return;
                    }

                    if (!PlayerUtils.isObserving(player)) {
                        CommandUtils.sendErrorMessage(player, "You can't toggle your game mode while playing!");
                        return;
                    }

                    if (player.getGameMode() == GameMode.SPECTATOR) {
                        player.setGameMode(GameMode.ADVENTURE);
                        player.setAllowFlight(true);
                        player.setFlying(true);
                    } else {
                        player.setGameMode(GameMode.SPECTATOR);
                    }
                });
    }

}
