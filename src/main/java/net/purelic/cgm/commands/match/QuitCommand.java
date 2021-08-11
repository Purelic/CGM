package net.purelic.cgm.commands.match;

import cloud.commandframework.Command;
import cloud.commandframework.bukkit.BukkitCommandManager;
import net.purelic.cgm.core.constants.MatchState;
import net.purelic.cgm.core.constants.MatchTeam;
import net.purelic.cgm.events.match.MatchQuitEvent;
import net.purelic.cgm.league.LeagueModule;
import net.purelic.commons.Commons;
import net.purelic.commons.commands.parsers.CustomCommand;
import net.purelic.commons.utils.CommandUtils;
import net.purelic.commons.utils.ServerUtils;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class QuitCommand implements CustomCommand {

    @Override
    public Command.Builder<CommandSender> getCommandBuilder(BukkitCommandManager<CommandSender> mgr) {
        return mgr.commandBuilder("quit", "obs")
            .senderType(Player.class)
            .handler(c -> {
                Player player = (Player) c.getSender();

                if (ServerUtils.isRanked() && LeagueModule.get().isPlaying(player)) {
                    CommandUtils.sendErrorMessage(player, "You aren't able to leave matches on ranked servers!");
                    return;
                }

                if (MatchTeam.getTeam(player) == MatchTeam.OBS) {
                    CommandUtils.sendErrorMessage(player, "You aren't currently playing!");
                } else if (MatchState.isActive()) {
                    Commons.callEvent(new MatchQuitEvent(player));
                } else {
                    CommandUtils.sendErrorMessage(player, "You cannot leave the match right now!");
                }
            });
    }

}
