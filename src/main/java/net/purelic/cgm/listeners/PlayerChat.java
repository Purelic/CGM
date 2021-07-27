package net.purelic.cgm.listeners;

import net.purelic.cgm.events.modules.ChatEvent;
import net.purelic.commons.Commons;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

public class PlayerChat implements Listener {

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Commons.callEvent(new ChatEvent(event.getPlayer(), event.getMessage(), false));
        event.setCancelled(true);
    }

//    // necessary for Matrix to detect chat violations
//    @EventHandler (priority = EventPriority.MONITOR)
//    public void onPlayerChatMonitor(AsyncPlayerChatEvent event) {
//        event.setCancelled(true);
//    }

}
