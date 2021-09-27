package net.purelic.cgm.listeners.modules;

import net.purelic.cgm.core.managers.MatchManager;
import net.purelic.commons.modules.Module;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.FishHook;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;

public class RodDurabilityModule implements Module {

    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        // only listening for entities damaged by hooks
        if (!(event.getDamager() instanceof FishHook)) return;

        FishHook hook = (FishHook) event.getDamager();

        // shooter should always be a player
        if (!(hook.getShooter() instanceof Player)) return;

        Player player = (Player) hook.getShooter();

        // check if player is a participant and alive
        if (!(MatchManager.isPlaying(player)
            && MatchManager.getParticipant(player).isAlive())) return;

        // check if item in the player's hand is a rod
        ItemStack item = player.getItemInHand();
        boolean rod = item != null && item.getType() == Material.FISHING_ROD;

        // ignore event if we can't get the rod or if it's unbreakable
        if (!rod || item.getItemMeta().spigot().isUnbreakable()) return;

        short durability = item.getDurability();

        if (durability == 64) { // if item has no durability left, break it
            player.setItemInHand(null);
            player.playSound(player.getLocation(), Sound.ITEM_BREAK, 1F, 1F);
        } else { // reduce durability by 1
            item.setDurability(++durability);
        }
    }

}
