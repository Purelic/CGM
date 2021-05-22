package net.purelic.cgm.events.participant;

import net.purelic.cgm.core.gamemodes.NumberSetting;
import net.purelic.cgm.core.managers.MatchManager;
import net.purelic.cgm.core.match.Participant;
import net.purelic.cgm.utils.MatchUtils;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.event.entity.PlayerDeathEvent;

public class ParticipantDeathEvent extends Event {

    private static final HandlerList HANDLERS = new HandlerList();

    private final Participant participant;
    private final Participant killer;
    private final PlayerDeathEvent deathEvent;
    private final boolean combatLog;
    private final boolean eliminated;
    private final boolean gameTypeWithScoring;

    public ParticipantDeathEvent(Participant participant, Player killer, PlayerDeathEvent deathEvent) {
        this.participant = participant;
        this.killer = killer == null ? null : MatchManager.getParticipant(killer);
        this.deathEvent = deathEvent;
        this.combatLog = deathEvent.getDeathMessage().equals("combat log");
        this.eliminated = NumberSetting.LIVES_PER_ROUND.value() > 0 && participant.getLives() == 1;
        this.gameTypeWithScoring = MatchUtils.hasKillScoring();
    }

    public Participant getParticipant() {
        return this.participant;
    }

    public Participant getKiller() {
        return this.killer;
    }

    public boolean hasKiller() {
        return this.killer != null;
    }

    public PlayerDeathEvent getDeathEvent() {
        return this.deathEvent;
    }

    public boolean isCombatLog() {
        return this.combatLog;
    }

    public boolean isEliminated() {
        return this.eliminated;
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
