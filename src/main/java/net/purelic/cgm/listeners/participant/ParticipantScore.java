package net.purelic.cgm.listeners.participant;

import net.purelic.cgm.core.constants.MatchTeam;
import net.purelic.cgm.core.gamemodes.EnumSetting;
import net.purelic.cgm.core.gamemodes.NumberSetting;
import net.purelic.cgm.core.gamemodes.constants.TeamType;
import net.purelic.cgm.core.managers.ScoreboardManager;
import net.purelic.cgm.core.match.Participant;
import net.purelic.cgm.events.match.RoundEndEvent;
import net.purelic.cgm.events.participant.ParticipantScoreEvent;
import net.purelic.cgm.utils.MatchUtils;
import net.purelic.commons.Commons;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class ParticipantScore implements Listener {

    @EventHandler
    public void onParticipantScore(ParticipantScoreEvent event) {
        Participant participant = event.getParticipant();
        int points = event.getPoints();

        if (EnumSetting.TEAM_TYPE.is(TeamType.SOLO)) {
            ScoreboardManager.updateSoloBoard();
            int scoreLimit = NumberSetting.SCORE_LIMIT.value();

            if (participant.getScore() >= scoreLimit && scoreLimit > 0 && !MatchUtils.isElimination()) {
                Commons.callEvent(new RoundEndEvent());
            }
        } else {
            MatchTeam team = MatchTeam.getTeam(participant.getPlayer());
            team.addScore(points);
        }
    }

}
