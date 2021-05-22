package net.purelic.cgm.events.participant;

import net.purelic.cgm.core.match.Participant;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class ParticipantEliminateEvent extends Event {

    private static final HandlerList HANDLERS = new HandlerList();

    private final Participant participant;
    private final Player player;
    private final boolean combatLog;

    public ParticipantEliminateEvent(Participant participant, boolean combatLog) {
        this.participant = participant;
        this.player = participant.getPlayer();
        this.combatLog = combatLog;
    }

    public Participant getParticipant() {
        return this.participant;
    }

    public Player getPlayer() {
        return this.player;
    }

    public boolean isCombatLog() {
        return this.combatLog;
    }

    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

}
