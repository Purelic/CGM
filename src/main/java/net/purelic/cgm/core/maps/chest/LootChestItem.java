package net.purelic.cgm.core.maps.chest;

import net.purelic.cgm.core.maps.chest.constants.LootItemLevel;
import net.purelic.commons.utils.ItemCrafter;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class LootChestItem {

    private final LootItemLevel level;
    private final ItemStack item;

    public LootChestItem(LootItemLevel level, Material material) {
        this(level, material, 1);
    }

    public LootChestItem(LootItemLevel level, Material material, int amount) {
        this(level, new ItemStack(material, amount));
    }

    public LootChestItem(LootItemLevel level, ItemCrafter itemCrafter) {
        this(level, itemCrafter.craft());
    }

    public LootChestItem(LootItemLevel level, ItemStack item) {
        this.level = level;
        this.item = item;
    }

    public LootItemLevel getLevel() {
        return this.level;
    }

    public ItemStack getItem() {
        return this.item;
    }

}
