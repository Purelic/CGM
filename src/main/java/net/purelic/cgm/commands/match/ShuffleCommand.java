package net.purelic.cgm.commands.match;

import cloud.commandframework.Command;
import cloud.commandframework.bukkit.BukkitCommandManager;
import net.purelic.cgm.core.constants.MatchState;
import net.purelic.cgm.core.constants.MatchTeam;
import net.purelic.commons.commands.parsers.CustomCommand;
import net.purelic.commons.commands.parsers.Permission;
import net.purelic.commons.utils.CommandUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ShuffleCommand implements CustomCommand {

    @Override
    public Command.Builder<CommandSender> getCommandBuilder(BukkitCommandManager<CommandSender> mgr) {
        return mgr.commandBuilder("shuffle")
            .senderType(Player.class)
            .permission(Permission.isMapDev(true))
            .handler(c -> {
                Player player = (Player) c.getSender();

                if (!MatchState.isState(MatchState.STARTING, MatchState.PRE_GAME)) {
                    CommandUtils.sendErrorMessage(player, "You can only use this command before the match starts!");
                    return;
                }

                Bukkit.getOnlinePlayers().stream().filter(pl -> MatchTeam.getTeam(pl) != MatchTeam.OBS).forEach(pl -> pl.performCommand("obs"));

                List<Player> shuffled = new ArrayList<>(Bukkit.getOnlinePlayers());
                Collections.shuffle(shuffled);

                shuffled.forEach(pl -> pl.performCommand("join"));
            });
    }

}
