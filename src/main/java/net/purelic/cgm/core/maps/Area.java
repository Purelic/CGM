package net.purelic.cgm.core.maps;

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
        return world;
    }

    public int getMinX() {
        return minX;
    }

    public int getMinZ() {
        return minZ;
    }

    public int getMaxX() {
        return maxX;
    }

    public int getMaxZ() {
        return maxZ;
    }

    public boolean contains(Area area) {
        return area.getWorld().equals(world) &&
                area.getMinX() >= minX && area.getMaxX() <= maxX &&
                area.getMinZ() >= minZ && area.getMaxZ() <= maxZ;
    }

    public boolean contains(Location location) {
        return contains(location.getBlockX(), location.getBlockZ());
    }

    public boolean contains(int x, int z) {
        return x >= minX && x <= maxX &&
                z >= minZ && z <= maxZ;
    }

    public boolean overlaps(Area area) {
        return area.getWorld().equals(world) &&
                !(area.getMinX() > maxX || area.getMinZ() > maxZ ||
                        minZ > area.getMaxX() || minZ > area.getMaxZ());
    }

}