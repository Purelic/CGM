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
        NETHER_WARTS,
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

//        DIAMOND_ORE(1, 8),
//        DIAMOND_CHESTPLATE,
//        DIAMOND_LEGGINGS,
//        DIAMOND_BOOTS,
//        IRON_BARDING,
//        DIAMOND_SPADE,
//        DIAMOND_PICKAXE,
//        SADDLE,
//        DIAMOND(1, 8),
//        DIAMOND_HELMET,
//        IRON_ORE(1, 32),
//        IRON_BLOCK(1, 16),
//        COOKED_BEEF(1, 32),
//        IRON_AXE,
//        IRON_SWORD,
//        IRON_BOOTS,
//        IRON_LEGGINGS,
//        IRON_CHESTPLATE,
//        IRON_HELMET,
//        DIAMOND_SWORD,
//        IRON_INGOT(1, 32),
//        DIAMOND_BLOCK(1, 4),
//        BREWING_STAND_ITEM(1, 32),
//        ENCHANTMENT_TABLE(1, 8),
//        EXP_BOTTLE(1, 16),
//        GLASS_BOTTLE(1, 32),
//        NETHER_STALK(1, 32),
//        REDSTONE_BLOCK(1, 32),
//        SUGAR_CANE,
//        BONE(1, 8),
//        GOLD_INGOT(1, 16),
//        GOLD_BLOCK(1, 4),
//        GOLDEN_APPLE(1, 4),
//        APPLE(1, 16),
//        FISHING_ROD,
//        ARROW(1, 32),
//        BOW,
//        COOKED_FISH(1, 32),
//        WATER_BUCKET,
//        LAVA_BUCKET,
//        FLINT_AND_STEEL,
//        GOLD_ORE(1, 32),
//        GOLDEN_CARROT(1, 32),
//        LEATHER(1, 32),
//        STONE(1, 32),
//        DIRT(1, 32),
//        GRAVEL(1, 32),
//        WEB(1, 16),
//        LOG(1, 32),
//        SAND(1, 32),
//        FLINT(1, 32),
//        TNT(1, 16),
//        LADDER(1, 16),
//        CHAINMAIL_HELMET,
//        LEATHER_BOOTS,
//        LEATHER_LEGGINGS,
//        LEATHER_CHESTPLATE,
//        STICK(1, 32),
//        BOOKSHELF(1, 16),
//        WORKBENCH(1, 16),
//        STONE_BUTTON(1, 32),
//        SPONGE(1, 32),
//        PUMPKIN(1, 32),
//        CHEST(1, 16),
//        WATER_LILY(1, 32),
//        GOLD_HOE,
//        IRON_HOE,
//        SOUL_SAND(1, 32),
//        ACACIA_FENCE(1, 32),
//        CACTUS(1, 32),
//        CLAY(1, 32),
//        QUARTZ(1, 32),
//        EGG(1, 16),
//        MELON(1, 32),
//        BRICK_STAIRS(1, 32),
//        MYCEL(1, 32),
//        TRAP_DOOR(1, 32),
//        SMOOTH_BRICK(1, 32),
//        IRON_PLATE(1, 16),
//        HOPPER(1, 16),
//        MUSHROOM_SOUP,
//        WOOL(1, 32),
//        OBSIDIAN(1, 16),
//        FEATHER(1, 16),
//        STRING(1, 3),
//        POWERED_RAIL(1, 32),
//        GLASS(1, 16),
//        LEVER(1, 16),
//        GRASS(1, 32),
//        ICE(1, 32),
//        EMERALD_ORE(1, 32),
//        EMERALD_BLOCK(1, 8),
//        CARROT_STICK,
//        CAULDRON_ITEM(1, 8),
//        SULPHUR(1, 8),
//        PISTON_BASE(1, 32),
//        LAPIS_BLOCK(1, 32),
//        IRON_FENCE(1, 32),
//        CLAY_BALL(1, 32),
//        BRICK(1, 32),
//        WHEAT(1, 32),
//        JUKEBOX(1, 16),
//        SANDSTONE(1, 32),
//        SUGAR(1, 2),
//        ITEM_FRAME(1, 16),
//        SPRUCE_DOOR_ITEM(1, 32),
//        ACTIVATOR_RAIL(1, 32),
//        COBBLE_WALL(1, 32),
//        MILK_BUCKET,
//        COBBLESTONE_STAIRS(1, 32),
//        COBBLESTONE(1, 32),
//        ENDER_PEARL(1, 2),
//        RECORD_5,
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
