package net.purelic.cgm.core.maps.flag.events;

import net.purelic.cgm.core.maps.flag.Flag;
import org.bukkit.event.HandlerList;

public class FlagCollectEvent extends FlagEvent {

    private static final HandlerList HANDLERS = new HandlerList();

    public FlagCollectEvent(Flag flag) {
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

}
