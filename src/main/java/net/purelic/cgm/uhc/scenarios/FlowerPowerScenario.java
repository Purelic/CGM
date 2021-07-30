package net.purelic.cgm.uhc.scenarios;

import net.purelic.commons.modules.Module;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.ExperienceOrb;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Random;

public class FlowerPowerScenario implements Module {

    private final Material[] flowers = new Material[]{
        Material.YELLOW_FLOWER,
        Material.RED_ROSE,
        Material.DEAD_BUSH,
        Material.RED_MUSHROOM,
        Material.BROWN_MUSHROOM,
        Material.DOUBLE_PLANT,
    };

    @EventHandler(ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        Block block = event.getBlock();

        // For tall flowers start with the bottom block.
        Block below = block.getRelative(BlockFace.DOWN);

        if (this.isFlower(below.getType())) {
            block = below;
        }

        if (this.isFlower(block.getType())) {
            Location blockLoc = block.getLocation().add(.5, .5, .5);
            block.setType(Material.AIR);

            ExperienceOrb xp = (ExperienceOrb) blockLoc.getWorld().spawnEntity(blockLoc, EntityType.EXPERIENCE_ORB);
            xp.setExperience(2);

            blockLoc.getWorld().dropItem(blockLoc, FlowerDrop.random());
        }
    }

    public boolean isFlower(Material material) {
        for (Material flower : flowers) {
            if (material == flower) return true;
        }

        return false;
    }

    enum FlowerDrop {

        REDSTONE,
        GLOWSTONE_DUST,
        SULPHUR,
        GLASS_BOTTLE,
        NETHER_STALK,
        MAGMA_CREAM,
        BLAZE_POWDER,
        BLAZE_ROD,
        SLIME_BALL,
        GHAST_TEAR,
        SUGAR,
        SPIDER_EYE,
        FERMENTED_SPIDER_EYE,
        RABBIT_FOOT,
        SPECKLED_MELON,
        GOLDEN_CARROT,
        ;

        private static final Random random = new Random();
        private final Material material;
        private final int min;
        private final int max;

        FlowerDrop() {
            this(1, 1);
        }

        FlowerDrop(int min, int max) {
            this.material = Material.valueOf(this.name());
            this.min = min;
            this.max = max;
        }

        public ItemStack getItem() {
            return new ItemStack(
                this.material,
                new Random().nextInt((this.max - this.min) + 1) + this.min
            );
        }

        public static ItemStack random() {
            return values()[random.nextInt(values().length)].getItem();
        }

    }

}
