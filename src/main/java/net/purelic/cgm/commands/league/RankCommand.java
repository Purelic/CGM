package net.purelic.cgm.commands.league;

import cloud.commandframework.Command;
import cloud.commandframework.bukkit.BukkitCommandManager;
import net.purelic.commons.Commons;
import net.purelic.commons.commands.parsers.CustomCommand;
import net.purelic.commons.commands.parsers.PlayerArgument;
import net.purelic.commons.profile.Profile;
import net.purelic.commons.profile.Rank;
import net.purelic.commons.utils.CommandUtils;
import net.purelic.commons.utils.NickUtils;
import net.purelic.commons.utils.ServerUtils;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Optional;

public class RankCommand implements CustomCommand {

    @Override
    public Command.Builder<CommandSender> getCommandBuilder(BukkitCommandManager<CommandSender> mgr) {
        return mgr.commandBuilder("rank", "rating")
                .senderType(Player.class)
                .argument(PlayerArgument.optional("player"))
                .handler(c -> {
                    Player player = (Player) c.getSender();
                    Optional<Player> targetArg = c.getOptional("player");

                    if (!ServerUtils.isRanked()) {
                        CommandUtils.sendErrorMessage(player, "You can only use this command on ranked servers!");
                        return;
                    }

                    Player target = targetArg.orElse(player);
                    Profile profile = Commons.getProfile(target);
                    Rank rank = profile.getLeagueRank();
                    CommandUtils.sendAlertMessage(player, NickUtils.getDisplayName(target) + " is currently " + rank.getFlair() + ChatColor.RESET + " " + rank.getName(false) + ChatColor.GRAY + " (" + profile.getRating() + ")");
                });
    }

}
