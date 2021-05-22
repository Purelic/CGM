package net.purelic.cgm.events.match;

import net.purelic.cgm.CGM;
import net.purelic.cgm.core.constants.MatchTeam;
import net.purelic.cgm.core.gamemodes.EnumSetting;
import net.purelic.cgm.core.gamemodes.constants.TeamType;
import net.purelic.cgm.core.managers.MatchManager;
import net.purelic.cgm.core.match.Participant;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import java.util.List;

public class RoundEndEvent extends Event {

    private static final HandlerList HANDLERS = new HandlerList();

    private final Participant winner;
    private final MatchTeam winnerTeam;

    public RoundEndEvent(Participant winner) {
        this.winner = winner;
        this.winnerTeam = EnumSetting.TEAM_TYPE.is(TeamType.SOLO) ? null : MatchTeam.getTeam(winner.getPlayer());
    }

    public RoundEndEvent() {
        MatchManager matchManager = CGM.getPlugin().getMatchManager();
        if (matchManager.allEliminated()) {
            if (EnumSetting.TEAM_TYPE.is(TeamType.SOLO)) {
                Participant participant = matchManager.getLastParticipantAlive();
                List<Participant> ordered = MatchManager.getOrderedParticipants(false);
                this.winner = participant != null || ordered.size() == 0 ? participant : ordered.get(0);
                this.winnerTeam = null;
            } else {
                this.winner = null;
                this.winnerTeam = matchManager.getLastTeamAlive();
            }
        } else {
            if (EnumSetting.TEAM_TYPE.is(TeamType.SOLO)) {
                this.winner = MatchManager.getTopParticipant(false);
                this.winnerTeam = null;
            } else {
                this.winner = null;
                this.winnerTeam = MatchTeam.getTopTeam(false);
            }
        }
    }

    public Participant getWinner() {
        return this.winner;
    }

    public MatchTeam getWinnerTeam() {
        return this.winnerTeam;
    }

    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

}
