package net.purelic.cgm.listeners.modules.bedwars;

import net.purelic.cgm.CGM;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

public class NoBottleModule implements Listener {

    @EventHandler
    public void onPlayerItemConsume(PlayerItemConsumeEvent event) {
        final Player player = event.getPlayer();

        if (event.getItem().getType() == Material.POTION) {
            new BukkitRunnable() {
                @Override
                public void run() {
                    player.setItemInHand(new ItemStack(Material.AIR));
                }
            }.runTaskLaterAsynchronously(CGM.get(), 1L);
        }
    }

}
