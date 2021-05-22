package net.purelic.cgm.events.match;

import net.purelic.cgm.core.constants.MatchTeam;
import net.purelic.cgm.core.managers.MatchManager;
import net.purelic.cgm.core.match.Participant;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class MatchQuitEvent extends Event {

    private static final HandlerList HANDLERS = new HandlerList();

    private final Player player;
    private final Participant participant;
    private final MatchTeam team;
    private final boolean disconnected;

    public MatchQuitEvent(Player player) {
        this(player, false);
    }

    public MatchQuitEvent(Player player, boolean disconnected) {
        this(player, MatchManager.getParticipant(player), disconnected);
    }

    public MatchQuitEvent(Player player, Participant participant, boolean disconnected) {
        this.player = player;
        this.participant = participant;
        this.team = MatchTeam.getTeam(player);
        this.disconnected = disconnected;
    }

    public Player getPlayer() {
        return this.player;
    }

    public Participant getParticipant() {
        return this.participant;
    }

    public MatchTeam getTeam() {
        return this.team;
    }

    public boolean isDisconnected() {
        return this.disconnected;
    }

    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

}
