package net.purelic.cgm.core.maps.region;

import org.bukkit.Location;

import java.util.Map;

public class CylinderRegion extends Region {

    public CylinderRegion(Map<String, Object> yaml) {
        super(yaml);
    }

    @Override
    public boolean contains(Location location) {
        int blockY = location.getBlockY();

        if (blockY >= this.getMinY() && blockY <= this.getMaxY()) {
            return new Vector2D(location.toVector())
                .subtract(this.getRegionCenter())
                .divide(this.getRadius())
                .lengthSq() <= 1.0D;
        } else {
            return false;
        }
    }

}
