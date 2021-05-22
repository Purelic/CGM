package net.purelic.cgm.events.match;

import net.purelic.cgm.core.constants.MatchState;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class MatchStateChangeEvent extends Event {

    private static final HandlerList HANDLERS = new HandlerList();

    private final MatchState previousState;
    private final MatchState newState;
    private final boolean forced;
    private final int seconds;

    public MatchStateChangeEvent(MatchState previousState, MatchState newState, boolean forced, int seconds) {
        this.previousState = previousState;
        this.newState = newState;
        this.forced = forced;
        this.seconds = seconds;
    }

    public MatchState getPreviousState() {
        return this.previousState;
    }

    public MatchState getNewState() {
        return this.newState;
    }

    public boolean isForced() {
        return this.forced;
    }

    public int getSeconds() {
        return this.seconds;
    }

    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

}
