package net.purelic.cgm.listeners.modules;

import net.purelic.cgm.CGM;
import net.purelic.cgm.core.constants.MatchState;
import net.purelic.cgm.core.constants.MatchTeam;
import net.purelic.cgm.core.gamemodes.ToggleSetting;
import net.purelic.cgm.core.managers.MatchManager;
import net.purelic.cgm.core.managers.ShopManager;
import net.purelic.cgm.core.match.constants.ParticipantState;
import net.purelic.commons.utils.ItemCrafter;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class ItemLockModule implements Listener {

    @EventHandler
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        Player player = event.getPlayer();

        if (!MatchState.isState(MatchState.STARTED)
            || MatchTeam.getTeam(player) == MatchTeam.OBS
            || (MatchManager.isPlaying(player) && !MatchManager.getParticipant(player).isState(ParticipantState.ALIVE))) {
            event.setCancelled(true);
            return;
        }

        ItemStack item = event.getItemDrop().getItemStack();

        if (this.isLocked(item)) {
            event.setCancelled(true);
        } else if (new ItemCrafter(item).hasTag("kit") && !CGM.getPlaylist().isUHC()) {
            event.getItemDrop().remove();
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        Inventory clicked = event.getClickedInventory();
        Inventory top = event.getView().getTopInventory();
        boolean shiftClick = event.getClick().isShiftClick();
        Player player = (Player) event.getWhoClicked();

        // shift click into a shop
        if (shiftClick && this.isShop(top)) {
            event.setCancelled(true);
        }

        // hotbar swap into a shop
        if (event.getAction() == InventoryAction.HOTBAR_SWAP && this.isShop(top)) {
            event.setCancelled(true);
        }

        // hotbar swap not outside their own inventory
        if (event.getAction() == InventoryAction.HOTBAR_SWAP
            && this.isLocked(player.getInventory().getItem(event.getHotbarButton()))
            && top.getType() != InventoryType.CRAFTING) {
            event.setCancelled(true);
        }

        // shift click outside their own inventory
        if (shiftClick
            && clicked == event.getWhoClicked().getInventory()
            && this.isLocked(event.getCurrentItem())
            && top.getType() != InventoryType.CRAFTING) {
            event.setCancelled(true);
        }

        if (clicked != event.getWhoClicked().getInventory()
            && this.isLocked(event.getCursor())) {
            event.setCancelled(true);
        }

        if (event.getSlotType() == InventoryType.SlotType.ARMOR) {
            String materialName = event.getCurrentItem().getType().name();

            if (materialName.contains("HELMET") && ToggleSetting.PLAYER_HELMET_LOCKED.isEnabled()) {
                event.setCancelled(true);
            } else if (materialName.contains("CHESTPLATE") && ToggleSetting.PLAYER_CHESTPLATE_LOCKED.isEnabled()) {
                event.setCancelled(true);
            } else if (materialName.contains("LEGGINGS") && ToggleSetting.PLAYER_LEGGINGS_LOCKED.isEnabled()) {
                event.setCancelled(true);
            } else if (materialName.contains("BOOTS") && ToggleSetting.PLAYER_BOOTS_LOCKED.isEnabled()) {
                event.setCancelled(true);
            }
        }

        if (event.isCancelled()) ((Player) event.getWhoClicked()).updateInventory();
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        Inventory inventory = event.getInventory();

        if (this.isShop(inventory)) {
            event.setCancelled(true);
            return;
        }

        if (this.isLocked(event.getOldCursor())) {
            int inventorySize = event.getInventory().getSize();

            for (int i : event.getRawSlots()) {
                if (i < inventorySize) {
                    event.setCancelled(true);
                    ((Player) event.getWhoClicked()).updateInventory();
                    break;
                }
            }
        }
    }

    private boolean isLocked(ItemStack item) {
        return item != null && new ItemCrafter(item).hasTag("locked");
    }

    private boolean isShop(Inventory inventory) {
        return ShopManager.isShop(inventory);
    }

}
