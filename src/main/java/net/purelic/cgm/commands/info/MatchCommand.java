package net.purelic.cgm.commands.info;

import cloud.commandframework.Command;
import cloud.commandframework.bukkit.BukkitCommandManager;
import net.md_5.bungee.api.ChatColor;
import net.purelic.cgm.core.constants.MatchState;
import net.purelic.cgm.core.gamemodes.CustomGameMode;
import net.purelic.cgm.core.gamemodes.EnumSetting;
import net.purelic.cgm.core.gamemodes.NumberSetting;
import net.purelic.cgm.core.gamemodes.ToggleSetting;
import net.purelic.cgm.core.gamemodes.constants.TeamType;
import net.purelic.cgm.core.managers.MatchManager;
import net.purelic.cgm.core.managers.TabManager;
import net.purelic.cgm.core.maps.CustomMap;
import net.purelic.cgm.utils.MatchUtils;
import net.purelic.commons.commands.parsers.CustomCommand;
import net.purelic.commons.utils.ChatUtils;
import net.purelic.commons.utils.CommandUtils;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class MatchCommand implements CustomCommand {

    @Override
    public Command.Builder<CommandSender> getCommandBuilder(BukkitCommandManager<CommandSender> mgr) {
        return mgr.commandBuilder("match", "howtoplay", "wtfisthisgame")
                .senderType(Player.class)
                .handler(c -> {
                    Player player = (Player) c.getSender();

                    if (MatchState.isActive() || MatchState.isState(MatchState.ENDED)) {
                        CustomMap map = MatchManager.getCurrentMap();
                        CustomGameMode gameMode = MatchManager.getCurrentGameMode();
                        boolean rounds = NumberSetting.ROUNDS.value() > 1;
                        int lives = NumberSetting.LIVES_PER_ROUND.value();
                        TeamType teamType =  EnumSetting.TEAM_TYPE.get();
                        String teamSizes = teamType == TeamType.SOLO ? "" : " (" + teamType.getTeams().size() + " teams of " + MatchUtils.getMaxTeamPlayers() + ")";
                        boolean regen = ToggleSetting.PLAYER_NATURAL_REGEN.isEnabled();

                        player.sendMessage("");
                        player.sendMessage(ChatUtils.getHeader("Match #" + MatchManager.getMatches()));
                        player.sendMessage(ChatColor.ITALIC + gameMode.getDescription());
                        player.sendMessage("");
                        player.sendMessage(ChatColor.GRAY + " • " + ChatColor.WHITE + "Map: " + ChatColor.YELLOW + map.getName());
                        player.sendMessage(ChatColor.GRAY + " • " + ChatColor.WHITE + "Game Mode: " + ChatColor.GOLD + gameMode.getName() + ChatColor.GRAY + " (" + gameMode.getAlias() + ")");
                        player.sendMessage(ChatColor.GRAY + " • " + ChatColor.WHITE + "Game Type: " + ChatColor.AQUA + gameMode.getGameType().getName());
                        // player.sendMessage(ChatColor.GRAY + " • " + ChatColor.WHITE + "Team Type: " + ChatColor.AQUA + teamType.getName() + ChatColor.GRAY + teamSizes);
                        player.sendMessage(ChatColor.GRAY + " • " + ChatColor.WHITE + "Objective: " + MatchUtils.getObjectiveString());
                        player.sendMessage(ChatColor.GRAY + " • " + ChatColor.WHITE + "Time Limit: " + ChatColor.AQUA + NumberSetting.TIME_LIMIT.value() + "m" + (rounds ? ChatColor.WHITE + " per round" : ""));
                        if (rounds) player.sendMessage(ChatColor.GRAY + " • " + TabManager.getRounds(false));
                        if (lives > 0) player.sendMessage(ChatColor.GRAY + " • " + ChatColor.WHITE + "Lives: " + ChatColor.AQUA + lives + (rounds ? ChatColor.WHITE + " per round" : ""));
                        if (!regen) player.sendMessage(ChatColor.GRAY + " • " + ChatColor.WHITE + "Natural Regen: " + ChatColor.AQUA + "Off");
                        player.sendMessage("");
                    } else {
                        CommandUtils.sendErrorMessage(player, "You can't view the match info right now!");
                    }
                });
    }

}
