package net.purelic.cgm.commands.toggles;

import cloud.commandframework.Command;
import cloud.commandframework.bukkit.BukkitCommandManager;
import net.md_5.bungee.api.ChatColor;
import net.purelic.cgm.core.constants.MatchState;
import net.purelic.cgm.utils.PlayerUtils;
import net.purelic.commons.commands.parsers.CustomCommand;
import net.purelic.commons.utils.CommandUtils;
import net.purelic.commons.utils.ItemCrafter;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

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
                        CommandUtils.sendSuccessMessage(player, "You're now in spectator mode! To switch back do /toggle gamemode");
                    }

                    for (ItemStack item : player.getInventory().getContents()) {
                        if (item == null) continue;

                        if (new ItemCrafter(item).hasTag("toggle_gm")) {
                            boolean spectator = player.getGameMode() == GameMode.SPECTATOR;
                            item.setType(spectator ? Material.PRISMARINE : Material.SEA_LANTERN);
                            ItemMeta meta = item.getItemMeta();
                            meta.setDisplayName("" + ChatColor.RESET + ChatColor.BOLD +
                                (spectator ? "Spectator" : "Creative") + " Mode" + ChatColor.RESET + ChatColor.GRAY + " (/toggle gamemode)");
                            item.setItemMeta(meta);
                            break;
                        }
                    }
                });
    }

}
