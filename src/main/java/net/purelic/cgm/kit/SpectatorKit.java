package net.purelic.cgm.kit;

import net.md_5.bungee.api.ChatColor;
import net.purelic.cgm.commands.toggles.ToggleSpectatorsCommand;
import net.purelic.cgm.core.constants.MatchState;
import net.purelic.cgm.core.managers.MatchManager;
import net.purelic.commons.utils.ItemCrafter;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

public class SpectatorKit implements Kit {

    @Override
    public void apply(Player player) {
        PlayerInventory inv = player.getInventory();

        if (MatchState.isActive()) {
            // since we have auto-join we only need to give the join item if the match is started
            // and we check if they're not playing/eliminated and now spectating
            if (MatchState.isState(MatchState.STARTED) && !MatchManager.isPlaying(player)) {
                inv.addItem(this.getJoinItem());
            }

            inv.addItem(this.getToggleSpecsItem(player));
        }

        if (inv.getItem(8) == null) {
            inv.setItem(8, this.getServerSelectorItem());
        } else {
            inv.addItem(this.getServerSelectorItem());
        }
    }

    private ItemStack getJoinItem() {
        return new ItemCrafter(Material.EMERALD)
            .name(ChatColor.BOLD + "Join Match" + ChatColor.RESET + ChatColor.GRAY + " (/join)")
            .command("join", false)
            .craft();
    }

    private ItemStack getToggleSpecsItem(Player player) {
        boolean hiding = ToggleSpectatorsCommand.hideSpectators(player);
        return new ItemCrafter(hiding ? Material.REDSTONE : Material.GLOWSTONE_DUST)
            .name(ChatColor.BOLD + (hiding ? "Hiding" : "Showing") + " Spectators" + ChatColor.RESET + ChatColor.GRAY + " (R-Click)")
            .command("toggle spectators", false)
            .addTag("toggle_spectators")
            .craft();
    }

    private ItemStack getServerSelectorItem() {
        return new ItemCrafter(Material.COMPASS)
            .name(ChatColor.BOLD + "Servers" + ChatColor.RESET + ChatColor.GRAY + " (/servers)")
            .spring("ServerSelector")
            .craft();
    }

}
