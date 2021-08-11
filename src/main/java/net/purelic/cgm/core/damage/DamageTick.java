package net.purelic.cgm.core.damage;

/******************************************************************************
 * Copyright (c) 2016.  Written by Devon "Turqmelon": http://turqmelon.com    *
 * For more information, see LICENSE.TXT.                                     *
 ******************************************************************************/

import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;

public abstract class DamageTick implements Comparable {

    private double damage;
    private final EntityDamageEvent.DamageCause cause;
    private final String name;
    private long timestamp;

    public DamageTick(double damage, EntityDamageEvent.DamageCause cause, String name, long timestamp) {
        this.damage = damage;
        this.cause = cause;
        this.name = name;
        this.timestamp = timestamp;
    }

    @Override
    public int compareTo(Object o) {
        if ((o instanceof DamageTick)) {
            DamageTick t = (DamageTick) o;
            if (t.getTimestamp() > getTimestamp()) {
                return -1;
            } else if (t.getTimestamp() < getTimestamp()) {
                return 1;
            }
        }
        return 0;
    }

    public abstract boolean matches(DamageTick tick);

    public abstract String getDeathMessage(Player player);

    public abstract String getSingleLineSummary();

    public String timeDiff() {
        long now = System.currentTimeMillis();
        long then = getTimestamp();
        long diff = now - then;
        if (diff < 1500) {
            return "just now";
        } else {
            diff = diff / 1000;
            return diff + "s prior";
        }
    }

    public void setDamage(double damage) {
        this.damage = damage;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public double getDamage() {
        return damage;
    }

    public EntityDamageEvent.DamageCause getCause() {
        return cause;
    }

    public String getName() {
        return name;
    }

    public long getTimestamp() {
        return timestamp;
    }

}