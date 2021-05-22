package net.purelic.cgm.events.match;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class MatchVoteEvent extends Event {

    private static final HandlerList HANDLERS = new HandlerList();

    private final Player player;
    private final String map;

    public MatchVoteEvent(Player player, String map) {
        this.player = player;
        this.map = map;
    }

    public Player getPlayer() {
        return this.player;
    }

    public String getMap() {
        return this.map;
    }

    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

}
