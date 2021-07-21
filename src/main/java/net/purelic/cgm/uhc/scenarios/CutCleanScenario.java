package net.purelic.cgm.uhc.scenarios;

import net.purelic.commons.modules.Module;
import net.purelic.commons.utils.TaskUtils;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.ExperienceOrb;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.LeavesDecayEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.Optional;
import java.util.Random;

public class CutCleanScenario implements Module {

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        for (int i = 0; i < event.getDrops().size(); i++) {
            Material replacement = null;
            Material drop = event.getDrops().get(i).getType();

            if (drop != null) {
                switch (drop) {
                    case RAW_BEEF:
                        replacement = Material.COOKED_BEEF;
                        break;
                    case RAW_CHICKEN:
                        replacement = Material.COOKED_CHICKEN;
                        break;
                    case MUTTON:
                        replacement = Material.COOKED_MUTTON;
                        break;
                    case RABBIT:
                        replacement = Material.COOKED_RABBIT;
                        break;
                    case PORK:
                        replacement = Material.GRILLED_PORK;
                        break;
                    default:
                        break;
                }
            }

            if (replacement != null) {
                ItemStack cooked = event.getDrops().get(i).clone();
                cooked.setType(replacement);
                event.getDrops().set(i, cooked);
            }
        }

        if (event.getEntityType() == EntityType.COW) {
            event.getDrops().add(new ItemStack(Material.LEATHER));
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onBlockBreak(BlockBreakEvent event) {
        Block block = event.getBlock();
        Material tool = event.getPlayer().getItemInHand().getType();
        Location loc = event.getBlock().getLocation().add(0.5, 0, 0.5);
        Material type = block.getType();
        ItemStack drop = null;

        Optional<SmeltableOre> ore = SmeltableOre.valueOf(type);

        if (ore.isPresent() && ore.get().isCorrectTool(tool)) {
            drop = new ItemStack(ore.get().getDrop());

            ExperienceOrb xp = (ExperienceOrb) loc.getWorld().spawnEntity(loc, EntityType.EXPERIENCE_ORB);
            xp.setExperience(ore.get().getXpPerBlock());
        } else if (type == Material.GRAVEL) {
            Random rand = new Random();
            int num = rand.nextInt(2) + 1;

            if (num == 1) { // 50%
                drop = new ItemStack(Material.FLINT);
            }
        } else if (type == Material.COAL_ORE) {
            Random rand = new Random();
            int num = rand.nextInt(2) + 1;

            if (num == 1) { // 50%
                drop = new ItemStack(Material.TORCH, 4);
            }
        } else if (type.name().contains("LEAVES") && tool != Material.SHEARS) {
            Random rand = new Random();
            int num = rand.nextInt(20) + 1;

            if (num == 1) { // 5%
                TaskUtils.run(() -> block.getWorld().dropItem(block.getLocation().add(.5, .5, .5), new ItemStack(Material.APPLE)));
            }
        }

        if (drop != null) {
            block.setType(Material.AIR);
            loc.getWorld().dropItem(loc, drop);
        }
    }

    @EventHandler
    public void onLeavesDecay(LeavesDecayEvent event) {
        Block block = event.getBlock();

        Random rand = new Random();
        int num = rand.nextInt(20) + 1;

        // 5%
        if (num != 1) return;

        // Add apple to drops
        TaskUtils.run(() -> block.getWorld().dropItem(block.getLocation().add(.5, .5, .5), new ItemStack(Material.APPLE)));
    }

    enum SmeltableOre {

        IRON(Material.IRON_ORE, Material.IRON_INGOT),
        GOLD(Material.GOLD_ORE, Material.GOLD_INGOT),
        ;

        private final Material block;
        private final Material drop;

        SmeltableOre(Material block, Material drop) {
            this.block = block;
            this.drop = drop;
        }

        public static Optional<SmeltableOre> valueOf(Material material) {
            return Arrays.stream(values())
                .filter(ore -> ore.equals(material))
                .findFirst();
        }

        public boolean isCorrectTool(Material tool) {
            switch (this) {
                case IRON:
                    return
                        tool == Material.DIAMOND_PICKAXE ||
                            tool == Material.GOLD_PICKAXE ||
                            tool == Material.IRON_PICKAXE ||
                            tool == Material.STONE_PICKAXE;
                case GOLD:
                    return tool == Material.DIAMOND_PICKAXE ||
                        tool == Material.GOLD_PICKAXE ||
                        tool == Material.IRON_PICKAXE;
                default:
                    return false;
            }
        }

        public int getXpPerBlock() {
            switch (this) {
                case IRON:
                    return 2;
                case GOLD:
                default:
                    return 0;
            }
        }

        public Material getDrop() {
            return this.drop;
        }

        public boolean equals(Material material) {
            return this.block == material;
        }

    }

}
