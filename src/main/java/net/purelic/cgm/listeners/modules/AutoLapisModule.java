package net.purelic.cgm.listeners.modules;

import org.bukkit.DyeColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.EnchantingInventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.Dye;

public class AutoLapisModule implements Listener {

    @EventHandler
    public void onInventoryOpen(InventoryOpenEvent event) {
        if (event.getInventory() instanceof EnchantingInventory) {
            EnchantingInventory inv = (EnchantingInventory) event.getInventory();
            Dye dye = new Dye();
            dye.setColor(DyeColor.BLUE);
            ItemStack item = dye.toItemStack();
            item.setAmount(64);
            inv.setItem(1, item);
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (event.getInventory() instanceof EnchantingInventory) {
            event.getInventory().remove(new Dye().toItemStack().getType());
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getClickedInventory() instanceof EnchantingInventory
            && event.getCurrentItem().getType() == new Dye().toItemStack().getType()) {
            event.setCancelled(true);
        }
    }

}
