package net.purelic.cgm.core.maps.bed.constants;

import net.purelic.cgm.utils.ColorConverter;
import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.material.Bed;
import org.bukkit.material.MaterialData;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public enum BedDefense {

    NONE(Material.AIR, Material.AIR, Material.AIR),
    WOOL(Material.WOOL, Material.AIR, Material.AIR),
    RANKED(Material.ENDER_STONE, Material.STAINED_GLASS, Material.AIR),
    FULL(Material.WOOD, Material.WOOL, Material.STAINED_GLASS),
    OBSIDIAN(Material.OBSIDIAN, Material.AIR, Material.AIR);

    private final Material first;
    private final Material second;
    private final Material third;

    BedDefense(Material first, Material second, Material third) {
        this.first = first;
        this.second = second;
        this.third = third;
    }

    public Set<BlockState> build(net.purelic.cgm.core.maps.bed.Bed bed) {
        return this.build(bed.getLocation(), bed.getDirection(), ColorConverter.getDyeColor(bed.getOwner()));
    }

    private Set<BlockState> build(Location location, BlockFace direction, DyeColor color) {
        Set<BlockState> states = new HashSet<>();
        BlockFace sideFace = this.getSideFace(direction);

        // Build bed
        Block bedHeadBlock = location.getBlock();
        Block bedFootBlock = bedHeadBlock.getRelative(direction);

        states.add(bedFootBlock.getState());
        states.add(bedFootBlock.getState());

        BlockState bedFootState = bedFootBlock.getState();
        bedFootState.setType(Material.BED_BLOCK);
        Bed bedFootData = new Bed(Material.BED_BLOCK);
        bedFootData.setHeadOfBed(false);
        bedFootData.setFacingDirection(direction.getOppositeFace());
        bedFootState.setData(bedFootData);
        bedFootState.update(true);

        BlockState bedHeadState = bedHeadBlock.getState();
        bedHeadState.setType(Material.BED_BLOCK);
        Bed bedHeadData = new Bed(Material.BED_BLOCK);
        bedHeadData.setHeadOfBed(true);
        bedHeadData.setFacingDirection(direction.getOppositeFace());
        bedHeadState.setData(bedHeadData);
        bedHeadState.update(true);

        if (this == NONE) return states;

        // Create straight layers
        states.addAll(Arrays.asList(this.setStraightBlocks(bedHeadBlock, BlockFace.UP, color)));
        states.addAll(Arrays.asList(this.setStraightBlocks(bedFootBlock, BlockFace.UP, color)));
        states.addAll(Arrays.asList(this.setStraightBlocks(bedHeadBlock, direction.getOppositeFace(), color)));
        states.addAll(Arrays.asList(this.setStraightBlocks(bedFootBlock, direction, color)));
        states.addAll(Arrays.asList(this.setStraightBlocks(bedHeadBlock, sideFace, color)));
        states.addAll(Arrays.asList(this.setStraightBlocks(bedFootBlock, sideFace, color)));
        states.addAll(Arrays.asList(this.setStraightBlocks(bedHeadBlock, sideFace.getOppositeFace(), color)));
        states.addAll(Arrays.asList(this.setStraightBlocks(bedFootBlock, sideFace.getOppositeFace(), color)));

        // Create diagonal layers
        states.addAll(Arrays.asList(this.setDiagonalBlocks(bedHeadBlock, direction.getOppositeFace(), color)));
        states.addAll(Arrays.asList(this.setDiagonalBlocks(bedFootBlock, direction, color)));
        states.addAll(Arrays.asList(this.setDiagonalBlocks(bedHeadBlock, sideFace, color)));
        states.addAll(Arrays.asList(this.setDiagonalBlocks(bedFootBlock, sideFace, color)));
        states.addAll(Arrays.asList(this.setDiagonalBlocks(bedHeadBlock, sideFace.getOppositeFace(), color)));
        states.addAll(Arrays.asList(this.setDiagonalBlocks(bedFootBlock, sideFace.getOppositeFace(), color)));

        // Create corner layers
        states.addAll(Arrays.asList(this.setCornerBlocks(bedHeadBlock, direction.getOppositeFace(), color)));
        states.addAll(Arrays.asList(this.setCornerBlocks(bedFootBlock, direction, color)));

        return states;
    }

    private BlockState[] setStraightBlocks(Block bed, BlockFace face, DyeColor color) {
        BlockState[] states = new BlockState[3];
        Block block = bed.getRelative(face);
        states[0] = this.setBlock(block, this.first, color);
        states[1] = this.setBlock(block.getRelative(face), this.second, color);
        states[2] = this.setBlock(block.getRelative(face).getRelative(face), this.third, color);
        return states;
    }

    private BlockState[] setDiagonalBlocks(Block bed, BlockFace face, DyeColor color) {
        BlockState[] states = new BlockState[3];
        Block block = bed.getRelative(face).getRelative(BlockFace.UP);
        states[0] = this.setBlock(block, this.second, color);
        states[1] = this.setBlock(block.getRelative(BlockFace.UP), this.third, color);
        states[2] = this.setBlock(block.getRelative(face), this.third, color);
        return states;
    }

    private BlockState[] setCornerBlocks(Block bed, BlockFace face, DyeColor color) {
        BlockState[] states = new BlockState[8];
        BlockFace side1 = this.getSideFace(face);
        Block block = bed.getRelative(face).getRelative(side1);

        states[0] = this.setBlock(block, this.second, color);
        states[1] = this.setBlock(block.getRelative(BlockFace.UP), this.third, color);
        states[2] = this.setBlock(block.getRelative(side1), this.third, color);
        states[3] = this.setBlock(block.getRelative(face), this.third, color);

        BlockFace side2 = side1.getOppositeFace();
        block = bed.getRelative(face).getRelative(side2);

        states[4] = this.setBlock(block, this.second, color);
        states[5] = this.setBlock(block.getRelative(BlockFace.UP), this.third, color);
        states[6] = this.setBlock(block.getRelative(side2), this.third, color);
        states[7] = this.setBlock(block.getRelative(face), this.third, color);

        return states;
    }

    private BlockState setBlock(Block block, Material material, DyeColor color) {
        BlockState currentState = block.getState();
        block.setType(material);

        if (material == Material.WOOL
                || material == Material.STAINED_CLAY
                || material == Material.STAINED_GLASS) {
            BlockState state = block.getState();
            MaterialData data = state.getData();
            data.setData(color.getData());
            state.update();
        }

        return currentState;
    }

    private BlockFace getSideFace(BlockFace face) {
        return face == BlockFace.NORTH || face == BlockFace.SOUTH ? BlockFace.EAST : BlockFace.NORTH;
    }

}
