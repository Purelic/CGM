package net.purelic.cgm.kit;

import net.purelic.cgm.CGM;
import net.purelic.commons.utils.ItemCrafter;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class ControlsKit implements Kit {

    @Override
    public void apply(Player player) {
        player.getInventory().addItem(this.getSetNextItem(), (CGM.getPlaylist().isUHC() ? this.getUHCItem() : this.getTogglesItem()));
    }

    private ItemStack getSetNextItem() {
        return new ItemCrafter(Material.ENCHANTED_BOOK)
            .name("" + ChatColor.RESET + ChatColor.BOLD + "Set Match" + ChatColor.RESET + ChatColor.GRAY + " (/setnext)")
            .command("setnext", false)
            .craft();
    }

    private ItemStack getTogglesItem() {
        return new ItemCrafter(Material.PAPER)
            .name("" + ChatColor.RESET + ChatColor.BOLD + "Toggles" + ChatColor.RESET + ChatColor.GRAY + " (/toggles)")
            .command("toggles", false)
            .craft();
    }

    private ItemStack getUHCItem() {
        return new ItemCrafter(Material.GOLD_INGOT)
            .name("" + ChatColor.RESET + ChatColor.BOLD + "UHC Scenarios" + ChatColor.RESET + ChatColor.GRAY + " (/uhc)")
            .command("uhc", false)
            .craft();
    }

}
