package net.purelic.cgm.events.match;

import net.purelic.cgm.CGM;
import net.purelic.cgm.core.constants.MatchTeam;
import net.purelic.cgm.core.gamemodes.EnumSetting;
import net.purelic.cgm.core.gamemodes.constants.TeamType;
import net.purelic.cgm.core.managers.MatchManager;
import net.purelic.cgm.core.match.Participant;
import net.purelic.cgm.core.match.RoundResult;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class RoundEndEvent extends Event {

    private static final HandlerList HANDLERS = new HandlerList();

    private final RoundResult result;

    public RoundEndEvent() {
        MatchManager matchManager = CGM.get().getMatchManager();

        if (matchManager.allEliminated()) {
            if (EnumSetting.TEAM_TYPE.is(TeamType.SOLO)) {
                this.result = new RoundResult(matchManager.getLastAlive());
            } else {
                this.result = new RoundResult(matchManager.getLastTeamAlive());
            }
        } else {
            if (EnumSetting.TEAM_TYPE.is(TeamType.SOLO)) {
                this.result = new RoundResult(MatchManager.getTopParticipant(false));
            } else {
                this.result = new RoundResult(MatchTeam.getTopTeam(false));
            }
        }
    }

    public RoundEndEvent(Participant winner) {
        this.result = new RoundResult(winner);
    }

    public RoundEndEvent(MatchTeam team) {
        this.result = new RoundResult(team);
    }

    public RoundResult getResult() {
        return this.result;
    }

    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

}
