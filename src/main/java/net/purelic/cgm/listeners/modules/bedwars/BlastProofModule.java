package net.purelic.cgm.listeners.modules.bedwars;

import net.minecraft.server.v1_8_R3.Block;
import net.purelic.cgm.core.gamemodes.EnumSetting;
import net.purelic.cgm.core.gamemodes.constants.GameType;
import net.purelic.cgm.events.match.MatchEndEvent;
import net.purelic.cgm.events.match.MatchStartEvent;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Set;

public class BlastProofModule implements Listener {

    private final float blastProofDurability = 1.8E7F;

    @EventHandler
    public void onMatchStart(MatchStartEvent event) {
        if (EnumSetting.GAME_TYPE.is(GameType.BED_WARS)) {
            this.setDurability("stained_glass", this.blastProofDurability);
        }
    }

    @EventHandler
    public void onMatchEnd(MatchEndEvent event) {
        if (EnumSetting.GAME_TYPE.is(GameType.BED_WARS)) {
            this.setDurability("stained_glass", 1.5F);
        }
    }

    private float getDurability(String blockName) {
        try {
            Block block = Block.getByName(blockName.toLowerCase());
            Field field = Block.class.getDeclaredField("durability");
            field.setAccessible(true);
            return field.getFloat(block);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
            return -1;
        }
    }

