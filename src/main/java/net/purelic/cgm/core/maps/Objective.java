package net.purelic.cgm.core.maps;

import net.purelic.cgm.core.constants.MatchTeam;
import net.purelic.cgm.utils.YamlObject;
import net.purelic.cgm.utils.YamlUtils;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.util.Vector;

import java.util.Map;

public abstract class Objective<E extends Enum<E>> extends YamlObject<E> {

    private final String rawLocation;
    private final double[] rawCoords;
    private final Vector coords;
    private final MatchTeam owner;
    private World world;
    private Location location;
    private Location center;
    private Block centerBlock;
    private boolean active;

    public Objective(Map<String, Object> yaml) {
        super(yaml);
        this.rawLocation = (String) yaml.get("location");
        this.rawCoords = YamlUtils.getCoords(this.rawLocation.split(","));
        this.coords = new Vector(this.rawCoords[0], this.rawCoords[1], this.rawCoords[2]);
        this.owner = MatchTeam.valueOf((String) yaml.getOrDefault("owner", MatchTeam.SOLO.name()));
        this.active = false;
    }

    public String getRawLocation() {
        return this.rawLocation;
    }

    public double[] getRawCoords() {
        return this.rawCoords;
    }

    public Vector getCoords() {
        return this.coords;
    }

    public boolean isLoaded() {
        return this.world != null;
    }

    public void unload() {
        this.world = null;
    }

    public void setWorld(World world) {
        this.world = world;
        this.location = this.coords.toLocation(world);
        this.center = this.location.clone().add(0.5, 0, 0.5);
        this.centerBlock = this.center.getBlock();
    }

    public World getWorld() {
        return this.world;
    }

    public Location getLocation() {
        return this.location;
    }

    public Location getCenter() {
        return this.center;
    }

    public Block getCenterBlock() {
        return this.centerBlock;
    }

    public MatchTeam getOwner() {
        return this.owner;
    }

    public boolean isOwner(MatchTeam team) {
        return this.owner == team;
    }

    public boolean isNeutral() {
        return this.owner == MatchTeam.SOLO;
    }

    public boolean isCapturedBy(MatchTeam team) {
        return this.getOwner() == team;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public boolean isActive() {
        return this.active;
    }

}
