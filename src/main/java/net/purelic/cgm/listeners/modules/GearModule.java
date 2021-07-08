package net.purelic.cgm.listeners.modules;

import net.minecraft.server.v1_8_R3.*;
import net.purelic.commons.modules.Module;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftVillager;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerInteractEntityEvent;

public class GearModule implements Module {

    @EventHandler
    public void onCopyVillagerTradeMenu(PlayerInteractEntityEvent event) {
        Player player = event.getPlayer();
        Entity entity = event.getRightClicked();

        if (!(entity instanceof Villager)) return;

        // Make a copy of the villager so multiple players can trade at once
        Villager villager = (Villager) entity;
        EntityPlayer entityPlayer = ((CraftPlayer) player).getHandle();
        EntityVillager original = ((CraftVillager) villager).getHandle();
        EntityVillager copy = new EntityVillager(original.getWorld());

        final NBTTagCompound nbt = new NBTTagCompound();
        original.b(nbt);
        copy.a(nbt);
        copy.dead = false;
        copy.setAge(1);
        copy.a_(entityPlayer); // trading player

        // Adds the "traded_item" nbt to all the sold items
        for (MerchantRecipe recipe : copy.getOffers(entityPlayer)) {
            net.minecraft.server.v1_8_R3.ItemStack item = recipe.getBuyItem3();
            NBTTagCompound itemNbt = item.hasTag() ? item.getTag() : new NBTTagCompound();
            itemNbt.set("traded_item", new NBTTagString("true"));
            item.setTag(itemNbt);
        }

        entityPlayer.openTrade(copy); // open the copied villager
        event.setCancelled(true); // cancel the original open event
    }

}
