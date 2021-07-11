package net.purelic.cgm.events.match;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class SpectatorJoinEvent extends Event {

    private static final HandlerList HANDLERS = new HandlerList();

    private final Player player;
    private final boolean initialJoin;

    public SpectatorJoinEvent(Player player, boolean initialJoin) {
        this.player = player;
        this.initialJoin = initialJoin;
    }

    public Player getPlayer() {
        return this.player;
    }

    public boolean isInitialJoin() {
        return this.initialJoin;
    }

    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

}
