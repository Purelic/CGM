package net.purelic.cgm.events.match;

import net.purelic.cgm.core.constants.MatchTeam;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class MatchJoinEvent extends Event {

    private static final HandlerList HANDLERS = new HandlerList();

    private final Player player;
    private final MatchTeam team;
    private final boolean forced;
    private final boolean firstJoin;

    public MatchJoinEvent(Player player, MatchTeam team, boolean forced, boolean firstJoin) {
        this.player = player;
        this.team = team;
        this.forced = forced;
        this.firstJoin = firstJoin;
    }

    public Player getPlayer() {
        return this.player;
    }

    public MatchTeam getTeam() {
        return this.team;
    }

    public boolean hasTeam() {
        return this.team != null;
    }

    public boolean isForced() {
        return this.forced;
    }

    public boolean isFirstJoin() {
        return this.firstJoin;
    }

    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

}
