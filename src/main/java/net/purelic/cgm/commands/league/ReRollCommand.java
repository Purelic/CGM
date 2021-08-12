package net.purelic.cgm.commands.league;

import cloud.commandframework.Command;
import cloud.commandframework.bukkit.BukkitCommandManager;
import net.md_5.bungee.api.ChatColor;
import net.purelic.cgm.core.constants.MatchState;
import net.purelic.cgm.core.constants.MatchTeam;
import net.purelic.cgm.core.runnables.StartCountdown;
import net.purelic.cgm.league.LeagueModule;
import net.purelic.commons.commands.parsers.CustomCommand;
import net.purelic.commons.utils.CommandUtils;
import net.purelic.commons.utils.NickUtils;
import net.purelic.commons.utils.ServerUtils;
import net.purelic.commons.utils.TaskUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class ReRollCommand implements CustomCommand {

    public static final List<MatchTeam> TEAMS_VOTED = new ArrayList<>();
    public static final List<Player> PLAYERS_VOTED = new ArrayList<>();
    public static boolean REROLLED = false;

    @Override
    public Command.Builder<CommandSender> getCommandBuilder(BukkitCommandManager<CommandSender> mgr) {
        return mgr.commandBuilder("reroll", "rr")
            .senderType(Player.class)
            .handler(c -> {
                Player player = (Player) c.getSender();
                MatchTeam team = MatchTeam.getTeam(player);

                if (!ServerUtils.isRanked()) {
                    CommandUtils.sendErrorMessage(player, "This command is only available on ranked servers!");
                    return;
                }

                if (!MatchState.isState(MatchState.STARTING)) {
                    CommandUtils.sendErrorMessage(player, "You can't use this command right now!");
                    return;
                }

                if (!LeagueModule.get().isPlaying(player)) {
                    CommandUtils.sendErrorMessage(player, "Spectators can't use this command!");
                    return;
                }

                if (REROLLED) {
                    CommandUtils.sendErrorMessage(player, "The match can only be re-rolled once!");
                    return;
                }

                if (TaskUtils.isRunning(StartCountdown.getCountdown()) && StartCountdown.getSeconds() <= 5) {
                    CommandUtils.sendErrorMessage(player, "It's too late to vote for a re-roll!");
                    return;
                }

                int totalPlaces = LeagueModule.get().getTotalPlaces();
                int majority = totalPlaces == 2 ? 2 : totalPlaces / 2;
                boolean reroll;

                if (team == MatchTeam.SOLO) {
                    if (PLAYERS_VOTED.contains(player)) {
                        CommandUtils.sendErrorMessage(player, "You've already voted to re-roll the map!");
                    } else {
                        PLAYERS_VOTED.add(player);
                        String progress = ChatColor.GRAY + " (" + PLAYERS_VOTED.size() + "/" + majority + ")";
                        Bukkit.broadcastMessage(NickUtils.getDisplayName(player) + " voted to /reroll the map!" + progress);
                    }

                    reroll = PLAYERS_VOTED.size() >= majority;
                } else {
                    if (TEAMS_VOTED.contains(team)) {
                        CommandUtils.sendErrorMessage(player, "Your team has already voted to re-roll the map!");
                    } else {
                        TEAMS_VOTED.add(team);
                        String progress = ChatColor.GRAY + " (" + TEAMS_VOTED.size() + "/" + majority + ")";
                        Bukkit.broadcastMessage(team.getColoredName() + " voted to /reroll the map!" + progress);
                    }

                    reroll = TEAMS_VOTED.size() >= majority;
                }

                if (reroll) {
                    REROLLED = true;
                    String prefix = (totalPlaces == 2 ? "Both" : "Most") + (team == MatchTeam.SOLO ? " players " : " teams ");
                    CommandUtils.broadcastAlertMessage(prefix + "voted to re-roll! Cycling to a new match...");
                    LeagueModule.get().cycleRandom();
                }
            });
    }

}
