package net.purelic.cgm.utils;

import net.purelic.cgm.core.constants.MatchTeam;
import net.purelic.cgm.core.maps.flag.Flag;
import net.purelic.cgm.core.maps.flag.constants.FlagState;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class ParticleUtils {

    public static float rgbToParticle(int rgb) {
        return Math.max(0.001f, (rgb / 255.0f));
    }

    public static void spawnColoredParticle(Player player, Location location, boolean hideFromPlayer) {
        if (hideFromPlayer) {
            for (Player online : Bukkit.getOnlinePlayers()) {
                if (online == player) continue;
                spawnColoredParticle(ColorConverter.convert(MatchTeam.getTeam(player).getColor()), location, online);
            }
        } else {
            spawnColoredParticle(ColorConverter.convert(MatchTeam.getTeam(player).getColor()), location, player);
        }
    }

    public static void spawnColoredParticle(Color color, Location location) {
        Bukkit.getOnlinePlayers().forEach(player -> spawnColoredParticle(color, location, player));
    }

    public static void spawnColoredParticle(Color color, Location location, Player player) {
        float r = ParticleUtils.rgbToParticle(color.getRed());
        float g = ParticleUtils.rgbToParticle(color.getGreen());
        float b = ParticleUtils.rgbToParticle(color.getBlue());
        spawnColoredParticle(player, location, r, g, b);
    }


    public static void spawnColoredParticle(Location location, float r, float g, float b) {
        Bukkit.getOnlinePlayers().forEach(player -> spawnColoredParticle(player, location, r, g, b));
    }

    public static void spawnColoredParticle(Player player, Location location, float r, float g, float b) {
        player.spigot().playEffect(location, Effect.COLOURED_DUST, 0, 0, r, g, b, 1, 0, 64);
    }

    public static BukkitRunnable getParticleCircle(final Flag flag) {
        final Color color = ColorConverter.convert(flag.getFlagColor());
        final Location flagLoc = flag.getLocation();
        final Location centerLoc = new Location(flagLoc.getWorld(), flagLoc.getBlockX() + 0.5, flagLoc.getBlockY() + 0.1, flagLoc.getBlockZ() + 0.5);

        return new BukkitRunnable() {

            int degree = 0;

            @Override
            public void run() {
                if (!flag.isState(FlagState.DROPPED)) {
                    this.cancel();
                } else {
                    if (this.degree >= 360) this.degree = 0;
                    else this.degree += 10;

                    double radians = Math.toRadians(this.degree);
                    double x = Math.cos(radians) * Flag.RETURN_RADIUS;
                    double z = Math.sin(radians) * Flag.RETURN_RADIUS;

                    centerLoc.add(x, 0, z);
                    ParticleUtils.spawnColoredParticle(color, centerLoc);
                    centerLoc.subtract(x * 2, 0, z * 2);
                    ParticleUtils.spawnColoredParticle(color, centerLoc);
                    centerLoc.add(x, 0, z);
                }
            }

        };
    }

}
