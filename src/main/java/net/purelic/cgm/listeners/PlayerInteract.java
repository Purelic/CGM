package net.purelic.cgm.listeners;

import net.purelic.cgm.events.match.MatchVoteEvent;
import net.purelic.commons.Commons;
import net.purelic.commons.utils.ItemCrafter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

public class PlayerInteract implements Listener {

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Action action = event.getAction();

        if (action == Action.RIGHT_CLICK_AIR || action == Action.RIGHT_CLICK_BLOCK) {
            Player player = event.getPlayer();
            ItemStack item = event.getItem();

            if (item == null) return;

            ItemCrafter itemCrafter = new ItemCrafter(item);

            if (itemCrafter.hasTag("setnext")) {
                Bukkit.dispatchCommand(player, "setnext");
            } else if (itemCrafter.hasTag("toggles")) {
                Bukkit.dispatchCommand(player, "toggles");
            } else if (itemCrafter.hasTag("map")) {
                Commons.callEvent(new MatchVoteEvent(player, itemCrafter.getTag("map")));
            } else if (itemCrafter.hasTag("join")) {
                Bukkit.dispatchCommand(player, "join");
            } else if (itemCrafter.hasTag("toggle_spectators")) {
                Bukkit.dispatchCommand(player, "toggle specs");
            }
        }
    }

}
