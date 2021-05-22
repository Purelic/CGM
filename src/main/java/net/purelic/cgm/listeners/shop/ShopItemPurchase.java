package net.purelic.cgm.listeners.shop;

import net.purelic.cgm.core.gamemodes.constants.ArmorType;
import net.purelic.cgm.core.managers.KitManager;
import net.purelic.cgm.core.managers.ShopManager;
import net.purelic.cgm.core.maps.shop.ShopItem;
import net.purelic.cgm.core.maps.shop.events.ShopItemPurchaseEvent;
import net.purelic.cgm.core.maps.shop.events.TeamUpgradePurchaseEvent;
import net.purelic.cgm.utils.ColorConverter;
import net.purelic.cgm.utils.SoundUtils;
import net.purelic.commons.Commons;
import net.purelic.commons.profile.Preference;
import net.purelic.commons.profile.Profile;
import net.purelic.commons.Commons;
import net.purelic.commons.utils.ItemCrafter;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.util.ArrayList;
import java.util.List;

public class ShopItemPurchase implements Listener {

    @EventHandler
    private void onShopItemPurchase(ShopItemPurchaseEvent event) {
        Player player = event.getPlayer();
        ShopItem item = event.getItem();

        this.removeItems(player, item);

        if (!item.isUpgrade()) SoundUtils.SFX.SHOP_ITEM_PURCHASED.play(player);

        if (item.isColored()) {
            item.setColor(ColorConverter.getDyeColor(player));
        }

        Profile profile = Commons.getProfile(player);
        PlayerInventory inventory = player.getInventory();
        ItemStack itemStack = item.getItemStack();
        String materialName = itemStack.getType().name();

        if (item.isUpgrade()) {
            Commons.callEvent(new TeamUpgradePurchaseEvent(player, item.getUpgrade()));
        } else if (item.isArmor()) {
            ArmorType type = item.getArmorType();
            this.applyArmorType(inventory, type);
        } else if (item.hasChild()) {
            for (int i = 0; i < inventory.getSize() - 1; i++) {
                ItemStack invItem = inventory.getItem(i);
                ItemStack child = item.getChild().getItemStack();

                if (invItem != null
                        && (invItem.equals(child)
                            || invItem.getType() == child.getType())) {
                    inventory.setItem(i, itemStack);
                    break;
                }
            }
        } else if (materialName.contains("_SWORD") && inventory.contains(Material.WOOD_SWORD)) {
            for (int i = 0; i < inventory.getSize() - 1; i++) {
                ItemStack invItem = inventory.getItem(i);

                if (invItem != null && invItem.getType() == Material.WOOD_SWORD) {
                    inventory.setItem(i, itemStack);
                    break;
                }
            }
        } else if (materialName.contains("_SWORD")) {
            this.replaceOrAddItem(inventory, profile.getPreference(Preference.HOTBAR_SWORD, 1), itemStack);
        } else if (materialName.contains("BOW")) {
            this.replaceOrAddItem(inventory, profile.getPreference(Preference.HOTBAR_BOW, 2), itemStack);
        } else if (materialName.contains("SHEARS")) {
            this.replaceOrAddItem(inventory, profile.getPreference(Preference.HOTBAR_SHEARS, 3), itemStack);
        } else if (materialName.contains("_PICKAXE")) {
            this.replaceOrAddItem(inventory, profile.getPreference(Preference.HOTBAR_PICKAXE, 4), itemStack);
        } else if (materialName.contains("_AXE")) {
            this.replaceOrAddItem(inventory, profile.getPreference(Preference.HOTBAR_AXE, 5), itemStack);
        } else if (materialName.contains("WOOL")) {
            this.replaceOrAddItem(inventory, profile.getPreference(Preference.HOTBAR_WOOL, 9), itemStack);
        } else {
            inventory.addItem(itemStack);
        }

        player.updateInventory();

        if (item.hasParent() || item.hasChild() || item.isUpgrade()) {
            ShopManager.openShop(player);
        }
    }

    private void replaceOrAddItem(Inventory inventory, Object slot, ItemStack item) {
        int index = KitManager.convertSlotToIndex(slot);
        ItemStack itemAt = inventory.getItem(index);
        inventory.setItem(index, item);
        if (itemAt != null) inventory.addItem(itemAt);
    }

    private void removeItems(Player player, ShopItem shopItem) {
        this.removeItems(player, shopItem.getCurrency(), shopItem.getPrice());
    }

    private void removeItems(Player player, Material material, int remove) {
        List<ItemStack> toRemove = new ArrayList<>();
        int removed = 0;

        for (ItemStack item : player.getInventory().getContents()) {
            if (removed == remove) break;

            if (item != null && item.getType() == material) {
                int amount = item.getAmount();
                int remaining = remove - removed;

                if (amount > remaining) {
                    item.setAmount(amount - remaining);
                    break;
                } else {
                    toRemove.add(item);
                    removed += amount;
                }
            }
        }

        toRemove.forEach(item -> player.getInventory().remove(item));
    }

    private void applyArmorType(PlayerInventory inventory, ArmorType armorType) {
        ItemStack[] currentArmor = inventory.getArmorContents();

        ItemStack[] newArmor = new ItemStack[] {
            new ItemCrafter(armorType.getBoots()).setTag("locked", "true").setUnbreakable().craft(),
            new ItemCrafter(armorType.getLeggings()).setTag("locked", "true").setUnbreakable().craft(),
            currentArmor[2], // chestplate
            currentArmor[3], // helmet
        };

        inventory.setArmorContents(newArmor);
    }

}
