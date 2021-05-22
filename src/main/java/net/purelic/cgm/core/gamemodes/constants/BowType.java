package net.purelic.cgm.core.gamemodes.constants;

import org.bukkit.Material;

public enum BowType {

    NONE(null),
    BOW(Material.BOW),
    ;

    private final Material material;

    BowType(Material material) {
        this.material = material;
    }

    public Material getMaterial() {
        return this.material;
    }

}
