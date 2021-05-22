package net.purelic.cgm.core.runnables;

import net.md_5.bungee.api.ChatColor;
import net.purelic.cgm.core.constants.MatchState;
import net.purelic.cgm.core.constants.MatchTeam;
import net.purelic.cgm.core.gamemodes.EnumSetting;
import net.purelic.cgm.core.gamemodes.NumberSetting;
import net.purelic.cgm.core.gamemodes.ToggleSetting;
import net.purelic.cgm.core.gamemodes.constants.GameType;
import net.purelic.cgm.core.gamemodes.constants.TeamType;
import net.purelic.cgm.core.managers.MatchManager;
import net.purelic.cgm.core.managers.TabManager;
import net.purelic.cgm.core.maps.bed.events.BedBreakEvent;
import net.purelic.cgm.core.maps.hill.Hill;
import net.purelic.cgm.core.match.Participant;
import net.purelic.cgm.events.match.RoundEndEvent;
import net.purelic.cgm.events.participant.ParticipantScoreEvent;
import net.purelic.cgm.listeners.modules.HeadModule;
import net.purelic.cgm.utils.*;
import net.purelic.commons.utils.CommandUtils;
import net.purelic.commons.Commons;
import net.purelic.commons.utils.ServerUtils;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class MatchCountdown extends BukkitRunnable {

    private static MatchCountdown countdown;
    private static int seconds;
    private static int elapsed;
    private static boolean forced;
    private static List<Hill> hills;
    private Hill currentHill;

    public MatchCountdown(boolean forced) {
        countdown = this;
        seconds = NumberSetting.TIME_LIMIT.value() * 60;
        elapsed = 0;
        hills = MatchManager.getCurrentMap().getLoadedHills();
        this.currentHill = null;
        MatchCountdown.forced = forced;
    }

    @Override
    public void run() {
        if (!MatchState.isState(MatchState.STARTED)) {
            this.cancel();
            return;
        }

        if (MatchTeam.totalPlaying() == 0 || (!MatchTeam.hasMinPlayers() && !forced && !ServerUtils.isPrivate())) {
            CommandUtils.broadcastErrorMessage("Not enough players to continue! Ending the match");
            MatchState.setState(MatchState.ENDED, !MatchUtils.isElimination() && !ServerUtils.isRanked(), 10);
            this.cancel();
            return;
        }

        if (EnumSetting.GAME_TYPE.is(GameType.KING_OF_THE_HILL)
                || (EnumSetting.GAME_TYPE.is(GameType.HEAD_HUNTER) && ToggleSetting.HEAD_COLLECTION_HILLS.isEnabled())) {
            if (NumberSetting.HILL_MOVE_INTERVAL.value() > 0) {
                HillUtils.getHills().stream().filter(Hill::isActive).forEach(hill -> hill.getWaypoint().setName(hill.getTitle()));

                if (elapsed % NumberSetting.HILL_MOVE_INTERVAL.value() == 0) {
                    this.moveHill();
                }
            }


            if (this.currentHill != null) {
                this.currentHill.updateScoreboard();
            }
        }

        if (EnumSetting.GAME_TYPE.is(GameType.KING_OF_THE_HILL)) {
            Map<MatchTeam, Integer> scoredTeams = new HashMap<>();
            Map<Participant, Integer> scoredParticipants = new HashMap<>();

            for (Hill hill : hills) {
                if (hill.isCaptured()) {
                    int points = NumberSetting.HILL_CAPTURE_POINTS.value();

                    if (EnumSetting.TEAM_TYPE.is(TeamType.SOLO)) {
                        Participant participant = hill.getCapturedByParticipant();
                        participant.addScore(points, true);
                        scoredParticipants.put(participant, points);
                    } else {
                        MatchTeam team = hill.getCapturedByTeam();
                        team.addScore(points, true);
                        scoredTeams.put(team, points);
                    }
                }
            }

            for (Map.Entry<Participant, Integer> participant : scoredParticipants.entrySet()) {
                Commons.callEvent(new ParticipantScoreEvent(participant.getKey(), participant.getValue()));
            }

            int scoreLimit = NumberSetting.SCORE_LIMIT.value();

            for (Map.Entry<MatchTeam, Integer> team : scoredTeams.entrySet()) {
                if (team.getKey().getScore() >= scoreLimit && scoreLimit > 0 && !MatchUtils.isElimination()) {
                    Commons.callEvent(new RoundEndEvent());
                    break;
                }
            }
        } else if (EnumSetting.GAME_TYPE.is(GameType.HEAD_HUNTER)) {
            HeadModule.displayParticles();
        } else if (EnumSetting.GAME_TYPE.is(GameType.CAPTURE_THE_FLAG)) {
            FlagUtils.scoreCarrierPoints();

            if (NumberSetting.FLAG_COLLECTION_INTERVAL.value() > 0) {
                HillUtils.getHills().forEach(hill -> hill.getWaypoint().setName(hill.getTitle()));

                if (elapsed % NumberSetting.FLAG_COLLECTION_INTERVAL.value() == 0 && elapsed > 0){
                    FlagUtils.collectFlags();
                }
            }
        } else if (EnumSetting.GAME_TYPE.is(GameType.BED_WARS)) {
            if (NumberSetting.BED_WARS_SUDDEN_DEATH.value() != 0) {
                if (NumberSetting.BED_WARS_SUDDEN_DEATH.value() * 60 == seconds) {
                    BedUtils.getBeds().stream().filter(bed -> !bed.isDestroyed()).forEach(bed -> Commons.callEvent(new BedBreakEvent(bed, true)));
                }
            }
        }

        // MatchUtils.updateTabAll(seconds);
        TabManager.updateTime(NumberSetting.TIME_LIMIT.value() == 0 ? elapsed : seconds);

        if (seconds >= 0 && (seconds % 60 == 0 || seconds <= 15)) {
            if (seconds >= 15) Bukkit.broadcastMessage(TabManager.getTime(false));
            SoundUtils.playCountdownNote(seconds);
        }

        if (seconds <= 0 && NumberSetting.TIME_LIMIT.value() > 0) {
            Commons.callEvent(new RoundEndEvent());
            this.cancel();
        }

        seconds--;
        elapsed++;
    }

    private void moveHill() {
        if (this.currentHill == null) {
            Object[] activeHills = hills.stream().filter(Hill::isActive).toArray();

            if (activeHills.length == 0) {
                int index = ToggleSetting.RANDOM_HILLS.isEnabled() ? new Random().nextInt(hills.size()) : 0;
                this.currentHill = hills.get(index);
                Bukkit.broadcastMessage(" ⦿ The hill has spawned at " + this.currentHill.getColoredName() + "!");
            } else {
                this.currentHill = (Hill) activeHills[0];
                Bukkit.broadcastMessage(" ⦿ The hill moves every " + ChatColor.AQUA + NumberSetting.HILL_MOVE_INTERVAL.value() + ChatColor.RESET + " seconds!");
            }

            this.currentHill.activate();
        } else {
            int index = hills.indexOf(this.currentHill);

            if (ToggleSetting.RANDOM_HILLS.isEnabled()) {
                int prev = index;

                while (index == prev) { // pick random hill location that's not equal to the current one
                    index = new Random().nextInt(hills.size());
                }
            } else {
                index += 1; // next hill
                if (index == hills.size()) index = 0; // go back to first hill if we've reached the end
            }

            this.currentHill.cancel(true);
            this.currentHill = hills.get(index);
            this.currentHill.activate();

            Bukkit.broadcastMessage(" ⦿ The hill has moved to " + this.currentHill.getColoredName() + "!");
        }
    }

    public void addTime(int seconds) {
        MatchCountdown.seconds += seconds;
    }

    public static MatchCountdown getCountdown() {
        return countdown;
    }

    public static int getSeconds() {
        return seconds;
    }

    public static int getElapsed() {
        return elapsed;
    }

    public static boolean isForced() {
        return forced;
    }

    public static List<Hill> getHills() {
        return hills;
    }

}
