package net.purelic.cgm.utils;

import net.minecraft.server.v1_8_R3.*;
import net.purelic.commons.utils.VersionUtils;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_8_R3.CraftServer;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack;
import org.bukkit.entity.Player;

public class PacketUtils {

    public static void sendPackets(Player player, Packet<?>... packets) {
        PlayerConnection connection = ((CraftPlayer) player).getHandle().playerConnection;
        for (Packet<?> packet : packets) connection.sendPacket(packet);
    }

    private static EntityPlayer getFakePlayer(Player player) {
        // Create a fake copy of the player
        WorldServer nmsWorld = ((CraftWorld) player.getWorld()).getHandle();

        return new EntityPlayer(
            ((CraftServer) Bukkit.getServer()).getServer(),
            nmsWorld,
            ((CraftPlayer) player).getHandle().getProfile(),
            new PlayerInteractManager(nmsWorld)
        );
    }

    public static void playDeathAnimation(Player player) {
        EntityPlayer fakePlayer = getFakePlayer(player);

        // Match the location of the player
        fakePlayer.setLocation(
            player.getLocation().getX(),
            player.getLocation().getY(),
            player.getLocation().getZ(),
            player.getLocation().getYaw(),
            player.getLocation().getPitch()
        );

        DataWatcher watcher = fakePlayer.getDataWatcher();
        watcher.watch(10, (byte) 127);

        Packet<?>[] packets = new Packet<?>[]{
            // Spawn the fake player
            new PacketPlayOutNamedEntitySpawn(fakePlayer),

            // Show the second skin layer
            new PacketPlayOutEntityMetadata(fakePlayer.getId(), watcher, true),

            // Kill the fake player/play death animation
            new PacketPlayOutEntityStatus(fakePlayer, (byte) 3),

            // Copy the player's armor onto the fake entity
            new PacketPlayOutEntityEquipment(fakePlayer.getId(), 4, CraftItemStack.asNMSCopy(player.getInventory().getHelmet())),
            new PacketPlayOutEntityEquipment(fakePlayer.getId(), 3, CraftItemStack.asNMSCopy(player.getInventory().getChestplate())),
            new PacketPlayOutEntityEquipment(fakePlayer.getId(), 2, CraftItemStack.asNMSCopy(player.getInventory().getLeggings())),
            new PacketPlayOutEntityEquipment(fakePlayer.getId(), 1, CraftItemStack.asNMSCopy(player.getInventory().getBoots()))
        };

        for (Player online : Bukkit.getOnlinePlayers()) {
            if (online == player) {
                // Don't play the death animation to the player that died
                continue;
            }

            if (VersionUtils.getProtocol(online).value() > VersionUtils.Protocol.MINECRAFT_1_8.value()) {
                // This death animation doesn't work properly for 1.9+
                // The fake player is spawned but the death animation doesn't play so the entity doesn't disappear
                continue;
            }

            // Send fake player/death animation packets
            sendPackets(online, packets);
        }
    }

}
