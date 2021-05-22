package net.purelic.cgm.listeners.modules.bedwars;

import net.purelic.cgm.core.gamemodes.EnumSetting;
import net.purelic.cgm.core.gamemodes.constants.GameType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.CraftItemEvent;

public class NoCraftingModule implements Listener {

    @EventHandler
    public void onCraftItem(CraftItemEvent event) {
        if (EnumSetting.GAME_TYPE.is(GameType.BED_WARS)) event.setCancelled(true);
    }

}
