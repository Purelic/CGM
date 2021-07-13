package net.purelic.cgm.commands.info;

import cloud.commandframework.Command;
import cloud.commandframework.bukkit.BukkitCommandManager;
import net.md_5.bungee.api.ChatColor;
import net.purelic.cgm.core.constants.MatchState;
import net.purelic.cgm.core.gamemodes.CustomGameMode;
import net.purelic.cgm.core.gamemodes.NumberSetting;
import net.purelic.cgm.core.gamemodes.ToggleSetting;
import net.purelic.cgm.core.managers.MatchManager;
import net.purelic.cgm.core.maps.CustomMap;
import net.purelic.commons.commands.parsers.CustomCommand;
import net.purelic.commons.utils.ChatUtils;
import net.purelic.commons.utils.CommandUtils;
import net.purelic.commons.utils.Fetcher;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.UUID;

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
                        boolean regen = ToggleSetting.PLAYER_NATURAL_REGEN.isEnabled();

                        List<UUID> authorIds = map.getYaml().getAuthors();
                        String authors = Fetcher.getBasicName(authorIds.get(0));

                        if (authorIds.size() == 2) {
                            authors += " and " + Fetcher.getBasicName(authorIds.get(1));
                        } else if (authorIds.size() > 2) {
                            authors += " and " + authorIds.size() + " others";
                        }

                        player.sendMessage("");
                        player.sendMessage(ChatUtils.getHeader("Match #" + MatchManager.getMatches()));
                        player.sendMessage(ChatColor.ITALIC + gameMode.getDescription());
                        player.sendMessage("");
                        if (!map.getName().equals("UHC")) player.sendMessage(ChatColor.GRAY + " • " + ChatColor.WHITE + "Map: " + map.getColoredName() + " by " + authors);
                        player.sendMessage(ChatColor.GRAY + " • " + ChatColor.WHITE + "Game Mode: " + gameMode.getColoredNameWithAlias());
                        if (lives > 0) player.sendMessage(ChatColor.GRAY + " • " + ChatColor.WHITE + "Lives: " + ChatColor.AQUA + lives + (rounds ? ChatColor.WHITE + " per round" : ""));
                        if (!regen) player.sendMessage(ChatColor.GRAY + " • " + ChatColor.WHITE + "Natural Regen: " + ChatColor.AQUA + "Off");
                        player.sendMessage("");
                    } else {
                        CommandUtils.sendErrorMessage(player, "You can't view the match info right now!");
                    }
                });
    }

}
