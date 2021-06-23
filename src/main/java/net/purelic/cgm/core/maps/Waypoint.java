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
import net.purelic.commons.utils.lunar.LunarWaypoint;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.EulerAngle;

public class Waypoint {

    private ArmorStand stand;
    private BukkitRunnable runnable;
    private Location particleBase;
    private Color particleColor;
    private LunarWaypoint lunarWaypoint;

    public Waypoint(Flag flag, Participant participant) {
        final Player target = participant.getPlayer();
        final Color color = PlayerUtils.getColorPreference(participant);

        this.lunarWaypoint = new LunarWaypoint(flag.getTitle(), target.getLocation().clone(), color);
        this.lunarWaypoint.show();
        this.lunarWaypoint.remove(target); // don't show it the carrier

        this.runnable = new BukkitRunnable() {

            @Override
            public void run() {
                Location location = target.getLocation().clone();
                lunarWaypoint.setLocation(location);

                for (int i = 3; i <= 28; i++) {
                    Location particleLoc = location.clone().add(0, i, 0);
                    ParticleUtils.spawnColoredParticle(color, particleLoc);
                }
            }

        };

        this.runnable.runTaskTimerAsynchronously(CGM.get(), 0L, 2L);
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

        this.runnable.runTaskTimerAsynchronously(CGM.get(), 0L, 2L);

        this.lunarWaypoint = new LunarWaypoint(label, location, color);
        this.lunarWaypoint.show();
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

    public void setColor(ChatColor chatColor) {
        Color color = ColorConverter.getDyeColor(chatColor).getColor();
        this.particleColor = color;
        if (this.lunarWaypoint != null) this.lunarWaypoint.setColor(color);
    }

    public void setName(String name) {
        this.stand.setCustomName(name.trim());
        if (!this.stand.isCustomNameVisible()) this.stand.setCustomNameVisible(true);
        if (this.lunarWaypoint != null) this.lunarWaypoint.setName(name.trim());
    }

    public void setLocation(String title, Location location) {
        this.setName(title);

        Location newLoc = new Location(location.getWorld(), location.getBlockX(), location.getBlockY(), location.getBlockZ());
        newLoc = newLoc.clone().add(0.5, 0, 0.5);
        if (this.stand != null) this.stand.teleport(newLoc.clone().add(0, 2, 0));

        this.particleBase = newLoc;

        if (this.lunarWaypoint != null) this.lunarWaypoint.setLocation(location);
    }

    public void hide() {
        if (this.stand != null) {
            this.stand.setCustomNameVisible(false);
            this.stand.setCustomName(null);
        }

        TaskUtils.cancelIfRunning(this.runnable);

        if (this.lunarWaypoint != null) this.lunarWaypoint.hide();
    }

    public void destroy() {
        if (this.stand != null) this.stand.remove();
        this.stand = null;
        TaskUtils.cancelIfRunning(this.runnable);
        if (this.lunarWaypoint != null) this.lunarWaypoint.destroy();
    }

    public void showLunarWaypoint(Player player) {
        if (this.lunarWaypoint != null) this.lunarWaypoint.show(player);
    }

}
