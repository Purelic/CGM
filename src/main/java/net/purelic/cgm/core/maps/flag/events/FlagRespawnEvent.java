package net.purelic.cgm.core.maps.flag.events;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.purelic.cgm.core.gamemodes.NumberSetting;
import net.purelic.cgm.core.maps.flag.Flag;
import net.purelic.cgm.events.Broadcastable;
import org.bukkit.event.HandlerList;

public class FlagRespawnEvent extends FlagEvent implements Broadcastable {

    private static final HandlerList HANDLERS = new HandlerList();

    public FlagRespawnEvent(Flag flag) {
        super(flag);
    }

    public Flag getFlag() {
        return this.flag;
    }

    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

    @Override
    public BaseComponent[] getBroadcastMessage() {
        int seconds = this.flag.willRespawnAtHome() ? NumberSetting.FLAG_RESPAWN_DELAY.value() : NumberSetting.FLAG_VOIDED_DELAY.value();
        String time = (seconds == 1 ? " second" : " seconds");
        return new ComponentBuilder(
            FlagEvent.BROADCAST_PREFIX +
                this.flag.getColoredName() +
                " will respawn in " +
                ChatColor.AQUA + seconds +
                ChatColor.RESET + time + "!").create();
    }

}
