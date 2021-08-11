package net.purelic.cgm.core.damage;

import net.purelic.cgm.core.managers.DamageManger;
import net.purelic.commons.utils.NickUtils;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;

public class BlockDamageTick extends DamageTick {

    private final Material type;
    private final Location location;

    public BlockDamageTick(double damage, EntityDamageEvent.DamageCause cause, String name, long timestamp, Material type, Location location) {
        super(damage, cause, name, timestamp);
        this.type = type;
        this.location = location;
    }

    public Material getType() {
        return type;
    }

    public Location getLocation() {
        return location;
    }

    @Override
    public boolean matches(DamageTick tick) {
        return (tick instanceof BlockDamageTick) && ((BlockDamageTick) tick).getType().equals(getType()) && DamageManger.samePlace(getLocation(), ((BlockDamageTick) tick).getLocation());
    }

    @Override
    public String getDeathMessage(Player player) {
        return DamageManger.ACCENT_COLOR + NickUtils.getDisplayName(player) + DamageManger.BASE_COLOR + " was killed by " + DamageManger.ACCENT_COLOR + getType().name().replace("_", " ");
    }

    @Override
    public String getSingleLineSummary() {
        return DamageManger.BASE_COLOR + "Hurt by " + DamageManger.ACCENT_COLOR + getType().name().replace("_", " ");
    }

}