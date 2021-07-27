package net.purelic.cgm.commands.toggles;

import cloud.commandframework.Command;
import cloud.commandframework.bukkit.BukkitCommandManager;
import net.md_5.bungee.api.ChatColor;
import net.purelic.cgm.utils.PlayerUtils;
import net.purelic.commons.commands.parsers.CustomCommand;
import net.purelic.commons.utils.CommandUtils;
import net.purelic.commons.utils.ItemCrafter;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ToggleSpectatorsCommand implements CustomCommand {

    private static final List<UUID> hidingSpectators = new ArrayList<>();

    @Override
    public Command.Builder<CommandSender> getCommandBuilder(BukkitCommandManager<CommandSender> mgr) {
        return mgr.commandBuilder("toggle")
            .literal("spectators", "specs")
            .senderType(Player.class)
            .handler(c -> {
                Player player = (Player) c.getSender();
                UUID uuid = player.getUniqueId();

                if (hidingSpectators.remove(uuid)) {
                    PlayerUtils.updateVisibility(player);
                    CommandUtils.sendSuccessMessage(player, "You will now see spectators and eliminated players!");
                } else {
                    hidingSpectators.add(uuid);
                    PlayerUtils.updateVisibility(player);
                    CommandUtils.sendSuccessMessage(player, "You are now hiding spectators and eliminated players!");
                }

                for (ItemStack item : player.getInventory().getContents()) {
                    if (item == null) continue;

                    if (new ItemCrafter(item).hasTag("toggle_spectators")) {
                        boolean hiding = hideSpectators(player);
                        item.setType(hiding ? Material.REDSTONE : Material.GLOWSTONE_DUST);
                        ItemMeta meta = item.getItemMeta();
                        meta.setDisplayName("" + ChatColor.RESET + ChatColor.BOLD +
                            (hiding ? "Hiding" : "Showing") + " Spectators" + ChatColor.RESET + ChatColor.GRAY + " (R-Click)");
                        item.setItemMeta(meta);
                        break;
                    }
                }
            });
    }

    public static boolean hideSpectators(Player player) {
        return hidingSpectators.contains(player.getUniqueId());
    }

}
