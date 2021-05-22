package net.purelic.cgm.events.participant;

import net.purelic.cgm.core.match.Participant;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class ParticipantScoreEvent extends Event {

    private static final HandlerList HANDLERS = new HandlerList();

    private final Participant participant;
    private final int points;

    public ParticipantScoreEvent(Participant participant, int points) {
        this.participant = participant;
        this.points = points;
    }

    public Participant getParticipant() {
        return this.participant;
    }

    public int getPoints() {
        return this.points;
    }

    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

}
