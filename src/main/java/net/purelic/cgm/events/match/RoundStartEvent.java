package net.purelic.cgm.events.match;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class RoundStartEvent extends Event {

    private static final HandlerList HANDLERS = new HandlerList();

    private final boolean forced;

    public RoundStartEvent(boolean forced) {
        this.forced = forced;
    }

    public boolean isForced() {
        return this.forced;
    }

    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

}
