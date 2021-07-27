package net.purelic.cgm.listeners.modules;

import net.purelic.cgm.utils.PreferenceUtils;
import net.purelic.commons.Commons;
import net.purelic.commons.profile.Preference;
import net.purelic.commons.profile.Profile;
import net.purelic.commons.utils.CommandUtils;
import net.purelic.commons.utils.ItemCrafter;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class HotbarModule implements Listener {

    private static final String TITLE = "Hotbar Preferences";

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        ItemStack item = event.getCurrentItem();
        Inventory inventory = event.getView().getTopInventory();

        if (item == null) return;

        if (inventory.getTitle().equals(TITLE) && (event.getClick().isShiftClick() || event.getAction() == InventoryAction.HOTBAR_SWAP)) {
            event.setCancelled(true);
            return;
        }

        Player player = (Player) event.getWhoClicked();
        ItemCrafter ic = new ItemCrafter(item);
        boolean hasTag = ic.hasTag("hotbar_menu_item");

        if (hasTag && ic.getTag("hotbar_menu_item").equals("open")) {
            event.setCancelled(true);
            openHotbarMenu(player);
        }
    }

    @EventHandler
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        Item item = event.getItemDrop();
        if (item != null
            && item.getItemStack() != null
            && new ItemCrafter(item.getItemStack()).hasTag("hotbar_menu_item")) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        Inventory inventory = event.getInventory();

        if (!inventory.getTitle().equals(TITLE)) return;

        Player player = (Player) event.getPlayer();
        Profile profile = Commons.getProfile(player);
        ItemStack[] items = player.getInventory().getContents();

        for (ItemStack item : items) {
            if (item != null && new ItemCrafter(item).hasTag("hotbar_menu_item")) {
                player.getInventory().remove(item);
            }
        }

        int i = 0;

        for (ItemStack item : inventory.getContents()) {
            i++;
            if (item == null) continue;

            if (!new ItemCrafter(item).hasTag("hotbar_menu_item")) {
                player.getInventory().addItem(item);
            } else {
                String value = new ItemCrafter(item).getTag("hotbar_menu_item");
                try {
                    Preference preference = Preference.valueOf(value);
                    profile.updatePreference(preference, (long) i);
                } catch (Exception e) {
                    // ignore
                }
            }
        }

        CommandUtils.sendSuccessMessage(player, "Your hotbar preferences have been saved!");
    }

    public static void openHotbarMenu(Player player) {
        Profile profile = Commons.getProfile(player);
        Inventory inventory = Bukkit.createInventory(null, 9, TITLE);
        setOrAddItem(inventory, profile, Preference.HOTBAR_SWORD, 1, Material.WOOD_SWORD);
        setOrAddItem(inventory, profile, Preference.HOTBAR_BOW, 2, Material.BOW);
        setOrAddItem(inventory, profile, Preference.HOTBAR_SHEARS, 3, Material.SHEARS);
        setOrAddItem(inventory, profile, Preference.HOTBAR_PICKAXE, 4, Material.WOOD_PICKAXE);
        setOrAddItem(inventory, profile, Preference.HOTBAR_AXE, 5, Material.WOOD_AXE);
        setOrAddItem(inventory, profile, Preference.HOTBAR_WOOL, 9, Material.WOOL);
        player.openInventory(inventory);
        CommandUtils.sendAlertMessage(player, "Your hotbar preferences will be saved once you close this menu.");
    }

    private static void setOrAddItem(Inventory inventory, Profile profile, Preference preference, int defaultValue, Material material) {
        int slot = PreferenceUtils.slotToIndex(profile.getPreference(preference, defaultValue));
        setOrAddItem(inventory, slot, material, preference);
    }

    private static void setOrAddItem(Inventory inventory, int slot, Material material, Preference preference) {
        ItemStack item = new ItemCrafter(material).setTag("hotbar_menu_item", preference.name()).craft();
        if (inventory.getItem(slot) == null) inventory.setItem(slot, item);
        else inventory.addItem(item);
    }

}
