package net.purelic.cgm.kit;

import net.purelic.cgm.CGM;
import net.purelic.commons.Commons;
import net.purelic.commons.profile.Preference;
import net.purelic.commons.profile.Profile;
import net.purelic.commons.profile.preferences.ArmorColor;
import net.purelic.commons.utils.ItemCrafter;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class ControlsKit implements Kit {

    @Override
    public void apply(Player player) {
        player.getInventory().addItem(this.getSetNextItem(), (CGM.getPlaylist().isUHC() ? this.getUHCItem() : this.getTogglesItem()));

        ArmorColor color = ArmorColor.YELLOW;
        Profile profile = Commons.getProfile(player);
        String colorPref = (String) profile.getPreference(Preference.ARMOR_COLOR_UNLOCK, ArmorColor.YELLOW.name());

        if (ArmorColor.contains(colorPref)) {
            color = ArmorColor.valueOf(colorPref.toUpperCase());
        }

        player.getInventory().setArmorContents(this.getArmor(color));
    }

    private ItemStack getSetNextItem() {
        return new ItemCrafter(Material.ENCHANTED_BOOK)
            .name("" + ChatColor.RESET + ChatColor.BOLD + "Set Match" + ChatColor.RESET + ChatColor.GRAY + " (/setnext)")
            .command("setnext", false)
            .addTag("locked")
            .craft();
    }

    private ItemStack getTogglesItem() {
        return new ItemCrafter(Material.PAPER)
            .name("" + ChatColor.RESET + ChatColor.BOLD + "Toggles" + ChatColor.RESET + ChatColor.GRAY + " (/toggles)")
            .command("toggles", false)
            .addTag("locked")
            .craft();
    }

    private ItemStack getUHCItem() {
        return new ItemCrafter(Material.GOLD_INGOT)
            .name("" + ChatColor.RESET + ChatColor.BOLD + "UHC Scenarios" + ChatColor.RESET + ChatColor.GRAY + " (/uhc)")
            .command("uhc", false)
            .addTag("locked")
            .craft();
    }

    private ItemStack[] getArmor(ArmorColor armorColor) {
        return new ItemStack[]{
            this.getColoredArmor(Material.LEATHER_BOOTS, armorColor.getBoots()),
            this.getColoredArmor(Material.LEATHER_LEGGINGS, armorColor.getLeggings()),
            this.getColoredArmor(Material.LEATHER_CHESTPLATE, armorColor.getChestplate()),
            this.getColoredArmor(Material.LEATHER_HELMET, armorColor.getHelmet())
        };
    }

    private ItemStack getColoredArmor(Material item, int rgb) {
        return new ItemCrafter(item).color(rgb).craft();
    }

}
