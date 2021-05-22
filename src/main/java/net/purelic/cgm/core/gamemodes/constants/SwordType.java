package net.purelic.cgm.core.gamemodes.constants;

import org.bukkit.Material;

public enum SwordType {

    NONE(null),
    WOOD(Material.WOOD_SWORD),
    STONE(Material.STONE_SWORD),
    GOLD(Material.GOLD_SWORD),
    IRON(Material.IRON_SWORD),
    DIAMOND(Material.DIAMOND_SWORD),
    STICK(Material.STICK),
    ;

    private final Material material;

    SwordType(Material material) {
        this.material = material;
    }

    public Material getMaterial() {
        return this.material;
    }

}
