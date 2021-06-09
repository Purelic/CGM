package net.purelic.cgm.utils;

import net.minecraft.server.v1_8_R3.*;
import net.purelic.commons.utils.VersionUtils;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_8_R3.CraftServer;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack;
import org.bukkit.entity.Player;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class PacketUtils {

    private static Method handle;
    private static Method sendPacket;
    private static Method center;
    private static Method distance;
    private static Method time;
    private static Method movement;
    private static Field player_connection;
    private static Constructor<?> constructor;
    private static Constructor<?> border_constructor;
    private static Object constant;

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

        Packet<?>[] packets = new Packet<?>[] {
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

//
//    public static void addToTab(Player player, Participant hidden) {
//        addToTab(player, getFakePlayer(hidden));
//    }
//
//    private static void addToTab(Player player, EntityPlayer fakePlayer) {
//        PlayerConnection connection = ((CraftPlayer) player).getHandle().playerConnection;
//        connection.sendPacket(new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.ADD_PLAYER, fakePlayer));
//    }
//
//    public static void removeFromTab(Player player) {
//        EntityPlayer entityPlayer = ((CraftPlayer) player).getHandle();
//        for (Player online : Bukkit.getOnlinePlayers()) {
//            if (!online.getUniqueId().equals(player.getUniqueId())) {
//                PlayerConnection connection = ((CraftPlayer) online).getHandle().playerConnection;
//                connection.sendPacket(new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.REMOVE_PLAYER, entityPlayer));
//            }
//        }
//    }
//
//    public static void removeBorder(Player player) {
//        // sendWorldBorderPacket(player, 0);
//    }

    public static void setBorder(Player player) {
        int percent = 100;
        int dist = -10000 * percent + 1300000;
        sendWorldBorderPacket(player, dist);
    }

    private static void sendWorldBorderPacket(Player player, int dist) {
        double oldRadius = 200000.0D;
        double newRadius = 200000.0D;
        long delay = 0L;

        try {
            Object wb = border_constructor.newInstance();
            center.invoke(wb, player.getLocation().getX(), player.getLocation().getY());
            distance.invoke(wb, dist);
            time.invoke(wb, 15);
            movement.invoke(wb, oldRadius, newRadius, delay);

            Object packet = constructor.newInstance(wb, constant);
            sendPacket.invoke(player_connection.get(handle.invoke(player)), packet);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    static {
        try {
            Class<?> enumclass;
            handle = getClass("org.bukkit.craftbukkit", "entity.CraftPlayer").getMethod("getHandle");
            player_connection = getClass("net.minecraft.server", "EntityPlayer").getField("playerConnection");

            for (Method m : getClass("net.minecraft.server", "PlayerConnection").getMethods()) {
                if (m.getName().equals("sendPacket")) {
                    sendPacket = m;


                    break;
                }
            }

            try {
                enumclass = getClass("net.minecraft.server", "EnumWorldBorderAction");
            } catch (ClassNotFoundException x) {
                enumclass = getClass("net.minecraft.server", "PacketPlayOutWorldBorder$EnumWorldBorderAction");
            }

            constructor = getClass("net.minecraft.server", "PacketPlayOutWorldBorder").getConstructor(getClass("net.minecraft.server", "WorldBorder"), enumclass);
            border_constructor = getClass("net.minecraft.server", "WorldBorder").getConstructor();

            Method[] methods = getClass("net.minecraft.server", "WorldBorder").getMethods();

            String setCenter = "setCenter";
            String setWarningDistance = "setWarningDistance";
            String setWarningTime = "setWarningTime";
            String transitionSizeBetween = "transitionSizeBetween";

            if (inClass(methods, setCenter))
                setCenter = "c";
            if (inClass(methods, setWarningDistance))
                setWarningDistance = "c";
            if (inClass(methods, setWarningTime))
                setWarningTime = "b";
            if (inClass(methods, transitionSizeBetween)) {
                transitionSizeBetween = "a";
            }

            center = getClass("net.minecraft.server", "WorldBorder").getMethod(setCenter, double.class, double.class);
            distance = getClass("net.minecraft.server", "WorldBorder").getMethod(setWarningDistance, int.class);
            time = getClass("net.minecraft.server", "WorldBorder").getMethod(setWarningTime, int.class);
            movement = getClass("net.minecraft.server", "WorldBorder").getMethod(transitionSizeBetween, double.class, double.class, long.class);

            for (Object o : enumclass.getEnumConstants()) {
                if (o.toString().equals("INITIALIZE")) {
                    constant = o;
                    break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static boolean inClass(Method[] methods, String methodName) {
        for (Method m : methods) {
            if (m.getName().equals(methodName)) {
                return false;
            }
        }

        return true;
    }

    private static Class<?> getClass(String prefix, String name) throws Exception {
        return Class.forName(prefix + "." + Bukkit.getServer().getClass().getPackage().getName().substring(Bukkit.getServer().getClass().getPackage().getName().lastIndexOf(".") + 1) + "." + name);
    }

}
