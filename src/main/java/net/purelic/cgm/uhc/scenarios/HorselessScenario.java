package net.purelic.cgm.uhc.scenarios;

import net.purelic.commons.modules.Module;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.spigotmc.event.entity.EntityMountEvent;

public class HorselessScenario implements Module {

    @EventHandler
    public void onEntityMount(EntityMountEvent event) {
        if (event.getEntity() instanceof Player && event.getMount().getType().equals(EntityType.HORSE)) {
            event.setCancelled(true);
        }
    }

}
