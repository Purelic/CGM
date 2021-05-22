package net.purelic.cgm.core.maps.shop.events;

import net.purelic.cgm.core.constants.MatchTeam;
import net.purelic.cgm.core.maps.shop.constants.TeamUpgrade;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class TrapPurchaseEvent extends Event {

    private static final HandlerList HANDLERS = new HandlerList();

    private final Player player;
    private final MatchTeam team;
    private final TeamUpgrade upgrade;

    public TrapPurchaseEvent(Player player, TeamUpgrade upgrade) {
        this.player = player;
        this.team = MatchTeam.getTeam(player);
        this.upgrade = upgrade;
    }

    public Player getPlayer() {
        return this.player;
    }

    public MatchTeam getTeam() {
        return this.team;
    }

    public TeamUpgrade getUpgrade() {
        return this.upgrade;
    }

    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

}
