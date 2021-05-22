package net.purelic.cgm.core.maps.shop.events;

import net.md_5.bungee.api.chat.BaseComponent;
import net.purelic.cgm.core.maps.bed.Bed;
import net.purelic.cgm.events.Broadcastable;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class TrapTriggeredEvent extends Event implements Broadcastable {

    private static final HandlerList HANDLERS = new HandlerList();

    private final Bed bed;
    private final Player player;

    public TrapTriggeredEvent(Bed bed, Player player) {
        this.bed = bed;
        this.player = player;
    }

    public Bed getBed() {
        return this.bed;
    }

    public Player getTriggeredBy() {
        return this.player;
    }

    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

    @Override
    public BaseComponent[] getBroadcastMessage() {
        return new BaseComponent[0];
    }

}
