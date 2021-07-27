package net.purelic.cgm.scoreboards;

import net.md_5.bungee.api.ChatColor;
import net.purelic.cgm.core.constants.MatchState;
import net.purelic.cgm.core.constants.MatchTeam;
import net.purelic.cgm.core.gamemodes.NumberSetting;
import net.purelic.cgm.core.managers.MatchManager;
import net.purelic.cgm.core.managers.ScoreboardManager;
import net.purelic.cgm.core.match.Participant;
import net.purelic.cgm.utils.MatchUtils;
import net.purelic.cgm.utils.tab.TabUtils;
import net.purelic.commons.utils.Fetcher;
import net.purelic.commons.utils.NickUtils;
import org.bukkit.entity.Player;

import java.util.List;

public class PlayerScoreboard extends MatchScoreboard {

    @Override
    public void update() {
        // initialize the scoreboard size with how many slots objectives take up
        int size = this.objectives == 0 ? 0 : this.objectives + 1;

        // add the timer slots to the scoreboard size
        size += this.timers.size() == 0 ? 0 : this.timers.size() + 1;

        // end for player slots
        int end = this.timers.size() == 0 ? 15 : 14 - this.timers.size();

        // we display a slightly different scoreboard layout for elimination/non-scoring game modes
        if (MatchUtils.isElimination() && !MatchUtils.hasKillScoring()) {
            // if the match hasn't started we don't display # of players alive
            if (MatchState.isState(MatchState.PRE_GAME, MatchState.STARTING)) {
                return;
            }

            List<Player> alive = TabUtils.sort(MatchUtils.getAlivePlayers(MatchTeam.SOLO));

            // if we can list all the remaining alive players on the scoreboard
            if (alive.size() <= this.slots - 1) {
                // add section header
                String score = alive.size() + " " + MatchTeam.SOLO.getColor() + "Alive" + ChatColor.WHITE + ":";
                ScoreboardManager.setScore(this.start, score);

                // add the list of players alive
                int index = 0;
                int start = this.start + 1; // make local copy so we don't update it during our loop
                for (int slot = start; slot < end; slot++) {
                    if (index >= alive.size()) break;
                    ScoreboardManager.setScore(slot, Fetcher.getBasicName(alive.get(index)));
                    index++;
                }

                // add the list of players alive + the "Alive:" slot to the size
                size += alive.size() + 1;
            } else {
                // show a count of alive players
                int slot = this.objectives == 0 ? 0 : this.objectives + 1;
                String score = alive.size() + " " + MatchTeam.SOLO.getColor() + "Alive";
                ScoreboardManager.setScore(slot, score);

                // add the "# Alive" slot to the size
                size += 1;
            }
        } else {
            List<Participant> participants = MatchManager.getOrderedParticipants(true);

            // count the number of players who have actually scored points
            int scored = 0;

            int limit = NumberSetting.SCORE_LIMIT.value();
            String scoreSuffix = limit == 0 ? "" : ChatColor.DARK_GRAY + "/" + ChatColor.GRAY + limit;

            int index = 0;
            int start = this.start; // make local copy so we don't update it during our loop

            for (int slot = start; slot < end; slot++) {
                if (index >= participants.size()) break;

                Participant participant = participants.get(index);
                String scoreColor = this.getScoreColor(participant);
                int score = participant.getTotalScore();

                if (score > 0) {
                    ScoreboardManager.setScore(slot, scoreColor + score + scoreSuffix + "  " + NickUtils.getDisplayName(participant.getPlayer()));
                    scored++;
                }

                index++;
            }

            size += scored;
        }

        // remove extra/unused scoreboard rows
        ScoreboardManager.resetScores(size);
        this.size = size;
    }

}
