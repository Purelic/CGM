package net.purelic.cgm.core.gamemodes.constants;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public enum ArmorType {

    NONE,
    LEATHER,
    CHAINMAIL,
    GOLD,
    IRON,
    DIAMOND,
    ;

    private final Material helmet;
    private final Material chestplate;
    private final Material leggings;
    private final Material boots;

    ArmorType() {
        String name = this.name();
        boolean none = name.equals("NONE");
        this.helmet = none ? null : Material.valueOf(name + "_HELMET");
        this.chestplate = none ? null : Material.valueOf(name + "_CHESTPLATE");
        this.leggings = none ? null : Material.valueOf(name + "_LEGGINGS");
        this.boots = none ? null : Material.valueOf(name + "_BOOTS");
    }

    public Material getHelmet() {
        return this.helmet;
    }

    public Material getChestplate() {
        return this.chestplate;
    }

    public Material getLeggings() {
        return this.leggings;
    }

    public Material getBoots() {
        return this.boots;
    }

    public static ArmorType getCurrentArmorType(Player player, ArmorPiece piece) {
        return getCurrentArmorType(player.getInventory().getArmorContents(), piece.ordinal());
    }

    private static ArmorType getCurrentArmorType(ItemStack[] armor, int index) {
        return armor.length == 0 ? null : getCurrentArmorType(armor[index]);
    }

    private static ArmorType getCurrentArmorType(ItemStack item) {
        return item == null ? null : getCurrentArmorType(item.getType());
    }

    private static ArmorType getCurrentArmorType(Material material) {
        return material == null || material == Material.AIR ?
            null : valueOf(material.name().split("_")[0]);
    }

}
