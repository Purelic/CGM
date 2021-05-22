package net.purelic.cgm.commands.controls;

import cloud.commandframework.Command;
import cloud.commandframework.arguments.standard.IntegerArgument;
import cloud.commandframework.bukkit.BukkitCommandManager;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.TextComponent;
import net.purelic.cgm.commands.match.SpectateCommand;
import net.purelic.cgm.core.constants.MatchState;
import net.purelic.cgm.core.runnables.CycleCountdown;
import net.purelic.cgm.core.runnables.StartCountdown;
import net.purelic.cgm.core.runnables.VotingCountdown;
import net.purelic.commons.commands.parsers.CustomCommand;
import net.purelic.commons.commands.parsers.Permission;
import net.purelic.commons.utils.CommandUtils;
import net.purelic.commons.utils.Fetcher;
import net.purelic.commons.utils.TaskUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Optional;

public class StartCommand implements CustomCommand {

    @Override
    public Command.Builder<CommandSender> getCommandBuilder(BukkitCommandManager<CommandSender> mgr) {
        return mgr.commandBuilder("start")
            .senderType(Player.class)
            .permission(Permission.isMapDev(true))
            .argument(IntegerArgument.optional("seconds"))
            .handler(c -> {
                Player player = (Player) c.getSender();
                Optional<Integer> secondsArg = c.getOptional("seconds");
                int seconds = secondsArg.map(integer -> Math.max(0, integer)).orElse(15);

                if (TaskUtils.isRunning(CycleCountdown.getCountdown())) {
                    CommandUtils.sendErrorMessage(player, "You can't start a countdown while a map is cycling!");
                } else if (MatchState.isState(MatchState.WAITING)) {
                    MatchState.setState(MatchState.VOTING, true, seconds);
                    CommandUtils.broadcastAlertMessage(
                        Fetcher.getFancyName(player),
                        new TextComponent(" force started the voting countdown "),
                        new ComponentBuilder("(" + seconds + " second" + (seconds == 1 ? "" : "s") + ")")
                            .color(ChatColor.GRAY).create()[0]
                    );
                } else if (MatchState.isState(MatchState.PRE_GAME)) {
                    if (SpectateCommand.SPECTATORS.size() == Bukkit.getOnlinePlayers().size()) {
                        CommandUtils.sendErrorMessage(player, "You can't start the match with no players!");
                        return;
                    }

                    MatchState.setState(MatchState.STARTING, true, seconds);
                    CommandUtils.broadcastAlertMessage(
                        Fetcher.getFancyName(player),
                        new TextComponent(" force started the match countdown "),
                        new ComponentBuilder("(" + seconds + " second" + (seconds == 1 ? "" : "s") + ")")
                            .color(ChatColor.GRAY).create()[0]
                    );
                } else if (MatchState.isState(MatchState.VOTING) && TaskUtils.isRunning(VotingCountdown.getCountdown())) {
                    VotingCountdown.setCountdown(seconds);
                    CommandUtils.broadcastAlertMessage(
                        Fetcher.getFancyName(player),
                        new TextComponent(" updated the voting countdown "),
                        new ComponentBuilder("(" + seconds + " second" + (seconds == 1 ? "" : "s") + ")")
                            .color(ChatColor.GRAY).create()[0]
                    );
                } else if (MatchState.isState(MatchState.STARTING) && TaskUtils.isRunning(StartCountdown.getCountdown())) {
                    if (!secondsArg.isPresent()) {
                        StartCountdown.setCountdown(0);
                        StartCountdown.setForced();
                        CommandUtils.broadcastAlertMessage(
                            Fetcher.getFancyName(player),
                            new TextComponent(" started the match")
                        );
                    } else {
                        StartCountdown.setCountdown(seconds);
                        StartCountdown.setForced();
                        CommandUtils.broadcastAlertMessage(
                            Fetcher.getFancyName(player),
                            new TextComponent(" updated the start countdown "),
                            new ComponentBuilder("(" + seconds + " second" + (seconds == 1 ? "" : "s") + ")")
                                .color(ChatColor.GRAY).create()[0]
                        );
                    }
                } else {
                    CommandUtils.sendErrorMessage(player, "You can't start a countdown right now!");
                }
            });
    }

}
