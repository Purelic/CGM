package net.purelic.cgm.events.participant;

import net.purelic.cgm.core.damage.KillAssist;
import net.purelic.cgm.core.match.Participant;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class ParticipantKillEvent extends Event {

    private static final HandlerList HANDLERS = new HandlerList();

    private final Participant participant;
    private final Participant killed;
    private final KillAssist assist;
    private final boolean elimination;
    private final boolean gameTypeWithScoring;

    public ParticipantKillEvent(Participant participant, Participant killed, KillAssist assist, boolean elimination, boolean gameTypeWithScoring) {
        this.participant = participant;
        this.killed = killed;
        this.assist = assist;
        this.elimination = elimination;
        this.gameTypeWithScoring = gameTypeWithScoring;
    }

    public Participant getParticipant() {
        return this.participant;
    }

    public Participant getKilled() {
        return this.killed;
    }

    public KillAssist getAssist() {
        return this.assist;
    }

    public boolean isElimination() {
        return this.elimination;
    }

    public boolean isGameTypeWithScoring() {
        return this.gameTypeWithScoring;
    }

    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

}
