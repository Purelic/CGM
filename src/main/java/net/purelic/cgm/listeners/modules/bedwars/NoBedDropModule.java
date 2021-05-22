package net.purelic.cgm.listeners.modules.bedwars;

import net.purelic.cgm.core.gamemodes.EnumSetting;
import net.purelic.cgm.core.gamemodes.constants.GameType;
import org.bukkit.Material;
import org.bukkit.entity.Item;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ItemSpawnEvent;

public class NoBedDropModule implements Listener {

    @EventHandler
    public void onItemSpawn(ItemSpawnEvent event) {
        if (!EnumSetting.GAME_TYPE.is(GameType.BED_WARS)) return;

        Item item = event.getEntity();

        if (item.getItemStack().getType() != Material.BED) return;

        item.remove();
    }

}
