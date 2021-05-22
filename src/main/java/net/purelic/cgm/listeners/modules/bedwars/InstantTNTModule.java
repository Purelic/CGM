package net.purelic.cgm.listeners.modules.bedwars;

import net.minecraft.server.v1_8_R3.EntityLiving;
import net.minecraft.server.v1_8_R3.EntityTNTPrimed;
import net.purelic.cgm.core.gamemodes.ToggleSetting;
import net.purelic.commons.Commons;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftLivingEntity;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftTNTPrimed;
import org.bukkit.entity.Player;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.ExplosionPrimeEvent;
import org.bukkit.inventory.ItemStack;

import java.lang.reflect.Field;

public class InstantTNTModule implements Listener {

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        Block block = event.getBlock();

        if (block.getType() != Material.TNT || !ToggleSetting.INSTANT_TNT.isEnabled()) return;

        Player player = event.getPlayer();
        World world = block.getWorld();
        TNTPrimed tnt = world.spawn(block.getLocation().clone().add(0.5, 0.5, 0.5), TNTPrimed.class);
        tnt.setFuseTicks(50);

        if (this.callPrimeEvent(tnt)) {
            event.setCancelled(true);
            world.playSound(tnt.getLocation(), Sound.FUSE, 1, 1);
            this.setTNTSource(tnt, player);

            ItemStack inHand = player.getItemInHand();

            if (inHand.getAmount() == 1) {
                player.setItemInHand(null);
            } else {
                inHand.setAmount(inHand.getAmount() - 1);
            }
        }
    }

    private boolean callPrimeEvent(TNTPrimed tnt) {
        ExplosionPrimeEvent primeEvent = new ExplosionPrimeEvent(tnt);
        Commons.callEvent(primeEvent);

        if (primeEvent.isCancelled()) {
            tnt.remove();
            return false;
        } else {
            return true;
        }
    }

    private void setTNTSource(TNTPrimed tnt, Player player) {
        // Change via NMS the source of the TNT by the player
        EntityLiving nmsEntityLiving = ((CraftLivingEntity) player).getHandle();
        EntityTNTPrimed nmsTNT = ((CraftTNTPrimed) tnt).getHandle();

        try {
            Field sourceField = EntityTNTPrimed.class.getDeclaredField("source");
            sourceField.setAccessible(true);
            sourceField.set(nmsTNT, nmsEntityLiving);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

}
