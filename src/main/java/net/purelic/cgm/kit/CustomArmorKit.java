package net.purelic.cgm.kit;

import net.purelic.commons.Commons;
import net.purelic.commons.profile.Preference;
import net.purelic.commons.profile.Profile;
import net.purelic.commons.profile.preferences.ArmorColor;
import net.purelic.commons.utils.ItemCrafter;
import net.purelic.commons.utils.NickUtils;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class CustomArmorKit implements Kit {

    @Override
    public void apply(Player player) {
        ArmorColor color = ArmorColor.YELLOW;
        Profile profile = Commons.getProfile(player);
        String colorPref = (String) profile.getPreference(Preference.ARMOR_COLOR_UNLOCK, ArmorColor.YELLOW.name());

        if (ArmorColor.contains(colorPref)) {
            color = ArmorColor.valueOf(colorPref.toUpperCase());
        }

        if (NickUtils.isNicked(player)) color = ArmorColor.YELLOW;

        player.getInventory().setArmorContents(this.getArmor(color));
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