    private void setDurability(String blockName, float durability) {
        try {
            Block block = Block.getByName(blockName.toLowerCase());
            Field field = Block.class.getDeclaredField("durability");
            field.setAccessible(true);
            field.set(block, durability);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    public static boolean touchingBlastProofBlocks(org.bukkit.block.Block block, int touching) {
        int count = 0;

        if (isBlastProof(block.getRelative(BlockFace.UP))) {
            count++;
        }

        if (isBlastProof(block.getRelative(BlockFace.DOWN))) {
            count++;
        }

        if (isBlastProof(block.getRelative(BlockFace.NORTH))) {
            count++;
        }

        if (isBlastProof(block.getRelative(BlockFace.SOUTH))) {
            count++;
        }

        if (isBlastProof(block.getRelative(BlockFace.EAST))) {
            count++;
        }

        if (isBlastProof(block.getRelative(BlockFace.WEST))) {
            count++;
        }

        return count >= touching;
    }

    public static Set<org.bukkit.block.Block> getBlastProofBlocks(org.bukkit.block.Block tnt) {
        Set<org.bukkit.block.Block> toRemove = new HashSet<>();

        boolean up = isBlastProof(tnt.getRelative(BlockFace.UP));
        boolean down = isBlastProof(tnt.getRelative(BlockFace.DOWN));
        boolean north = isBlastProof(tnt.getRelative(BlockFace.NORTH));
        boolean south = isBlastProof(tnt.getRelative(BlockFace.SOUTH));
        boolean east = isBlastProof(tnt.getRelative(BlockFace.EAST));
        boolean west = isBlastProof(tnt.getRelative(BlockFace.WEST));

        if (up) {
            if (north) {
                toRemove.add(tnt.getRelative(0, 1, -1));
            }

            if (south) {
                toRemove.add(tnt.getRelative(0, 1, 1));
            }

            if (east) {
                toRemove.add(tnt.getRelative(1, 1, 0));
            }

            if (west) {
                toRemove.add(tnt.getRelative(-1, 1, 0));
            }
        }

        if (down) {
            if (north) {
                toRemove.add(tnt.getRelative(0, -1, -1));
            }

            if (south) {
                toRemove.add(tnt.getRelative(0, -1, 1));
            }

            if (east) {
                toRemove.add(tnt.getRelative(1, -1, 0));
            }

            if (west) {
                toRemove.add(tnt.getRelative(-1, -1, 0));
            }
        }

        if (north) {
            if (west) {
                toRemove.add(tnt.getRelative(-1, 0, -1));
                if (down) toRemove.add(tnt.getRelative(-1, -1, -1));
                if (up) toRemove.add(tnt.getRelative(-1, 1, -1));
            }

            if (east) {
                toRemove.add(tnt.getRelative(1, 0, -1));
                if (down) toRemove.add(tnt.getRelative(1, -1, -1));
                if (up) toRemove.add(tnt.getRelative(1, 1, -1));
            }
        }

        if (south) {
            if (west) {
                toRemove.add(tnt.getRelative(-1, 0, 1));
                if (down) toRemove.add(tnt.getRelative(-1, -1, 1));
                if (up) toRemove.add(tnt.getRelative(-1, 1, 1));
            }

            if (east) {
                toRemove.add(tnt.getRelative(1, 0, 1));
                if (down) toRemove.add(tnt.getRelative(1, -1, 1));
                if (up) toRemove.add(tnt.getRelative(1, 1, 1));
            }
        }

        // annoying corner blocks
        org.bukkit.block.Block nw = tnt.getRelative(BlockFace.NORTH_WEST);
        org.bukkit.block.Block corner = nw.getRelative(BlockFace.DOWN);

        if (isBlastProof(nw)) {
            if ((north || isBlastProof(corner.getRelative(BlockFace.EAST)))
                && (west || isBlastProof(corner.getRelative(BlockFace.SOUTH)))) {
                toRemove.add(corner);
            }
        }

        corner = nw.getRelative(BlockFace.UP);

        if (isBlastProof(nw)) {
            if ((north || isBlastProof(corner.getRelative(BlockFace.EAST)))
                && (west || isBlastProof(corner.getRelative(BlockFace.SOUTH)))) {
                toRemove.add(corner);
            }
        }

        org.bukkit.block.Block ne = tnt.getRelative(BlockFace.NORTH_EAST);
        corner = ne.getRelative(BlockFace.DOWN);

        if (isBlastProof(ne)) {
            if ((north || isBlastProof(corner.getRelative(BlockFace.EAST)))
                && (west || isBlastProof(corner.getRelative(BlockFace.SOUTH)))) {
                toRemove.add(corner);
            }
        }

        corner = ne.getRelative(BlockFace.UP);

        if (isBlastProof(ne)) {
            if ((north || isBlastProof(corner.getRelative(BlockFace.WEST)))
                && (east || isBlastProof(corner.getRelative(BlockFace.SOUTH)))) {
                toRemove.add(corner);
            }
        }

        org.bukkit.block.Block sw = tnt.getRelative(BlockFace.SOUTH_WEST);
        corner = sw.getRelative(BlockFace.DOWN);

        if (isBlastProof(sw)) {
            if ((south || isBlastProof(corner.getRelative(BlockFace.EAST)))
                && (west || isBlastProof(corner.getRelative(BlockFace.NORTH)))) {
                toRemove.add(corner);
            }
        }

        corner = sw.getRelative(BlockFace.UP);

        if (isBlastProof(sw)) {
            if ((south || isBlastProof(corner.getRelative(BlockFace.EAST)))
                && (west || isBlastProof(corner.getRelative(BlockFace.NORTH)))) {
                toRemove.add(corner);
            }
        }

        org.bukkit.block.Block se = tnt.getRelative(BlockFace.SOUTH_EAST);
        corner = se.getRelative(BlockFace.DOWN);

        if (isBlastProof(se)) {
            if ((south || isBlastProof(corner.getRelative(BlockFace.WEST)))
                && (east || isBlastProof(corner.getRelative(BlockFace.NORTH)))) {
                toRemove.add(corner);
            }
        }

        corner = se.getRelative(BlockFace.UP);

        if (isBlastProof(se)) {
            if ((south || isBlastProof(corner.getRelative(BlockFace.WEST)))
                && (east || isBlastProof(corner.getRelative(BlockFace.NORTH)))) {
                toRemove.add(corner);
            }
        }

        return toRemove;
    }

    private static boolean isBlastProof(org.bukkit.block.Block block) {
        return block != null && block.getType() != null && block.getType() == Material.STAINED_GLASS;
    }

}
