package net.purelic.cgm.core.maps;

import net.purelic.cgm.core.constants.MatchTeam;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

public class SpawnPoint {

    private final MatchTeam team;
    private final double x;
    private final double y;
    private final double z;
    private final float yaw;
    private final float pitch;

    public SpawnPoint(MatchTeam team, String coords) {
        String[] split = coords.split(",");
        this.team = team;
        this.x = Double.parseDouble(split[0]);
        this.y = Double.parseDouble(split[1]);
        this.z = Double.parseDouble(split[2]);
        this.yaw = Float.parseFloat(split[3]);
        this.pitch = 0F;
    }

    public SpawnPoint(MatchTeam team, double x, double y, double z, float yaw) {
        this.team = team;
        this.x = x;
        this.y = y;
        this.z = x;
        this.yaw = yaw;
        this.pitch = 0F;
    }

    public Location getLocation(World world) {
        return new Location(world, this.x, this.y, this.z, this.yaw, this.pitch);
    }

    public void teleport(Player player) {
        player.teleport(this.getLocation(player.getWorld()));
    }

    public void teleport(Player player, World world) {
        player.teleport(this.getLocation(world));
    }

}
