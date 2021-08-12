package net.purelic.cgm.listeners.modules;

import io.netty.channel.*;
import net.minecraft.server.v1_8_R3.PacketPlayOutEntityVelocity;
import net.purelic.cgm.commands.toggles.ToggleKnockbackCommand;
import net.purelic.cgm.events.match.MatchJoinEvent;
import net.purelic.cgm.events.match.MatchQuitEvent;
import net.purelic.cgm.events.match.RoundEndEvent;
import net.purelic.cgm.utils.EntityUtils;
import net.purelic.cgm.utils.PlayerUtils;
import net.purelic.commons.modules.Module;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.util.Vector;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class KnockbackModule implements Module {

    private static KnockbackModule instance;

    private final Map<UUID, Hit> damaged;
    private double groundHorizontal;
    private double groundVertical;
    private double airHorizontal;
    private double airVertical;
    private double sprintHorizontal;
    private double sprintVertical;
    private double sprintYawFactor; // how much the player's yaw should affect the kb (0 - 1.0)

    public KnockbackModule() {
        instance = this;
        this.damaged = new ConcurrentHashMap<>();
        this.groundHorizontal = 0.42;
        this.groundVertical = 0.3;
        this.airHorizontal = 0.42;
        this.airVertical = 0.3;
        this.sprintHorizontal = 2;
        this.sprintVertical = 1.3;
        this.sprintYawFactor = 0.5;
    }

    public double getGroundHorizontal() {
        return this.groundHorizontal;
    }

    public void setGroundHorizontal(double groundHorizontal) {
        this.groundHorizontal = groundHorizontal;
    }

    public double getGroundVertical() {
        return this.groundVertical;
    }

    public void setGroundVertical(double groundVertical) {
        this.groundVertical = groundVertical;
    }

    public double getAirHorizontal() {
        return this.airHorizontal;
    }

    public void setAirHorizontal(double airHorizontal) {
        this.airHorizontal = airHorizontal;
    }

    public double getAirVertical() {
        return this.airVertical;
    }

    public void setAirVertical(double airVertical) {
        this.airVertical = airVertical;
    }

    public double getSprintHorizontal() {
        return this.sprintHorizontal;
    }

    public void setSprintHorizontal(double sprintHorizontal) {
        this.sprintHorizontal = sprintHorizontal;
    }

    public double getSprintVertical() {
        return this.sprintVertical;
    }

    public void setSprintVertical(double sprintVertical) {
        this.sprintVertical = sprintVertical;
    }

    public double getSprintYawFactor() {
        return this.sprintYawFactor;
    }

    public void setSprintYawFactor(double sprintYawFactor) {
        this.sprintYawFactor = sprintYawFactor;
    }

    public static KnockbackModule get() {
        return instance;
    }

    @EventHandler(ignoreCancelled = true)
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player) || !(event.getDamager() instanceof Player)) return;

        Player attacker = (Player) event.getDamager();
        Player victim = (Player) event.getEntity();

        if (PlayerUtils.isObserving(attacker) || PlayerUtils.isObserving(victim)) return;

        Vector direction = new Vector(victim.getLocation().getX() - attacker.getLocation().getX(), 0.0D, victim.getLocation().getZ() - attacker.getLocation().getZ());
        int kbEnchantLevel = attacker.getItemInHand().getEnchantmentLevel(Enchantment.KNOCKBACK);
        Hit hit = new Hit(direction.normalize(), attacker.isSprinting(), kbEnchantLevel, attacker, System.currentTimeMillis());

        this.damaged.put(victim.getUniqueId(), hit);
    }

    private void updateVelocity(Player player, PacketPlayOutEntityVelocity packet) {
        if (!this.damaged.containsKey(player.getUniqueId()) || !ToggleKnockbackCommand.knockback) return;

        Hit hit = this.damaged.get(player.getUniqueId());
        this.damaged.remove(player.getUniqueId());

        if (System.currentTimeMillis() - hit.getTime() > 1000L) return;

        Vector initKb = hit.getDirection().setY(1);
        Vector attackerYaw = hit.getAttacker().getLocation().getDirection().normalize().setY(1);

        if (hit.isSprintKb()) {
            initKb = initKb.clone().multiply(1.0D - this.getSprintYawFactor()).add(attackerYaw.clone().multiply(this.getSprintYawFactor()));
            initKb.setX(initKb.getX() * this.getSprintHorizontal());
            initKb.setY(initKb.getY() * this.getSprintVertical());
            initKb.setZ(initKb.getZ() * this.getSprintHorizontal());
        }

        double horizontalMultiplier = EntityUtils.isOnGround(player) ? this.getGroundHorizontal() : this.getAirHorizontal();
        double verticalMultiplier = EntityUtils.isOnGround(player) ? this.getGroundVertical() : this.getAirVertical();

        Vector resultKb = new Vector(
            initKb.getX() * horizontalMultiplier,
            initKb.getY() * verticalMultiplier,
            initKb.getZ() * horizontalMultiplier
        );

        if (hit.getKbEnchantLevel() < 0) {
            double additionalKb = hit.getKbEnchantLevel() * 0.45D;
            double distance = Math.sqrt(Math.pow(resultKb.getX(), 2.0D) + Math.pow(resultKb.getZ(), 2.0D));

            double ratioX = resultKb.getX() / distance;
            ratioX = ratioX * additionalKb + resultKb.getX();

            double ratioZ = resultKb.getZ() / distance;
            ratioZ = ratioZ * additionalKb + resultKb.getZ();

            resultKb = new Vector(ratioX, resultKb.getY(), ratioZ);
        }

        this.updatePacket(packet, resultKb.getX(), resultKb.getY(), resultKb.getZ());
    }

    private void updatePacket(PacketPlayOutEntityVelocity packet, double x, double y, double z) {
        double var8 = 3.9D;

        if (x < -var8) {
            x = -var8;
        }

        if (y < -var8) {
            y = -var8;
        }

        if (z < -var8) {
            z = -var8;
        }

        if (x > var8) {
            x = var8;
        }

        if (y > var8) {
            y = var8;
        }

        if (z > var8) {
            z = var8;
        }

        try {
            Field packetX = PacketPlayOutEntityVelocity.class.getDeclaredField("b");
            packetX.setAccessible(true);
            packetX.setInt(packet, (int) (x * 8000.0D));

            Field packetY = PacketPlayOutEntityVelocity.class.getDeclaredField("c");
            packetY.setAccessible(true);
            packetY.setInt(packet, (int) (y * 8000.0D));

            Field packetZ = PacketPlayOutEntityVelocity.class.getDeclaredField("d");
            packetZ.setAccessible(true);
            packetZ.setInt(packet, (int) (z * 8000.0D));
        } catch (Exception ignored) {
        }
    }

    @EventHandler
    public void onMatchJoin(MatchJoinEvent event) {
        this.injectPlayer(event.getPlayer());
    }

    private void injectPlayer(final Player player) {
        ChannelDuplexHandler channelDuplexHandler = new ChannelDuplexHandler() {

            @Override
            public void channelRead(ChannelHandlerContext channelHandlerContext, Object packet) throws Exception {
                if (packet instanceof PacketPlayOutEntityVelocity) {
                    PacketPlayOutEntityVelocity entityVelocityPacket = (PacketPlayOutEntityVelocity) packet;
                    updateVelocity(player, entityVelocityPacket);
                }

                super.channelRead(channelHandlerContext, packet);
            }

            @Override
            public void write(ChannelHandlerContext channelHandlerContext, Object packet, ChannelPromise channelPromise) throws Exception {
                super.write(channelHandlerContext, packet, channelPromise);
            }

        };

        ChannelPipeline pipeline = ((CraftPlayer) player).getHandle().playerConnection.networkManager.channel.pipeline();
        pipeline.addBefore("packet_handler", player.getName(), channelDuplexHandler);
    }

    @EventHandler
    public void onMatchQuit(MatchQuitEvent event){
        Player player = event.getPlayer();
        this.removePlayer(player);
        this.damaged.remove(player.getUniqueId());
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event){
        Player player = event.getPlayer();
        this.removePlayer(player);
        this.damaged.remove(player.getUniqueId());
    }

    private void removePlayer(Player player) {
        Channel channel = ((CraftPlayer) player).getHandle().playerConnection.networkManager.channel;
        channel.eventLoop().submit(() -> {
            channel.pipeline().remove(player.getName());
            return null;
        });
    }

    @EventHandler
    public void onRoundEnd(RoundEndEvent event) {
        this.damaged.clear();
    }

    private static class Hit {

        private final Vector direction;
        private final boolean sprintKb;
        private final int kbEnchantLevel;
        private final Player attacker;
        private final long time;

        public Hit(Vector direction, boolean sprintKb, int kbEnchantLevel, Player attacker, long time) {
            this.direction = direction;
            this.sprintKb = sprintKb;
            this.kbEnchantLevel = kbEnchantLevel;
            this.attacker = attacker;
            this.time = time;
        }

        public Vector getDirection() {
            return this.direction;
        }

        public boolean isSprintKb() {
            return this.sprintKb;
        }

        public int getKbEnchantLevel() {
            return this.kbEnchantLevel;
        }

        public Player getAttacker() {
            return this.attacker;
        }

        public long getTime() {
            return this.time;
        }

    }

}
