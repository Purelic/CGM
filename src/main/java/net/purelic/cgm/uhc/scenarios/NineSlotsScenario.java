package net.purelic.cgm.uhc.scenarios;

import net.purelic.cgm.events.participant.ParticipantRespawnEvent;
import net.purelic.commons.modules.Module;
import net.purelic.commons.utils.ItemCrafter;
import org.bukkit.ChatColor;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

public class NineSlotsScenario implements Module {

    private final ItemStack fillItem;

    public NineSlotsScenario() {
        this.fillItem = new ItemCrafter(new ItemStack(Material.STAINED_GLASS_PANE, 1, DyeColor.GRAY.getDyeData()))
            .name("" + ChatColor.RED + ChatColor.BOLD + "BLOCKED")
            .addTag("locked")
            .craft();
    }

    @EventHandler
    public void onGameStarted(ParticipantRespawnEvent event) {
        for (int i = 9; i <= 35; i++) {
            event.getPlayer().getInventory().setItem(i, this.fillItem);
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        ItemStack item = event.getCurrentItem();

        if (item != null && item.equals(this.fillItem)) {
            event.setCancelled(true);
        }
    }

}
