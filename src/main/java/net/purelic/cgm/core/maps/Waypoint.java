package net.purelic.cgm.core.maps;

import net.md_5.bungee.api.ChatColor;
import net.purelic.cgm.CGM;
import net.purelic.cgm.core.maps.flag.Flag;
import net.purelic.cgm.core.maps.hill.Hill;
import net.purelic.cgm.core.maps.hill.constants.HillType;
import net.purelic.cgm.core.match.Participant;
import net.purelic.cgm.utils.ColorConverter;
import net.purelic.cgm.utils.ParticleUtils;
import net.purelic.cgm.utils.PlayerUtils;
import net.purelic.commons.utils.TaskUtils;
import org.bukkit.Color;
import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.EulerAngle;

import java.util.ArrayList;
import java.util.List;

public class Waypoint {

    private ArmorStand stand;
    private BukkitRunnable runnable;
    private Location particleBase;
    private Color particleColor;

    public Waypoint(Participant participant) {
        this.runnable = new BukkitRunnable() {

            private final Player target = participant.getPlayer();
            private final Color color = PlayerUtils.getColorPreference(participant);

            @Override
            public void run() {
                Location location = this.target.getLocation().clone();

                for (int i = 3; i <= 28; i++) {
                    Location particleLoc = location.clone().add(0, i, 0);
                    ParticleUtils.spawnColoredParticle(this.color, particleLoc);
                }
            }

        };

        this.runnable.runTaskTimerAsynchronously(CGM.getPlugin(), 0L, 2L);
    }

    public Waypoint(Hill hill) {
        this(
            hill.getCenter(),
            hill.getTitle(),
            hill.getColor(),
            3,
            hill.getType() == HillType.CTF_GOAL
        );
    }

    public Waypoint(Flag flag, Location location) {
        this(
            location,
            flag.getTitle(),
            flag.isNeutral() ? ChatColor.WHITE : flag.getOwner().getColor(),
            2,
            false
        );
    }

    private Waypoint(Location location, String label, ChatColor color, int offset, boolean hideBeam) {
        this(location, label, ColorConverter.convert(color), offset, hideBeam);
    }

    private Waypoint(Location location, String label, Color color, int offset, boolean hideBeam) {
        this.stand = this.getStand(location.clone().add(0, offset, 0));

        this.setName(label);

        if (hideBeam) { // only show name of waypoint
            return;
        }

        this.particleBase = location;
        this.particleColor = color;

        this.runnable = new BukkitRunnable() {
            @Override
            public void run() {
                for (int i = (offset + 1); i <= (offset + 26); i++) {
                    Location particleLoc = particleBase.clone().add(0, i, 0);
                    ParticleUtils.spawnColoredParticle(particleColor, particleLoc);
                }
            }

        };

        this.runnable.runTaskTimerAsynchronously(CGM.getPlugin(), 0L, 2L);
    }

    private ArmorStand getStand(Location location) {
        ArmorStand stand = (ArmorStand) location.getWorld().spawnEntity(location, EntityType.ARMOR_STAND);
        stand.setSmall(true);
        stand.setCanPickupItems(false);
        stand.setMarker(true);
        stand.setGravity(false);
        stand.setVisible(false);
        stand.setRemoveWhenFarAway(false);
        stand.setBasePlate(false);
        stand.setLeftLegPose(new EulerAngle(Math.PI, 0.0D, 0.0D));
        stand.setRightLegPose(new EulerAngle(Math.PI, 0.0D, 0.0D));
        return stand;
    }

    public void update(ChatColor standColor, String name) {
        this.setColor(standColor);
        this.setName(name);
    }

    public void setColor(ChatColor color) {
        this.particleColor = ColorConverter.getDyeColor(color).getColor();
    }

    public void setName(String name) {
        this.stand.setCustomName(name.trim());
        if (!this.stand.isCustomNameVisible()) this.stand.setCustomNameVisible(true);
    }

    public void setLocation(String title, Location location) {
        this.setName(title);

        Location newLoc = new Location(location.getWorld(), location.getBlockX(), location.getBlockY(), location.getBlockZ());
        newLoc = newLoc.clone().add(0.5, 0, 0.5);
        if (this.stand != null) this.stand.teleport(newLoc.clone().add(0, 2, 0));

        this.particleBase = newLoc;
    }

    public void hide() {
        if (this.stand != null) {
            this.stand.setCustomNameVisible(false);
            this.stand.setCustomName(null);
        }

        TaskUtils.cancelIfRunning(this.runnable);
    }

    public void destroy() {
        if (this.stand != null) this.stand.remove();
        this.stand = null;
        TaskUtils.cancelIfRunning(this.runnable);
    }

}
