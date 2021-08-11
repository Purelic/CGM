package net.purelic.cgm.core.damage;

/******************************************************************************
 * Copyright (c) 2016.  Written by Devon "Turqmelon": http://turqmelon.com    *
 * For more information, see LICENSE.TXT.                                     *
 ******************************************************************************/

import net.purelic.cgm.core.managers.DamageManger;
import net.purelic.commons.utils.NickUtils;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;

import java.text.DecimalFormat;

public class FallDamageTick extends DamageTick {

    private final double distance;

    public FallDamageTick(double damage, String name, long timestamp, double distance) {
        super(damage, EntityDamageEvent.DamageCause.FALL, name, timestamp);
        this.distance = distance;
    }

    public double getDistance() {
        return distance;
    }

    @Override
    public boolean matches(DamageTick tick) {
        return false;
    }

    @Override
    public String getDeathMessage(Player player) {
        DecimalFormat df = new DecimalFormat("#");
        return DamageManger.ACCENT_COLOR + NickUtils.getDisplayName(player) + DamageManger.BASE_COLOR + " was killed by " + DamageManger.ACCENT_COLOR + df.format(getDistance()) + DamageManger.BASE_COLOR + " block fall";
    }

    @Override
    public String getSingleLineSummary() {
        DecimalFormat df = new DecimalFormat("#");
        return DamageManger.BASE_COLOR + "Fell " + DamageManger.ACCENT_COLOR + df.format(getDistance()) + DamageManger.BASE_COLOR + " blocks";
    }

}