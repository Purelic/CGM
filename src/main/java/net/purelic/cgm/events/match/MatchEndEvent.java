package net.purelic.cgm.events.match;

import net.purelic.cgm.core.gamemodes.CustomGameMode;
import net.purelic.cgm.core.maps.CustomMap;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class MatchEndEvent extends Event {

    private static final HandlerList HANDLERS = new HandlerList();

    private final CustomMap map;
    private final CustomGameMode gameMode;
    private final boolean forced;

    public MatchEndEvent(CustomMap map, CustomGameMode gameMode, boolean forced) {
        this.map = map;
        this.gameMode = gameMode;
        this.forced = forced;
    }

    public CustomMap getMap() {
        return this.map;
    }

    public CustomGameMode getGameMode() {
        return this.gameMode;
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
