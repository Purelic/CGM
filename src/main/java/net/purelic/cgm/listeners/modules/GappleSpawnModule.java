package net.purelic.cgm.listeners.modules;

import net.purelic.cgm.core.gamemodes.ToggleSetting;
import org.bukkit.Material;
import org.bukkit.entity.Item;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.SpawnerSpawnEvent;

public class GappleSpawnModule implements Listener {

    @EventHandler
    public void onSpawnerSpawn(SpawnerSpawnEvent event) {
        if (event.getEntity() instanceof Item && ((Item) event.getEntity()).getItemStack().getType() == Material.GOLDEN_APPLE) {
            if (ToggleSetting.PLAYER_SWORD_INSTANT_KILL.isEnabled()) {
                event.setCancelled(true);
            }
        }
    }

}
