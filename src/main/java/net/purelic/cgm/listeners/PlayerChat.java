package net.purelic.cgm.listeners;

import net.purelic.cgm.events.modules.ChatEvent;
import net.purelic.commons.Commons;
import net.purelic.commons.profile.preferences.ChatChannel;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

public class PlayerChat implements Listener {

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        event.setCancelled(true);

        Player player = event.getPlayer();
        String message = event.getMessage();
        ChatChannel channel = Commons.getProfile(player).getChatChannel();

        if (channel == ChatChannel.GLOBAL || channel == ChatChannel.ALL) {
            Commons.callEvent(new ChatEvent(player, message, channel == ChatChannel.GLOBAL));
        }
    }

}
