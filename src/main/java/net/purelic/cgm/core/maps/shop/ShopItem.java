package net.purelic.cgm.core.maps.shop;

import net.purelic.cgm.core.gamemodes.constants.ArmorType;
import net.purelic.cgm.core.maps.shop.constants.TeamUpgrade;
import net.purelic.commons.utils.ItemCrafter;
import org.apache.commons.lang3.text.WordUtils;
import org.bukkit.ChatColor;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class ShopItem {

    private final String id;
    private String description;
    private final int price;
    private final Material currency;
    private final int slot;
    private final boolean colored;
    private final ShopItem parent;
    private final ArmorType armorType;
    private ShopItem child;
    private Shop shop;
    private ItemStack item;

    public ShopItem(ItemStack item, int price, Material currency, int slot) {
        this(item, price, currency, slot, false, null, null);
    }

    public ShopItem(ItemStack item, int price, Material currency, int slot, boolean colored) {
        this(item, price, currency, slot, colored, null, null);
    }

    public ShopItem(ItemStack item, int price, Material currency, int slot, ArmorType armorType) {
        this(item, price, currency, slot, false, armorType, null);
    }

    public ShopItem(ItemStack item, int price, Material currency, int slot, ArmorType armorType, ShopItem parent) {
        this(item, price, currency, slot, false, armorType, parent);
    }

    public ShopItem(ItemStack item, int price, Material currency, int slot, ShopItem parent) {
        this(item, price, currency, slot, false, null, parent);
    }

    public ShopItem(TeamUpgrade upgrade, int price, int slot) {
        this(upgrade, price, slot, null);
    }

    public ShopItem(TeamUpgrade upgrade, int price, int slot, ShopItem parent) {
        this(new ItemCrafter(upgrade.getMaterial()).name(upgrade.getName()).setTag("team_upgrade", upgrade.name()).craft(),
            price, Material.DIAMOND, slot, false, null, parent);
    }

    public ShopItem(ItemStack item, int price, Material currency, int slot, boolean colored, ArmorType armorType, ShopItem parent) {
        this.id = UUID.randomUUID().toString();
        this.description = "";
        this.price = price;
        this.currency = currency;
        this.slot = slot;
        this.colored = colored;
        this.armorType = armorType;
        this.parent = parent;
        this.child = null;
        this.shop = null;
        this.item = this.setItemTags(item);
        if (parent != null) parent.setChild(this);
    }

    private ItemStack setItemTags(ItemStack item) {
        ItemCrafter ic = new ItemCrafter(item)
            .setTag("shop_item", this.id);

        if (this.hasParent()) ic.setTag("parent_id", this.parent.getId());
        if (this.hasChild()) ic.setTag("child_id", this.child.getId());

        return ic.craft();
    }

    private List<String> getLore(Player player) {
        List<String> lore = new ArrayList<>();
        lore.add(this.getCostString(false));

        if (!this.description.isEmpty()) {
            lore.add("");
            String wrapped = WordUtils.wrap(ChatColor.GRAY + this.description, 30, "%%" + ChatColor.GRAY, false);
            lore.addAll(Arrays.asList(wrapped.split("%%")));
        } else if (this.isArmor()) {
            lore.add("");
            String description = "You will permanently respawn with " + this.getName() + "!";
            String wrapped = WordUtils.wrap(ChatColor.GRAY + description, 30, "%%" + ChatColor.GRAY, false);
            lore.addAll(Arrays.asList(wrapped.split("%%")));
        } else if (!this.isUpgrade() && this.isTiered()) {
            lore.add("");
            String description =
                this.hasChild() ?
                    "You will be downgraded to " + this.getChild().getName() + " upon death!" :
                    "You will permanently respawn with " + this.getName() + "!";
            String wrapped = WordUtils.wrap(ChatColor.GRAY + description, 30, "%%" + ChatColor.GRAY, false);
            lore.addAll(Arrays.asList(wrapped.split("%%")));
        }

        if (this.isTiered() || this.isUpgrade()) {
            lore.add("");
        }

        List<ShopItem> children = new ArrayList<>();
        ShopItem child = this.getChild();
        while (child != null) {
            children.add(child);
            child = child.getChild();
        }

        Collections.reverse(children);
        children.forEach(childItem -> lore.add(ChatColor.GREEN + " > " + childItem.getName()));

        if (this.hasChild() || this.hasParent() || this.isUpgrade()) {
            if (this.hasItem(player)) {
                lore.add(ChatColor.GREEN + " > " + this.getName());
            } else {
                lore.add(ChatColor.AQUA + " > " + ChatColor.WHITE + this.getName() + " " + this.getCostString(true));
            }
        }

        ShopItem parent = this.getParent();
        while (parent != null) {
            lore.add(ChatColor.GRAY + " > " + ChatColor.WHITE + parent.getName() + " " + parent.getCostString(true));
            parent = parent.getParent();
        }

        return lore;
    }

    public String getCostString(boolean truncate) {
        if (truncate) {
            return ChatColor.GRAY + "(" + this.getCurrencyColor() + this.price + ChatColor.GRAY + ")";
        }

        return ChatColor.GRAY + "Cost: " +
            this.getCurrencyColor() + this.price + " " +
            WordUtils.capitalizeFully(this.currency.name().replaceAll("_", " ")) +
            (this.price == 1 ? "" : "s");
    }

    private ChatColor getCurrencyColor() {
        if (this.currency == Material.EMERALD) return ChatColor.GREEN;
        else if (this.currency == Material.DIAMOND) return ChatColor.AQUA;
        else if (this.currency == Material.GOLD_INGOT) return ChatColor.YELLOW;
        else return ChatColor.WHITE;
    }

    public String getId() {
        return this.id;
    }

    public ItemStack getItemStack() {
        return this.getItemStack(null, true);
    }

    public ItemStack getItemStack(Player player) {
        return this.getItemStack(player, false);
    }

    private ItemStack getItemStack(Player player, boolean stripped) {
        ItemCrafter ic = new ItemCrafter(this.item);

        if (!stripped) {
            ic.clearLore();
            this.getLore(player).forEach(ic::lore);
            return this.item;
        }

        if (!ic.hasTag("keep_nbt") && !ic.hasTag("locked")) ic.clearTags();
        else ic.removeTag("shop_item");

        ic.clearLore();
        return ic.craft();
    }

    public void setItemStack(ItemStack item) {
        this.item = item;
    }

    public String getName() {
        Material material = this.item.getType();
        String name = ChatColor.stripColor(this.item.getItemMeta().getDisplayName());
        return (name == null ? WordUtils.capitalizeFully(material.name().replaceAll("_", " ")) : name);
    }

    public ShopItem withDescription(String description) {
        this.description = description;
        return this;
    }

    public int getPrice() {
        return this.price;
    }

    public Material getCurrency() {
        return this.currency;
    }

    public int getSlot() {
        return this.slot;
    }

    public boolean isColored() {
        return this.colored;
    }

    public void setColor(DyeColor color) {
        this.item.setDurability(color.getData());
    }

    public ArmorType getArmorType() {
        return this.armorType;
    }

    public boolean isArmor() {
        return this.armorType != null;
    }

    public void setType(Material material) {
        this.item.setType(material);
    }

    public boolean isUpgrade() {
        return new ItemCrafter(this.item).hasTag("team_upgrade");
    }

    public boolean isLimited() {
        return new ItemCrafter(this.item).hasTag("limited");
    }

    public boolean isTiered() {
        return this.hasChild() || this.hasParent();
    }

    public TeamUpgrade getUpgrade() {
        String val = new ItemCrafter(this.item).getTag("team_upgrade");
        return TeamUpgrade.valueOf(val);
    }

    public boolean hasParent() {
        return this.parent != null;
    }

    public ShopItem getParent() {
        return this.parent;
    }

    public boolean hasChild() {
        return this.child != null;
    }

    public ShopItem getChild() {
        return this.child;
    }

    public void setChild(ShopItem item) {
        this.child = item;
        this.item = this.setItemTags(this.item);
    }

    public void setShop(Shop shop) {
        this.shop = shop;
    }

    public boolean hasItem(Player player) {
        return this.shop.hasItem(player, this);
    }

}
