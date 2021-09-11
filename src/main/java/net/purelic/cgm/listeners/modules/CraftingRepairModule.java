package net.purelic.cgm.listeners.modules;

import net.purelic.cgm.core.gamemodes.EnumSetting;
import net.purelic.cgm.core.gamemodes.constants.GameType;
import net.purelic.commons.utils.ItemCrafter;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.inventory.ItemStack;

public class CraftingRepairModule implements DynamicModule {

    // sets max durability of rods and f&s to 4 uses
    @EventHandler
    public void onCraftingPrepare(PrepareItemCraftEvent event) {
        Material material = event.getRecipe().getResult().getType();

        if (material == Material.FISHING_ROD) {
            ItemStack item = new ItemCrafter(Material.FISHING_ROD).durability(61).craft();
            event.getInventory().setResult(item);
        } else if (material == Material.FLINT_AND_STEEL) {
            ItemStack item = new ItemCrafter(Material.FLINT_AND_STEEL).durability(61).craft();
            event.getInventory().setResult(item);
        }
    }

    @Override
    public boolean isValid() {
        return EnumSetting.GAME_TYPE.is(GameType.SURVIVAL_GAMES);
    }

}
