package net.purelic.cgm.core.maps.region;

import org.bukkit.Location;
import org.bukkit.World;

public class Area {

    private final World world;
    private final int minX;
    private final int maxX;
    private final int minZ;
    private final int maxZ;

    public Area(Location loc1, Location loc2) {
        this(loc1.getWorld(), loc1.getBlockX(), loc1.getBlockZ(), loc2.getBlockX(), loc2.getBlockZ());
    }

    public Area(World world, int x1, int z1, int x2, int z2) {
        this.world = world;
        this.minX = Math.min(x1, x2);
        this.minZ = Math.min(z1, z2);
        this.maxX = Math.max(x1, x2);
        this.maxZ = Math.max(z1, z2);
    }

    public World getWorld() {
        return this.world;
    }

    public int getMinX() {
        return this.minX;
    }

    public int getMinZ() {
        return this.minZ;
    }

    public int getMaxX() {
        return this.maxX;
    }

    public int getMaxZ() {
        return this.maxZ;
    }

    public boolean contains(Area area) {
        return area.getWorld().equals(this.world) &&
                area.getMinX() >= this.minX && area.getMaxX() <= this.maxX &&
                area.getMinZ() >= this.minZ && area.getMaxZ() <= this.maxZ;
    }

    public boolean contains(Location location) {
        return this.contains(location.getBlockX(), location.getBlockZ());
    }

    public boolean contains(int x, int z) {
        return x >= this.minX && x <= this.maxX &&
                z >= this.minZ && z <= this.maxZ;
    }

    public boolean overlaps(Area area) {
        return area.getWorld().equals(this.world) &&
                !(area.getMinX() > this.maxX || area.getMinZ() > this.maxZ ||
                    this.minZ > area.getMaxX() || this.minZ > area.getMaxZ());
    }

}