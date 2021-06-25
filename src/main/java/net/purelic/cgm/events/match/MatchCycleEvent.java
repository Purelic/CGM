package net.purelic.cgm.events.match;

import net.purelic.cgm.core.gamemodes.CustomGameMode;
import net.purelic.cgm.core.maps.CustomMap;
import net.purelic.cgm.match.Match;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class MatchCycleEvent extends Event {

    private static final HandlerList HANDLERS = new HandlerList();

    private final Match match;
    private final CustomMap map;
    private final CustomGameMode gameMode;

    public MatchCycleEvent(Match match) {
        this.match = match;
        this.map = match.getMap();
        this.gameMode = match.getGameMode();
    }

    @Deprecated
    public MatchCycleEvent(CustomMap map, CustomGameMode gameMode) {
        this.match = new Match(map, gameMode); // TODO temp
        this.map = map;
        this.gameMode = gameMode;
    }

    public Match getMatch() {
        return this.match;
    }

    public CustomMap getMap() {
        return this.map;
    }

    public CustomGameMode getGameMode() {
        return this.gameMode;
    }

    public boolean hasMap() {
        return this.map != null;
    }

    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

}
