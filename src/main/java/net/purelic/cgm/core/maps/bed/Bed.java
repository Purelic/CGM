package net.purelic.cgm.core.maps.bed;

import net.purelic.cgm.CGM;
import net.purelic.cgm.core.constants.MatchTeam;
import net.purelic.cgm.core.maps.Objective;
import net.purelic.cgm.core.maps.bed.constants.BedDefense;
import net.purelic.cgm.core.maps.bed.events.BedBreakEvent;
import net.purelic.cgm.listeners.modules.BlockProtectionModule;
import net.purelic.cgm.utils.YamlUtils;
import net.purelic.commons.Commons;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class Bed implements Listener, Objective {

    private final int[] coords;
    private final MatchTeam owner;
    private final BedDefense defense;
    private final BlockFace direction;
    private final Set<Block> blocks;

    private Location location;

    public Bed(Map<String, Object> data) {
        // final variables
        this.coords = YamlUtils.getCoords(((String) data.get("location")).split(","));
        this.owner = MatchTeam.valueOf((String) data.getOrDefault("owner", MatchTeam.BLUE.name()));
        this.defense = BedDefense.valueOf((String) data.getOrDefault("defense", BedDefense.NONE.name()));
        this.direction = BlockFace.valueOf((String) data.getOrDefault("direction", BlockFace.NORTH.name()));
        this.blocks = new HashSet<>();

        // non-final variables
        this.location = null;

        // register bed listeners
        CGM.getPlugin().registerListener(this);
    }

    @Override
    public MatchTeam getOwner() {
        return this.owner;
    }

    public BedDefense getDefense() {
        return this.defense;
    }

    public BlockFace getDirection() {
        return this.direction;
    }

    public Set<Block> getBlocks() {
        return this.blocks;
    }

    public boolean isDestroyed() {
        return this.blocks.isEmpty();
    }

    public void destroy() {
        this.blocks.forEach(Block::breakNaturally);
        this.blocks.clear();
    }

    public Location getLocation() {
        return this.location;
    }

    @Override
    public boolean isLoaded() {
        return this.location != null;
    }

    public void place(World world) {
        this.location = new Location(world, this.coords[0], this.coords[1], this.coords[2]).add(0.5, 0, 0.5);
        this.buildDefense();
    }

    private void buildDefense() {
        Set<BlockState> states = this.defense.build(this);

        for (BlockState state : states) {
            Block block = state.getBlock();
            Material material = block.getType();

            if (material == null || material == Material.AIR) continue;

            if (material == Material.BED_BLOCK) {
                this.blocks.add(block); // foot of bed
                this.blocks.add(block.getRelative(this.direction.getOppositeFace())); // head of bed
            } else {
                BlockProtectionModule.addBreakableBlock(block);
            }
        }
    }

    @Override
    public void reset() {
        this.blocks.clear();

        this.location = null;
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Block block = event.getBlock();
        Player player = event.getPlayer();
        MatchTeam team = MatchTeam.getTeam(player);

        if (!this.blocks.contains(block)) return; // not this bed, ignore

        if (team == this.owner) event.setCancelled(true);
        else Commons.callEvent(new BedBreakEvent(this, player));
    }

    // Some versions are able to place blocks where the bed is
    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        if (this.blocks.contains(event.getBlock())) event.setCancelled(true);
    }

}
