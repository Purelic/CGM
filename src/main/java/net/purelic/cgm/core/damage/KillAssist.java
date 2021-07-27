package net.purelic.cgm.core.damage;

/******************************************************************************
 * Copyright (c) 2016.  Written by Devon "Turqmelon": http://turqmelon.com    *
 * For more information, see LICENSE.TXT.                                     *
 ******************************************************************************/

import org.bukkit.entity.Player;

public class KillAssist implements Comparable {

    private final Player attacker;
    private final double damage;
    private final int percentage;
    private boolean killer;

    public KillAssist(Player attacker, double damage, int percentage, boolean killer) {
        this.attacker = attacker;
        this.damage = damage;
        this.percentage = percentage;
        this.killer = killer;
    }

    public Player getAttacker() {
        return attacker;
    }

    public double getDamage() {
        return damage;
    }

    public int getPercentage() {
        return percentage;
    }

    public boolean isKiller() {
        return this.killer;
    }

    public void setKiller(boolean killer) {
        this.killer = killer;
    }

    @Override
    public int compareTo(Object o) {
        if ((o instanceof KillAssist)) {
            KillAssist assist = (KillAssist) o;
            if (assist.getPercentage() > getPercentage()) {
                return -1;
            } else if (assist.getPercentage() < getPercentage()) {
                return 1;
            }
        }
        return 0;
    }

}