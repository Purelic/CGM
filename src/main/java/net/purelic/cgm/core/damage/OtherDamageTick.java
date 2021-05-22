package net.purelic.cgm.core.damage;

/******************************************************************************
 * Copyright (c) 2016.  Written by Devon "Turqmelon": http://turqmelon.com    *
 * For more information, see LICENSE.TXT.                                     *
 ******************************************************************************/

import net.purelic.cgm.core.managers.DamageManger;
import net.purelic.commons.utils.NickUtils;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;

public class OtherDamageTick extends DamageTick {

    public OtherDamageTick(double damage, EntityDamageEvent.DamageCause cause, String name, long timestamp) {
        super(damage, cause, name, timestamp);
    }

    @Override
    public boolean matches(DamageTick tick) {
        return (tick instanceof OtherDamageTick) && tick.getName().equals(getName());
    }

    @Override
    public String getDeathMessage(Player player) {
        return DamageManger.ACCENT_COLOR + NickUtils.getDisplayName(player) + DamageManger.BASE_COLOR + " was killed by " + DamageManger.ACCENT_COLOR + getName();
    }

    @Override
    public String getSingleLineSummary() {
        return DamageManger.ACCENT_COLOR + getName() + DamageManger.BASE_COLOR + " damage";
    }
}