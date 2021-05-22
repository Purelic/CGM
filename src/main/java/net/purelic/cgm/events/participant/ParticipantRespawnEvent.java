package net.purelic.cgm.events.participant;

import net.purelic.cgm.core.match.Participant;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class ParticipantRespawnEvent extends Event {

    private static final HandlerList HANDLERS = new HandlerList();

    private final Participant participant;
    private final Player player;
    private final boolean roundStart;

    public ParticipantRespawnEvent(Participant participant, boolean roundStart) {
        this.participant = participant;
        this.player = participant.getPlayer();
        this.roundStart = roundStart;
    }

    public Participant getParticipant() {
        return this.participant;
    }

    public Player getPlayer() {
        return this.player;
    }

    public boolean isRoundStart() {
        return this.roundStart;
    }

    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

}
