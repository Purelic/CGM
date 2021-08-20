package net.purelic.cgm.core.maps.chest;

import net.purelic.cgm.core.gamemodes.constants.LootType;
import net.purelic.cgm.core.managers.LootManager;
import net.purelic.cgm.core.maps.chest.constants.LootChestTier;
import net.purelic.cgm.core.maps.chest.constants.LootItemLevel;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Chest;
import org.bukkit.inventory.Inventory;

import java.util.*;

public class LootChest {

    private final LootChestTier tier;
    private final Location location;
    private LootType lootType;
    private Inventory inventory;

    public LootChest(String coordsString, LootChestTier tier) {
        double[] coords = this.getCoords(coordsString.split(","));
        this.location = new Location(null, coords[0], coords[1], coords[2]);
        this.tier = tier;
    }

    private double[] getCoords(String[] args) {
        double[] coords = new double[4];

        for (int i = 0; i < args.length; i++) {
            coords[i] = Double.parseDouble(args[i]);
        }

        return coords;
    }

    public void setWorld(World world, LootType lootType) {
        this.location.setWorld(world);
        this.inventory = ((Chest) this.location.getBlock().getState()).getInventory();
        this.lootType = lootType;
        if (this.lootType != LootType.CUSTOM) this.refill();
    }

    public void refill() {
        // Check if chest still exists (e.g. chest was broken)
        if (this.location.getBlock().getType() != Material.CHEST) return;

        // Clear the inventory before filling with new items
        this.inventory.clear();

        int chestValue = 0;
        List<LootChestItem> items = new ArrayList<>();

        // Get one item from each level first
        for (LootItemLevel level : this.tier.getLevels()) {
            LootChestItem item = this.getRandomItem(level);
            if (item == null) continue;
            items.add(item);
            chestValue += level.getItemValue();
        }

        // Fill the chest with random items until the total item value is met
        while (chestValue < this.tier.getTotalItemValue()) {
            LootItemLevel level = this.tier.getRandomLevel();
            LootChestItem item = this.getRandomItem(level);
            if (item == null) continue;
            items.add(item);
            chestValue += level.getItemValue();
        }

        // Shuffle item slots
        List<Integer> slots = Arrays.asList(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26);
        Collections.shuffle(slots);

        // Fill the chests with items in random slots
        for (int i = 0; i < items.size(); i++) {
            this.inventory.setItem(slots.get(i), items.get(i).getItem());
        }
    }

    private LootChestItem getRandomItem(LootItemLevel level) {
        List<LootChestItem> randomItems = LootManager.getItems(this.lootType, level);
        if (randomItems.isEmpty()) return null;
        return randomItems.get(new Random().nextInt(randomItems.size()));
    }

}
