package net.purelic.cgm.listeners.modules.bedwars;

import net.purelic.cgm.CGM;
import net.purelic.cgm.core.gamemodes.EnumSetting;
import net.purelic.cgm.core.gamemodes.constants.GameType;
import net.purelic.cgm.listeners.modules.BlockProtectionModule;
import net.purelic.cgm.utils.ColorConverter;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Egg;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.projectiles.ProjectileSource;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class BridgeEggModule implements Listener {

    private final Map<Player, Long> cooldowns = new HashMap<>();
    private final double cooldownTime = 1.0D;
    private final Set<Projectile> eggs = new HashSet<>();

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Action action = event.getAction();
        Player player = event.getPlayer();

        if (!action.name().contains("RIGHT_CLICK")) return;

        ItemStack inHand = player.getItemInHand();

        if (inHand == null
                || inHand.getType() != Material.EGG
                || !inHand.hasItemMeta()) return;

        if (this.cooldowns.containsKey(player)) {
            double timeLeft = (this.cooldowns.get(player) + this.cooldownTime * 1000L) - System.currentTimeMillis();
            if (timeLeft > 0) {
                event.setCancelled(true);
                player.updateInventory();
                return;
            }
        }

        this.cooldowns.put(player, System.currentTimeMillis());
    }

    @EventHandler
    public void onProjectileHit(ProjectileHitEvent event) {
        Entity entity = event.getEntity();
        if (!(entity instanceof Egg)) return;
        this.eggs.remove(entity);
    }

    @EventHandler
    public void onProjectileLaunch(ProjectileLaunchEvent event) {
        Entity entity = event.getEntity();

        if (!(entity instanceof Egg) || !EnumSetting.GAME_TYPE.is(GameType.BED_WARS)) return;

        Egg egg = (Egg) entity;
        ProjectileSource shooter = egg.getShooter();

        if (!(shooter instanceof Player)) return;

        this.eggs.add(egg);
        this.launchBridgeEgg((Player) shooter, egg);
    }

    private void launchBridgeEgg(final Player shooter, final Projectile proj) {
        new BukkitRunnable() {

            private final Location shooterLoc = shooter.getLocation();
            private final byte data = ColorConverter.getDyeColor(shooter).getData();
            private final Material material = Material.WOOL;
            private final int maxTicks = 60;
            private int ticks = 0;

            @Override
            public void run() {
                if (this.ticks >= this.maxTicks
                    || !eggs.contains(proj)) {
                    this.cancel();
                    return;
                }

                Location loc = proj.getLocation().clone().subtract(0, 2, 0);

                if (shooterLoc.distance(loc) > 3.0D) {
                    shooter.playSound(loc, Sound.STEP_WOOL, 10.0F, 1.0F);
                    this.setBlock(loc);
                    this.setBlock(loc.clone().subtract(0.0D, 0.0D, 1.0D));
                    this.setBlock(loc.clone().subtract(1.0D, 0.0D, 0.0D));
                    this.setBlock(loc.clone().add(0.0D, 0.0D, 1.0D));
                    this.setBlock(loc.clone().add(1.0D, 0.0D, 0.0D));
                }

                this.ticks++;
            }

            private void setBlock(Location location) {
                Block block = location.getBlock();
                if (block.getType() != Material.AIR
                        || !BlockProtectionModule.withinBuildLimits(block)
                        || BlockProtectionModule.isSpawnProtected(block)) return;
                block.setType(this.material);
                block.setData(this.data);
                BlockProtectionModule.addBreakableBlock(block);
            }

        }.runTaskTimer(CGM.get(), 0L, 1L);
    }

}
