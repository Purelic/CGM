package net.purelic.cgm.core.maps.region;

import org.bukkit.Material;

public enum TeleporterType {

    NONE(Material.AIR),
    WATER(Material.WATER),
    ;

    private final Material material;

    TeleporterType(Material material) {
        this.material = material;
    }

    public Material getMaterial() {
        return this.material;
    }

}
