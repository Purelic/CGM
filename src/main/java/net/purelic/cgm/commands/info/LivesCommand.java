package net.purelic.cgm.commands.info;

import cloud.commandframework.Command;
import cloud.commandframework.bukkit.BukkitCommandManager;
import net.purelic.cgm.core.gamemodes.NumberSetting;
import net.purelic.cgm.core.managers.MatchManager;
import net.purelic.cgm.core.match.Participant;
import net.purelic.commons.commands.parsers.CustomCommand;
import net.purelic.commons.commands.parsers.PlayerArgument;
import net.purelic.commons.utils.CommandUtils;
import net.purelic.commons.utils.NickUtils;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

public class LivesCommand implements CustomCommand {

    public static final Set<Player> SPECTATORS = new HashSet<>();

    @Override
    public Command.Builder<CommandSender> getCommandBuilder(BukkitCommandManager<CommandSender> mgr) {
        return mgr.commandBuilder("lives")
            .senderType(Player.class)
            .argument(PlayerArgument.optional("player"))
            .handler(c -> {
                Player sender = (Player) c.getSender();
                Optional<Player> playerArg = c.getOptional("player");

                if (playerArg.isPresent()) {
                    Player player = playerArg.get();

                    if (!MatchManager.isPlaying(player)) {
                        CommandUtils.sendErrorMessage(sender, NickUtils.getDisplayName(player) + " is not currently playing!");
                        return;
                    }

                    if (NumberSetting.LIVES_PER_ROUND.value() == 0) {
                        CommandUtils.sendAlertMessage(sender, "This game mode does not have limited lives");
                        return;
                    }

                    Participant participant = MatchManager.getParticipant(player);
                    int lives = participant.getLives();

                    CommandUtils.sendAlertMessage(
                        sender,
                        NickUtils.getDisplayName(player) + " has " + ChatColor.AQUA + lives + ChatColor.RESET + " " + (lives == 1 ? "life" : "lives") + " remaining");
                } else {
                    if (!MatchManager.isPlaying(sender)) {
                        CommandUtils.sendErrorMessage(sender, "You are not currently playing!");
                        return;
                    }

                    if (NumberSetting.LIVES_PER_ROUND.value() == 0) {
                        CommandUtils.sendAlertMessage(sender, "This game mode does not have limited lives");
                        return;
                    }

                    Participant participant = MatchManager.getParticipant(sender);
                    int lives = participant.getLives();

                    CommandUtils.sendAlertMessage(
                        sender,
                        "You have " + ChatColor.AQUA + lives + ChatColor.RESET + " " + (lives == 1 ? "life" : "lives") + " remaining");
                }
            });
    }

}
