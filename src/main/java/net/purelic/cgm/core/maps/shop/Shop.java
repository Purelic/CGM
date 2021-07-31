package net.purelic.cgm.core.maps.shop;

import net.purelic.cgm.core.constants.MatchTeam;
import net.purelic.cgm.core.gamemodes.constants.ArmorPiece;
import net.purelic.cgm.core.gamemodes.constants.ArmorType;
import net.purelic.cgm.core.managers.ShopManager;
import net.purelic.cgm.core.maps.shop.constants.TeamUpgrade;
import net.purelic.cgm.utils.ColorConverter;
import net.purelic.commons.utils.ItemCrafter;
import org.bukkit.Bukkit;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class Shop {

    private final String title;
    private final Map<String, ShopItem> items;

    public Shop(String title) {
        this.title = title;
        this.items = new HashMap<>();
    }

    public String getTitle() {
        return this.title;
    }

    public void addItem(ShopItem item) {
        this.items.put(item.getId(), item);
        item.setShop(this);

        ShopItem parent = item.getParent();
        while (parent != null) {
            this.addItem(parent);
            parent = parent.getParent();
        }
    }

    public ShopItem getItem(String id) {
        return this.items.get(id);
    }

    public void open(Player player) {
        this.updateItemColors(player);
        player.openInventory(this.getInventory(player));
    }

    private void updateItemColors(Player player) {
        DyeColor color = ColorConverter.getDyeColor(MatchTeam.getTeam(player));
        this.items.values().stream()
            .filter(ShopItem::isColored)
            .forEach(item -> item.setColor(color));
    }

    private Inventory getInventory(Player player) {
        Inventory inventory = Bukkit.createInventory(null, 54, this.title);

        inventory.setItem(4, new ItemCrafter(Material.BLAZE_POWDER).name("Edit Hotbar Preferences").setTag("hotbar_menu_item", "open").craft());

        this.items.values().stream()
            .filter(item -> !item.hasChild())
            .forEach(item -> inventory.setItem(item.getSlot(), this.getParentItem(player, item).getItemStack(player)));
        TeamUpgrade.applyUpgrades(player, inventory);
        return inventory;
    }

    private ShopItem getParentItem(Player player, ShopItem item) {
        if (!item.hasParent()) return item;

        // TODO This logic could be cleaner
        if (item.isUpgrade()) {
            boolean hasItem = this.hasItem(player, item);

            while (hasItem) {
                if (!item.hasParent()) break;
                item = item.getParent();
                hasItem = this.hasItem(player, item);
            }

            return item;
        } else {
            ShopItem current = item;
            boolean hasItem = false;

            while (item.hasParent() && !hasItem) {
                hasItem = this.hasItem(player, item);
                item = item.getParent();
            }

            if (!hasItem && this.hasItem(player, item) && item.hasChild() && !item.hasParent()) {
                current = item; // max tier
            }

            return hasItem ? (item.hasChild() ? item : current) : current;
        }
    }

    public boolean hasItem(Player player, ShopItem item) {
        if (item.isArmor()) return ArmorType.getCurrentArmorType(player, ArmorPiece.BOOTS) == item.getArmorType();
        else if (item.isUpgrade()) return ShopManager.hasUpgrade(player, item.getUpgrade());
        else return this.inventoryContains(player.getInventory(), item.getItemStack());
    }

    private boolean inventoryContains(Inventory inventory, ItemStack itemStack) {
        return inventory.contains(itemStack) || Arrays.stream(inventory.getContents()).anyMatch(item ->
            item != null && item.getType() == itemStack.getType() && this.hasSameEnchantments(itemStack, item));
    }

    private boolean hasSameEnchantments(ItemStack item1, ItemStack item2) {
        Map<Enchantment, Integer> item1Enchantments = item1.getEnchantments();
        Map<Enchantment, Integer> item2Enchantments = item2.getEnchantments();

        for (Map.Entry<Enchantment, Integer> entry : item1Enchantments.entrySet()) {
            Enchantment enchantment = entry.getKey();
            int level = entry.getValue();

            if (item2Enchantments.containsKey(enchantment)
                && item2Enchantments.get(enchantment) == level) continue;
            return false;
        }

        return true;
    }

}
