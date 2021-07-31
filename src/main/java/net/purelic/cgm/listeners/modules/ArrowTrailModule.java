package net.purelic.cgm.listeners.modules;

import net.purelic.cgm.CGM;
import net.purelic.cgm.core.constants.MatchState;
import net.purelic.cgm.utils.ParticleUtils;
import net.purelic.cgm.utils.PlayerUtils;
import org.bukkit.Color;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.projectiles.ProjectileSource;
import org.bukkit.scheduler.BukkitRunnable;

public class ArrowTrailModule implements Listener {

    @EventHandler
    public void onProjectileLaunch(ProjectileLaunchEvent event) {
        if (!MatchState.isState(MatchState.STARTED)) {
            event.setCancelled(true);
            return;
        }

        Projectile proj = event.getEntity();
        ProjectileSource projSource = proj.getShooter();

        if (!(projSource instanceof Player)) return;
        if (proj instanceof Arrow) ((Arrow) proj).setCritical(false);

        Player shooter = (Player) projSource;
        new ProjectileTrail(shooter, proj).runTaskTimerAsynchronously(CGM.get(), 0, 1);
    }

    private static class ProjectileTrail extends BukkitRunnable {

        private final Projectile projectile;
        private final float r;
        private final float g;
        private final float b;

        public ProjectileTrail(Player shooter, Projectile projectile) {
            Color color = PlayerUtils.getColorPreference(shooter);
            this.projectile = projectile;
            this.r = ParticleUtils.rgbToParticle(color.getRed());
            this.g = ParticleUtils.rgbToParticle(color.getGreen());
            this.b = ParticleUtils.rgbToParticle(color.getBlue());
        }

        @Override
        public void run() {
            if (this.projectile.isDead() || this.projectile.isOnGround()) {
                this.cancel();
            }

            ParticleUtils.spawnColoredParticle(this.projectile.getLocation(), this.r, this.g, this.b);
        }

    }


}
