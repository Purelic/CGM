package net.purelic.cgm.uhc.scenarios;

import net.purelic.commons.modules.Module;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.inventory.ItemStack;

public class BowlessScenario implements Module {

    @EventHandler
    public void onCraftingPrepare(PrepareItemCraftEvent event) {
        Material material = event.getRecipe().getResult().getType();

        if (material == Material.BOW || material == Material.ARROW) {
            event.getInventory().setResult(new ItemStack(Material.AIR));
        }
    }

    @EventHandler
    public void onItemSpawn(ItemSpawnEvent event) {
        Material material = event.getEntity().getItemStack().getType();

        if (material == Material.BOW || material == Material.ARROW) {
            event.setCancelled(true);
        }
    }

}
