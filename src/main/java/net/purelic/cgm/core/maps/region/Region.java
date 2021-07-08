package net.purelic.cgm.core.maps.region;

import net.purelic.cgm.core.constants.MatchTeam;
import net.purelic.cgm.core.managers.MatchManager;
import net.purelic.cgm.core.maps.MapYaml;
import net.purelic.cgm.core.maps.Objective;
import net.purelic.commons.utils.YamlUtils;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public abstract class Region extends Objective<RegionModifiers> {

    private final String name;
    private final RegionType type;
    private final boolean enter;
    private final boolean leave;
    private final boolean instantDeath;
    private final boolean breakBlocks;
    private final boolean placeBlocks;
    private final boolean pvp;
    private final boolean damage;
    private final Vector min;
    private final Vector max;
    private final Vector center;
    private final Vector radius;
    private final int minY;
    private final int maxY;
    private final Location destination;
    private final TeleporterType teleporterType;
    private final DependencyType dependencyType;
    private int dependency;

    public Region(Map<String, Object> yaml) {
        super(yaml);
        this.name = this.get(RegionModifiers.NAME);
        this.type = this.get(RegionModifiers.TYPE, RegionType.CUBOID);
        this.enter = this.get(RegionModifiers.ENTER, true);
        this.leave = this.get(RegionModifiers.LEAVE, true);
        this.instantDeath = this.get(RegionModifiers.INSTANT_DEATH, false);
        this.breakBlocks = this.get(RegionModifiers.BREAK_BLOCKS, true);
        this.placeBlocks = this.get(RegionModifiers.PLACE_BLOCKS, true);
        this.pvp = this.get(RegionModifiers.PVP, true);
        this.damage = this.get(RegionModifiers.DAMAGE, true);
        this.min = this.getVector(RegionModifiers.MIN);
        this.max = this.getVector(RegionModifiers.MAX);
        this.center = this.getVector(RegionModifiers.CENTER);
        this.radius = this.getVector(RegionModifiers.RADIUS);
        this.minY = this.get(RegionModifiers.MIN_Y);
        this.maxY = this.get(RegionModifiers.MAX_Y);
        String destinationCoords = this.get(RegionModifiers.DESTINATION);
        this.destination = destinationCoords == null ? null : YamlUtils.getLocationFromCoords((World) null, destinationCoords);
        this.teleporterType = this.get(RegionModifiers.TELEPORTER_TYPE, TeleporterType.NONE);
        this.dependencyType = this.get(RegionModifiers.DEPENDENCY_TYPE, DependencyType.HILL);
        this.dependency = this.get(RegionModifiers.DEPENDENCY, -1);
    }

    public String getName() {
        return this.isNeutral() ? this.name : this.getOwner().getColor() + this.name + ChatColor.RESET;
    }

    public RegionType getType() {
        return this.type;
    }

    public boolean canEnter() {
        return this.enter;
    }

    public boolean canLeave() {
        return this.leave;
    }

    public boolean isInstantDeath() {
        return this.instantDeath;
    }

    public boolean canBreakBlocks() {
        return this.breakBlocks;
    }

    public boolean canPlaceBlocks() {
        return this.placeBlocks;
    }

    public boolean canPvP() {
        return this.pvp;
    }

    public boolean canTakeDamage() {
        return this.damage;
    }

    public Vector getMin() {
        return this.min;
    }

    public Vector getMax() {
        return this.max;
    }

    public Vector getRegionCenter() {
        return this.center;
    }

    public Vector getRadius() {
        return this.radius;
    }

    public int getMinY() {
        return this.minY;
    }

    public int getMaxY() {
        return this.maxY;
    }

    public boolean isTeleporter() {
        return this.destination != null;
    }

    public boolean hasDependency() {
        return this.dependency > -1;
    }

    public Objective<?> getDependency() {
        MapYaml yaml = MatchManager.getCurrentMap().getYaml();
        List<Objective<?>> objectives = new ArrayList<>();

        if (this.dependencyType == DependencyType.HILL) {
            objectives.addAll(yaml.getHills());
        } else if (this.dependencyType == DependencyType.FLAG) {
            // TODO objectives.addAll(yaml.getFlags());
        }

        if (this.dependency >= objectives.size()) {
            this.dependency = -1;
            return null;
        }

        return objectives.get(this.dependency);
    }

    public boolean canTeleport(Player player) {
        return this.canTeleport(MatchTeam.getTeam(player));
    }

    // TODO also need a cooldown
    public boolean canTeleport(MatchTeam team) {
        if (!this.isTeleporter()) return false;
        if (this.isNeutral() || !this.hasDependency()) return true;
        if (!this.isNeutral() && team != this.getOwner()) return false;

        Objective<?> dependency = this.getDependency();
        if (dependency == null) return true;

        return dependency.isLoaded() && dependency.isActive() && dependency.isCapturedBy(this.getOwner());
    }

    public void teleport(Player player) {
        Location destination = this.destination.clone();
        destination.setWorld(player.getWorld());

        player.getWorld().playSound(player.getLocation(), Sound.ENDERMAN_TELEPORT, 1F, 1.5F);
        player.getWorld().playSound(destination, Sound.ENDERMAN_TELEPORT, 1F, 0.5F);
        player.teleport(destination);
    }

    public void show() {
        this.fill(Material.AIR, this.teleporterType.getMaterial());
    }

    public void hide() {
        this.fill(this.teleporterType.getMaterial(), Material.AIR);
    }

    public void fill(Material replace, Material replacement) {
        if (!this.isTeleporter()) return;

        for (int x = this.min.getBlockX(); x <= this.max.getBlockX(); x++) {
            for (int y = this.minY; y <= this.maxY; y++) {
                for (int z = this.min.getBlockZ(); z <= this.max.getBlockZ(); z++) {
                    Block block = this.getWorld().getBlockAt(x, y, z);

                    if (block.getType() == replace && this.contains(block)) {
                        BlockState state = block.getState();
                        state.setType(replacement);
                        state.update(true, false);
                    }
                }
            }
        }
    }

    abstract boolean contains(Location location);

    public boolean contains(Player player) {
        return this.contains(player.getLocation());
    }

    public boolean contains(Block block) {
        return this.contains(block.getLocation());
    }

}
