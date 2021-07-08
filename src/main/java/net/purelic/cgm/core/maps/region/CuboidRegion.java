package net.purelic.cgm.core.maps.region;

import org.bukkit.Location;

import java.util.Map;

public class CuboidRegion extends Region {

    public CuboidRegion(Map<String, Object> yaml) {
        super(yaml);
    }

    @Override
    public boolean contains(Location location) {
        double x = location.getX();
        double y = location.getY();
        double z = location.getZ();
        return x >= (double) this.getMin().getBlockX() && x <= (double) this.getMax().getBlockX()
            && y >= (double) this.getMin().getBlockY() && y <= (double) this.getMax().getBlockY()
            && z >= (double) this.getMin().getBlockZ() && z <= (double) this.getMax().getBlockZ();
    }

}
