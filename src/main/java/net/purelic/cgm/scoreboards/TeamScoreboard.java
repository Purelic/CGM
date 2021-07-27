package net.purelic.cgm.scoreboards;

import net.md_5.bungee.api.ChatColor;
import net.purelic.cgm.core.constants.MatchState;
import net.purelic.cgm.core.constants.MatchTeam;
import net.purelic.cgm.core.gamemodes.EnumSetting;
import net.purelic.cgm.core.gamemodes.NumberSetting;
import net.purelic.cgm.core.gamemodes.constants.GameType;
import net.purelic.cgm.core.gamemodes.constants.TeamType;
import net.purelic.cgm.core.managers.MatchManager;
import net.purelic.cgm.core.managers.ScoreboardManager;
import net.purelic.cgm.utils.BedUtils;
import net.purelic.cgm.utils.MatchUtils;

import java.util.List;

public class TeamScoreboard extends MatchScoreboard {

    @Override
    public void update() {
        TeamType teamType = EnumSetting.TEAM_TYPE.get();
        List<MatchTeam> teams = MatchManager.getOrderedTeams(teamType);

        int slot = this.start; // make a local copy so we don't update while looping

        for (MatchTeam team : teams) {
            int limit = NumberSetting.SCORE_LIMIT.value();
            String scoreSuffix = limit == 0 ? "" : ChatColor.DARK_GRAY + "/" + ChatColor.GRAY + limit;

            // for bed wars we display the bed status
            if (EnumSetting.GAME_TYPE.is(GameType.BED_WARS)) {
                ScoreboardManager.setScore(slot, BedUtils.getScoreboardScore(team));
            } else {
                if (MatchUtils.isElimination() && !MatchUtils.hasKillScoring()) {
                    // if the match hasn't started don't show # alive
                    if (MatchState.isState(MatchState.PRE_GAME, MatchState.STARTING)) continue;

                    int alive = MatchUtils.getAlive(team);
                    String prefix = alive == 0 ? " " + ChatColor.RED + BedUtils.SYMBOL_TEAM_ELIMINATED : " " + alive;
                    String suffix = team.getColor() + (alive == 0 ? team.getName() : "Alive");
                    ScoreboardManager.setScore(slot, prefix + " " + suffix);
                } else {
                    String scoreColor = this.getScoreColor(team);
                    ScoreboardManager.setScore(slot, scoreColor + team.getScore() + scoreSuffix + " " + team.getColoredName());
                }
            }

            slot++;
        }

        // initialize the scoreboard size with how many slots objectives take up
        int size = this.objectives == 0 ? 0 : this.objectives + 1;

        // add the timer slots to the scoreboard size
        size += this.timers.size() == 0 ? 0 : this.timers.size() + 1;

        // add team slots
        size += teams.size();

        // remove extra/unused scoreboard rows
        ScoreboardManager.resetScores(size);
        this.size = size;
    }

}
