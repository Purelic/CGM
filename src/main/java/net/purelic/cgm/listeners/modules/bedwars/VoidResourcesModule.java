package net.purelic.cgm.listeners.modules.bedwars;

import net.purelic.cgm.core.gamemodes.EnumSetting;
import net.purelic.cgm.core.gamemodes.constants.GameType;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.inventory.ItemStack;

public class VoidResourcesModule implements Listener {

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        if (!EnumSetting.GAME_TYPE.is(GameType.BED_WARS)
            || !this.isResource(event.getItemDrop())) return;

        Player player = event.getPlayer();

        if (this.isFalling(player) && this.isOverVoid(player)) {
            event.setCancelled(true);
        }
    }

    private boolean isResource(Item item) {
        if (item == null) return false;

        ItemStack itemStack = item.getItemStack();

        if (itemStack == null) return false;

        Material material = itemStack.getType();

        return material == Material.IRON_INGOT
            || material == Material.GOLD_INGOT
            || material == Material.DIAMOND
            || material == Material.EMERALD;
    }

    private boolean isFalling(Player player) {
        return player.getVelocity().getY() < -0.7;
    }

    private boolean isOverVoid(Player player) {
        Location location = player.getLocation();
        World world = location.getWorld();
        int blockX = location.getBlockX();
        int blockY = location.getBlockY();
        int blockZ = location.getBlockZ();

        for (int i = blockY; i >= 0; i--) {
            Block block = world.getBlockAt(blockX, i, blockZ);

            if (!block.isLiquid() && !block.isEmpty()) {
                return false;
            }
        }

        return true;
    }

}
