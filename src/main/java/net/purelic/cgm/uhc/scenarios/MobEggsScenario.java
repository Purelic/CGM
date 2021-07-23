package net.purelic.cgm.uhc.scenarios;

import net.purelic.commons.modules.Module;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Random;

public class MobEggsScenario implements Module {

    private final Random random = new Random();
    private final EntityType[] mobs = new EntityType[]{
        EntityType.CREEPER,
        EntityType.SKELETON,
        EntityType.SPIDER,
        EntityType.GIANT,
        EntityType.ZOMBIE,
        EntityType.SLIME,
        EntityType.GHAST,
        EntityType.ENDERMAN,
        EntityType.CAVE_SPIDER,
        EntityType.SILVERFISH,
        EntityType.BLAZE,
        EntityType.MAGMA_CUBE,
        EntityType.ENDER_DRAGON,
        EntityType.WITHER,
        EntityType.BAT,
        EntityType.WITCH,
        EntityType.ENDERMITE,
        EntityType.GUARDIAN,
        EntityType.PIG,
        EntityType.SHEEP,
        EntityType.COW,
        EntityType.CHICKEN,
        EntityType.SQUID,
        EntityType.WOLF,
        EntityType.MUSHROOM_COW,
        EntityType.SNOWMAN,
        EntityType.OCELOT,
        EntityType.IRON_GOLEM,
        EntityType.VILLAGER,
        EntityType.HORSE,
        EntityType.RABBIT,
    };

    @EventHandler
    public void onProjectileHit(ProjectileHitEvent event) {
        if (event.getEntityType() != EntityType.EGG) {
            return;
        }

        EntityType type = getRandomEntity();
        Location loc = event.getEntity().getLocation();
        loc.getWorld().spawnEntity(loc, type);
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        if (event.getEntityType() != EntityType.CHICKEN) {
            return;
        }

        int num = this.random.nextInt(20) + 1;

        if (num == 1) { // 5%
            event.getDrops().add(new ItemStack(Material.EGG));
        }
    }

    private EntityType getRandomEntity() {
        return this.mobs[this.random.nextInt(this.mobs.length)];
    }

}
