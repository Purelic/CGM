package net.purelic.cgm.core.maps.region;

import org.bukkit.Location;

import java.util.Map;

public class EllipsoidRegion extends Region {

    public EllipsoidRegion(Map<String, Object> yaml) {
        super(yaml);
    }

    @Override
    public boolean contains(Location location) {
        return location.toVector()
            .subtract(this.getRegionCenter())
            .divide(this.getRadius())
            .lengthSquared() <= 1.0D;
    }

}
