package net.purelic.cgm.utils;

import net.minecraft.server.v1_8_R3.*;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

public class EntityUtils {

    private static EntityHider entityHider;

//    private static void registerEntityHider() {
//        EntityUtils.entityHider = new EntityHider(CGM.get(), EntityHider.Policy.BLACKLIST);
//    }

    public static void hideEntity(Player player, Entity entity) {
        PacketPlayOutEntityDestroy ppp = new PacketPlayOutEntityDestroy(entity.getEntityId());
        ((CraftPlayer) player).getHandle().playerConnection.sendPacket(ppp);

//        if (EntityUtils.entityHider == null) EntityUtils.registerEntityHider();
//        EntityUtils.entityHider.hideEntity(player, entity);
//
//        PacketPlayOutEntityDestroy packet = new PacketPlayOutEntityDestroy(entity.getEntityId());
//        ((CraftPlayer) player).getHandle().playerConnection.sendPacket(packet);
    }

//    public static void showEntity(Player player, Entity entity) {
//        if (EntityUtils.entityHider == null) EntityUtils.registerEntityHider();
//        EntityUtils.entityHider.showEntity(player, entity);
//    }

    public static void teleportEntity(Player player, Entity entity, Location location) {
        PacketPlayOutEntityTeleport ppp = new PacketPlayOutEntityTeleport(
            entity.getEntityId(),
            MathHelper.floor(location.getX() * 32.0D),
            MathHelper.floor(location.getY() * 32.0D),
            MathHelper.floor(location.getZ() * 32.0D),
            (byte) ((int) (location.getYaw() * 256.0F / 360.0F)),
            (byte) ((int) (location.getPitch() * 256.0F / 360.0F)),
            entity.isOnGround()
        );
        // PacketPlayOutEntityTeleport ppp = new PacketPlayOutEntityTeleport(((CraftEntity) entity).getHandle());
        ((CraftPlayer) player).getHandle().playerConnection.sendPacket(ppp);
    }

    public static void showEntity(Player player, Entity entity) {
        PacketPlayOutSpawnEntity ppp = new PacketPlayOutSpawnEntity(((CraftEntity) entity).getHandle(), 0);
        ((CraftPlayer) player).getHandle().playerConnection.sendPacket(ppp);
    }

    public static EntityArmorStand spawnArmorStand(Player player, Location loc) {
        WorldServer s = ((CraftWorld) player.getWorld()).getHandle();
        EntityArmorStand stand = new EntityArmorStand(s);

        stand.setLocation(loc.getX(), loc.getY(), loc.getZ(), 0, 0);
        stand.setGravity(false);
        stand.setInvisible(true);
        stand.setBasePlate(false);
        stand.setArms(false);
        stand.setSmall(true);
        // stand.setMarker(true);

        PacketPlayOutSpawnEntityLiving packet = new PacketPlayOutSpawnEntityLiving(stand);
        ((CraftPlayer) player).getHandle().playerConnection.sendPacket(packet);

        return stand;
    }

    public static void destroyArmorStand(Player player, EntityArmorStand entity) {
        PacketPlayOutEntityDestroy ppp = new PacketPlayOutEntityDestroy(entity.getId());
        ((CraftPlayer) player).getHandle().playerConnection.sendPacket(ppp);
    }

    public static void attachEntity(Player player, EntityArmorStand entity) {
        PacketPlayOutAttachEntity packet = new PacketPlayOutAttachEntity(
            0,
            ((CraftPlayer) player).getHandle(),
            entity
        );

        ((CraftPlayer) player).getHandle().playerConnection.sendPacket(packet);
    }

    public static int getDistanceFromGround(Entity entity) {
        Location location = entity.getLocation().clone();
        int y = location.getBlockY();
        int distance = 0;

        for (int i = y; i >= 0; i--) {
            location.setY(i);
            Block block = location.getBlock();
            if (block.isLiquid() || block.getType().isSolid()) break;
            distance++;
        }

        return distance;
    }

    public static boolean isOnGround(Player player) {
        return ((Entity) player).isOnGround();
    }

    public static void setAi(Entity entity, boolean enabled) {
        net.minecraft.server.v1_8_R3.Entity nmsEn = ((CraftEntity) entity).getHandle();
        NBTTagCompound comp = new NBTTagCompound();
        nmsEn.c(comp);
        comp.setByte("NoAI", (byte) 1);
        nmsEn.f(comp);
        nmsEn.b(true);
    }

}
