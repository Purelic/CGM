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
import net.purelic.commons.utils.NickUtils;
import org.bukkit.Bukkit;
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
                    UUID firstAuthor = authorIds.get(0);

                    String authors;

                    if (Bukkit.getPlayer(firstAuthor) != null) {
                        Player author = Bukkit.getPlayer(firstAuthor);

                        if (author.isOnline() && NickUtils.isNicked(author)) {
                            authors = ChatColor.DARK_AQUA + NickUtils.getRealName(author);
                        } else {
                            authors = Fetcher.getBasicName(firstAuthor);
                        }
                    } else {
                        authors = Fetcher.getBasicName(firstAuthor);
                    }

                    if (authorIds.size() == 2) {
                        UUID secondAuthor = authorIds.get(0);

                        if (Bukkit.getPlayer(secondAuthor) != null) {
                            Player author = Bukkit.getPlayer(secondAuthor);

                            if (author.isOnline() && NickUtils.isNicked(author)) {
                                authors += " and " + ChatColor.DARK_AQUA + NickUtils.getRealName(author);
                            } else {
                                authors += " and " + Fetcher.getBasicName(secondAuthor);
                            }
                        } else {
                            authors += " and " + Fetcher.getBasicName(secondAuthor);
                        }
                    } else if (authorIds.size() > 2) {
                        authors += " and " + authorIds.size() + " others";
                    }

                    player.sendMessage("");
                    player.sendMessage(ChatUtils.getHeader("Match #" + MatchManager.getMatches()));
                    player.sendMessage(ChatColor.ITALIC + gameMode.getDescription());
                    player.sendMessage("");
                    if (!map.getName().equals("UHC"))
                        player.sendMessage(ChatColor.GRAY + " • " + ChatColor.WHITE + "Map: " + map.getColoredName() + " by " + authors);
                    player.sendMessage(ChatColor.GRAY + " • " + ChatColor.WHITE + "Game Mode: " + gameMode.getColoredNameWithAlias());
                    if (lives > 0)
                        player.sendMessage(ChatColor.GRAY + " • " + ChatColor.WHITE + "Lives: " + ChatColor.AQUA + lives + (rounds ? ChatColor.WHITE + " per round" : ""));
                    if (!regen)
                        player.sendMessage(ChatColor.GRAY + " • " + ChatColor.WHITE + "Natural Regen: " + ChatColor.AQUA + "Off");
                    player.sendMessage("");
                } else {
                    CommandUtils.sendErrorMessage(player, "You can't view the match info right now!");
                }
            });
    }

}
