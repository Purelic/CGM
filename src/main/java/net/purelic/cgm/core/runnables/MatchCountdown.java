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
import net.purelic.commons.Commons;
import net.purelic.commons.utils.ChatUtils;
import net.purelic.commons.utils.CommandUtils;
import net.purelic.commons.utils.ServerUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class MatchCountdown extends BukkitRunnable {

    private static Random RANDOM = new Random();

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
            || (EnumSetting.GAME_TYPE.is(GameType.HEAD_HUNTER) && ToggleSetting.HEAD_COLLECTION_HILLS.isEnabled())
            || (EnumSetting.GAME_TYPE.is(GameType.DEATHMATCH) && ToggleSetting.DEATHMATCH_SCOREBOXES.isEnabled())) {
            if (NumberSetting.HILL_MOVE_INTERVAL.value() > 0) {
                HillUtils.getHills().stream()
                    .filter(Hill::isActive)
                    .filter(Hill::hasWaypoint)
                    .forEach(hill -> hill.getWaypoint().setName(hill.getTitle()));

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
                        for (Player player : hill.getPlayers()) {
                            Participant participant = MatchManager.getParticipant(player);
                            participant.addScore(points, true);
                            scoredParticipants.put(participant, points);
                        }
                    } else {
                        MatchTeam team = hill.getCapturedBy();

                        if (team != hill.getOwner()) {
                            team.addScore(points, true);
                            scoredTeams.put(team, points);
                        }
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

            int interval = NumberSetting.HEAD_COLLECTION_INTERVAL.value();

            if (interval > 0) {
                int seconds = MatchCountdown.getElapsed() % interval;
                seconds = interval - seconds;
                seconds = seconds == 0 ? interval : seconds;

                if (elapsed % interval == 0 && elapsed > 0) {
                    ChatUtils.broadcastActionBar("Heads collected!", true);
                    MatchManager.getParticipants().forEach(HeadModule::scoreHeads);
                } else if (seconds <= 20) {
                    ChatUtils.broadcastActionBar(
                        "Heads collecting in " + ChatColor.AQUA + seconds + ChatColor.RESET + " second" + ((seconds == 1) ? "!" : "s!"),
                        seconds % 5 == 0
                    );
                }
            }
        } else if (EnumSetting.GAME_TYPE.is(GameType.CAPTURE_THE_FLAG)) {
            FlagUtils.scoreCarrierPoints();

            if (NumberSetting.FLAG_COLLECTION_INTERVAL.value() > 0) {
                HillUtils.getHills()
                    .stream().filter(Hill::hasWaypoint)
                    .forEach(hill -> hill.getWaypoint().setName(hill.getTitle()));

                if (elapsed % NumberSetting.FLAG_COLLECTION_INTERVAL.value() == 0 && elapsed > 0) {
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

        // every 15 minutes, every minute if less than 5 minutes
        if (seconds >= 0 && (seconds % 900 == 0 || (seconds % 60 == 0 && seconds <= 300) || seconds <= 15)) {
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
                int index = ToggleSetting.RANDOM_HILLS.isEnabled() ? RANDOM.nextInt(hills.size()) : 0;
                this.currentHill = hills.get(index);
                Bukkit.broadcastMessage(" ⦿ The hill has spawned at " + this.currentHill.getName() + "!");
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
                    index = RANDOM.nextInt(hills.size());
                }
            } else {
                index += 1; // next hill
                if (index == hills.size()) index = 0; // go back to first hill if we've reached the end
            }

            this.currentHill.reset(true);
            this.currentHill = hills.get(index);
            this.currentHill.activate();

            Bukkit.broadcastMessage(" ⦿ The hill has moved to " + this.currentHill.getName() + "!");
        }
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

}
