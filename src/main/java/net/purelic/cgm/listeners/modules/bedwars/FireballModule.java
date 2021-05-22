package net.purelic.cgm.listeners.modules.bedwars;

import net.purelic.cgm.CGM;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Fireball;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.util.Vector;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

public class FireballModule implements Listener {

    private static Field fieldFireballDirX;
    private static Field fieldFireballDirY;
    private static Field fieldFireballDirZ;

    private static Method craftFireballHandle;

    private final Map<Player, Long> cooldowns = new HashMap<>();
    private final double cooldownTime = 0.5D;

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Action action = event.getAction();
        Player player = event.getPlayer();

        if (!action.name().contains("RIGHT_CLICK")) return;

        ItemStack inHand = player.getItemInHand();

        if (inHand == null
                || inHand.getType() != Material.FIREBALL
                || !inHand.hasItemMeta()) return;

        event.setCancelled(true);

        if (this.cooldowns.containsKey(player)) {
            double timeLeft = (this.cooldowns.get(player) + this.cooldownTime * 1000L) - System.currentTimeMillis();
            if (timeLeft > 0) return;
        }

        if (inHand.getAmount() == 1) {
            player.setItemInHand(null);
        } else {
            inHand.setAmount(inHand.getAmount() - 1);
        }

        this.launchFireball(player);
        this.cooldowns.put(player, System.currentTimeMillis());
        player.updateInventory();
    }

    public void launchFireball(Player player) {
        Fireball fireball = player.launchProjectile(Fireball.class);
        // fireball.setVelocity(player.getLocation().getDirection().multiply(0.5));
        setDirection(fireball, player.getLocation().getDirection());

        // fireball.setCustomName(ChatColor.BOLD + "༼ つ ◕_◕ ༽つ");
        fireball.setCustomName(ChatColor.BOLD + "¯\\_(ツ)_/¯");
        fireball.setCustomNameVisible(true);
        fireball.setMetadata("fireball", new FixedMetadataValue(CGM.getPlugin(), "fireball"));
    }

    static {
        String version = Bukkit.getServer().getClass().getPackage().getName().replace(".", ",").split(",")[3] + ".";
        String nmsFireball = "net.minecraft.server." + version + "EntityFireball";
        String craftFireball = "org.bukkit.craftbukkit." + version + "entity.CraftFireball";
        try {
            Class<?> fireballClass = Class.forName(nmsFireball);

            //should be accessible by default.
            fieldFireballDirX = fireballClass.getDeclaredField("dirX");
            fieldFireballDirY = fireballClass.getDeclaredField("dirY");
            fieldFireballDirZ = fireballClass.getDeclaredField("dirZ");

            craftFireballHandle = Class.forName(craftFireball).getDeclaredMethod("getHandle");

        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            Bukkit.shutdown();
        } catch (NoSuchFieldException | NoSuchMethodException e) {
            e.printStackTrace();
        }
    }


    public static Fireball setDirection(Fireball fireball, Vector direction) {
        try {
            Object handle = craftFireballHandle.invoke(fireball);
            fieldFireballDirX.set(handle, direction.getX() * 0.10D);
            fieldFireballDirY.set(handle, direction.getY() * 0.10D);
            fieldFireballDirZ.set(handle, direction.getZ() * 0.10D);

        } catch (IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }
        return fireball;
    }

}
