package net.purelic.cgm.core.maps;

import net.purelic.cgm.utils.SoundUtils;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.Map;

public class JumpPad {

    private final Location location;
    private final int power;
    private final int angle;
    private final boolean destructive;

    private World world;

    public JumpPad(Map<String, Object> map) {
        int[] coords = this.getCoords(((String) map.get("location")).split(","));
        this.location = new Location(null, coords[0], coords[1], coords[2]);
        this.power = (int) map.getOrDefault("power", 5);
        this.angle = (int) map.getOrDefault("angle", 45);
        this.destructive = (boolean) map.getOrDefault("destructive", true);
    }

    private int[] getCoords(String[] args) {
        int[] coords = new int[3];

        for (int i = 0; i < args.length; i++) {
            coords[i] = Integer.parseInt(args[i]);
        }

        return coords;
    }

    public void create(World world) {
        this.world = world;
        this.location.setWorld(this.world);
        if (this.destructive) this.world.getBlockAt(this.location).setType(Material.SLIME_BLOCK);
    }

    public Location getLocation() {
        return this.location;
    }

    public void playEffect() {
        this.world.spigot().playEffect(
            this.location.clone().add(0.5, 1, 0.5),
            Effect.INSTANT_SPELL,
            0,
            0,
            0.2F,
            0.25F,
            0.20F,
            100F,
            10,
            50
        );
    }

    public void launchPlayer(Player player) {
        Location launchLocation = player.getLocation().clone();
        launchLocation.setPitch((float) -this.angle);
        player.setVelocity(launchLocation.getDirection().normalize().multiply((this.power * 5D) / 15D));

        SoundUtils.SFX.JUMP_PAD_LAUNCH.playAll(this.location);
        player.getWorld().playEffect(this.location.clone().add(0.5, 1.25, 0.5), Effect.EXPLOSION_LARGE, 1, 1);
    }

}
