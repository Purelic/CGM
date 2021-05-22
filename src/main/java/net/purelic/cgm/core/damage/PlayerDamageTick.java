package net.purelic.cgm.core.damage;

/******************************************************************************
 * Copyright (c) 2016.  Written by Devon "Turqmelon": http://turqmelon.com    *
 * For more information, see LICENSE.TXT.                                     *
 ******************************************************************************/

import net.purelic.cgm.core.managers.DamageManger;
import net.purelic.commons.utils.NickUtils;
import org.bukkit.entity.Player;

import java.text.DecimalFormat;

public class PlayerDamageTick extends MonsterDamageTick {

    public PlayerDamageTick(double damage, String name, long timestamp, Player player) {
        super(damage, name, timestamp, player);
    }

    public PlayerDamageTick(double damage, String name, long timestamp, Player player, double distance) {
        super(damage, name, timestamp, player, distance);
    }

    public Player getPlayer() {
        return (Player)getEntity();
    }

    @Override
    public String getDeathMessage(Player player) {
        DecimalFormat df = new DecimalFormat("#.#");
        return getDeathMessageTemplate(player).replace("{KILLER}", NickUtils.getDisplayName(getPlayer()) + DamageManger.PUNCTUATION_COLOR + "(" + DamageManger.ACCENT_COLOR + df.format(getPlayer().getHealth()) + "❤" + DamageManger.PUNCTUATION_COLOR + ")");
    }

    @Override
    public String getSingleLineSummary() {
        DecimalFormat df = new DecimalFormat("#.#");
        return getMessageTemplate().replace("{ATTACKER}", NickUtils.getDisplayName(getPlayer()) + DamageManger.PUNCTUATION_COLOR + "(" + DamageManger.ACCENT_COLOR + df.format(getPlayer().getHealth()) + "❤" + DamageManger.PUNCTUATION_COLOR + ")");
    }

}