package net.purelic.cgm.core.damage;

/******************************************************************************
 * Copyright (c) 2016.  Written by Devon "Turqmelon": http://turqmelon.com    *
 * For more information, see LICENSE.TXT.                                     *
 ******************************************************************************/

import net.purelic.cgm.core.managers.DamageManger;
import net.purelic.commons.utils.NickUtils;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;

import java.text.DecimalFormat;

public class MonsterDamageTick extends DamageTick {

    private LivingEntity entity;
    private double distance;
    private boolean ranged = false;

    public MonsterDamageTick(double damage, String name, long timestamp, LivingEntity entity) {
        super(damage, EntityDamageEvent.DamageCause.ENTITY_ATTACK, name, timestamp);
        this.entity = entity;
    }

    public MonsterDamageTick(double damage, String name, long timestamp, LivingEntity entity, double distance) {
        super(damage, EntityDamageEvent.DamageCause.ENTITY_ATTACK, name, timestamp);
        this.entity = entity;
        this.distance = distance;
        this.ranged = true;
    }

    public double getDistance() {
        return distance;
    }

    public boolean isRanged() {
        return ranged;
    }

    public LivingEntity getEntity() {
        return entity;
    }

    protected String getMessageTemplate(){
        if (isRanged()){
            DecimalFormat df = new DecimalFormat("#.#");
            return DamageManger.BASE_COLOR + "Shot by " + DamageManger.ACCENT_COLOR + "{ATTACKER}" + DamageManger.BASE_COLOR + " from "+
                    DamageManger.ACCENT_COLOR + df.format(getDistance()) + "m " + DamageManger.BASE_COLOR + "away";
        }
        else{
            return DamageManger.BASE_COLOR + "Attacked by " + DamageManger.ACCENT_COLOR +  "{ATTACKER}";

        }
    }

    protected String getDeathMessageTemplate(Player player){
        if (isRanged()){
            DecimalFormat df = new DecimalFormat("#.#");
            return DamageManger.ACCENT_COLOR + NickUtils.getDisplayName(player) + DamageManger.BASE_COLOR + " was killed by " + DamageManger.ACCENT_COLOR + "{KILLER}" + DamageManger.BASE_COLOR + " from "+
                    DamageManger.ACCENT_COLOR + df.format(getDistance()) + "m " + DamageManger.BASE_COLOR + "away";
        }
        else{
            return DamageManger.ACCENT_COLOR + NickUtils.getDisplayName(player) + DamageManger.BASE_COLOR + " was killed by " + DamageManger.ACCENT_COLOR +  "{KILLER}";

        }
    }

    @Override
    public boolean matches(DamageTick tick) {
        return (tick instanceof MonsterDamageTick) && getEntity().getUniqueId().equals(((MonsterDamageTick) tick).getEntity().getUniqueId());
    }

    @Override
    public String getDeathMessage(Player player) {
        return getDeathMessageTemplate(player).replace("{KILLER}", DamageManger.getEntityName(getEntity()));

    }

    @Override
    public String getSingleLineSummary() {
        return getMessageTemplate().replace("{ATTACKER}", DamageManger.getEntityName(getEntity()));
    }
}