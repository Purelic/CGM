package net.purelic.cgm.match;

import net.md_5.bungee.api.ChatColor;
import net.purelic.cgm.CGM;
import net.purelic.cgm.core.constants.JoinState;
import net.purelic.cgm.core.constants.MatchState;
import net.purelic.cgm.core.constants.MatchTeam;
import net.purelic.cgm.core.gamemodes.*;
import net.purelic.cgm.core.managers.LeagueManager;
import net.purelic.cgm.core.managers.ScoreboardManager;
import net.purelic.cgm.core.maps.CustomMap;
import net.purelic.cgm.core.runnables.MatchCountdown;
import net.purelic.cgm.events.match.MatchCycleEvent;
import net.purelic.cgm.events.participant.ParticipantRespawnEvent;
import net.purelic.cgm.listeners.match.MatchStart;
import net.purelic.cgm.match.constants.ParticipantState;
import net.purelic.cgm.voting.VotingOption;
import net.purelic.commons.Commons;
import net.purelic.commons.runnables.MapLoader;
import net.purelic.commons.utils.*;
import net.purelic.commons.utils.constants.ServerStatus;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.*;

public class MatchManager2 {

    private static final int MATCH_RESTART_THRESHOLD = 25;

    private final Map<UUID, MatchParticipant> participants;
    private final Deque<Match> matchQueue;
    private final List<Match> matchHistory;
    private Match currentMatch;
    private boolean pendingRestart;

    public MatchManager2() {
        this.participants = new HashMap<>();
        this.matchQueue = new LinkedList<>();
        this.matchHistory = new ArrayList<>();
        this.pendingRestart = false;
        MatchState.setState(MatchState.WAITING);
    }

    public boolean isPlaying(Player player) {
        return this.participants.containsKey(player.getUniqueId());
    }

    public MatchParticipant getParticipant(Player player) {
        return this.participants.get(player.getUniqueId());
    }

    public Collection<MatchParticipant> getParticipants() {
        return this.participants.values();
    }

    public void addParticipant(Player player, boolean roundStart, boolean forced) {
        MatchParticipant participant = new MatchParticipant(player);
        this.participants.put(player.getUniqueId(), participant);

        if (!forced && participant.getLives() > 0 && TaskUtils.isRunning(MatchCountdown.getCountdown())) {
            participant.setState(ParticipantState.QUEUED);
        }

        CGM.getTabManager().updateStats(player);
        Commons.callEvent(new ParticipantRespawnEvent(participant, roundStart));
    }

    public Match getCurrentMatch() {
        return this.currentMatch;
    }

    public boolean hasActiveMatch() {
        return this.currentMatch != null;
    }

    public Deque<Match> getMatchQueue() {
        return this.matchQueue;
    }

    public boolean hasNextMatch() {
        return this.matchQueue.size() > 0;
    }

    public Match getNextMatch() {
        return this.matchQueue.getFirst();
    }

    public void setNextMatch(VotingOption votingOption) {
        this.setNextMatch(votingOption.getMap(), votingOption.getGameMode());
    }

    public void setNextMatch(CustomMap map, CustomGameMode gameMode) {
        this.queue(new Match(map, gameMode), true);

        CommandUtils.broadcastAlertMessage("The next match has been set to " +
            gameMode.getColoredName() + " on " + map.getColoredName());
    }

    public void queue(CustomMap map, CustomGameMode gameMode) {
        this.queue(new Match(map, gameMode), false);
    }

    private void queue(Match match, boolean next) {
        // add match to the queue
        if (next) this.matchQueue.addFirst(match);
        else this.matchQueue.addLast(match);

        // copy/load the map for the match
        TaskUtils.runAsync(new MapLoader(match.getMap().getName(), match.getId()));
    }

    public void clearQueue() {
        // delete all loaded worlds in the queue
        for (Match queued : this.matchQueue) {
            World world = queued.getMap().getWorld();
            if (world != null) MapUtils.deleteWorld(world);
        }

        this.matchQueue.clear();
    }

    public List<Match> getMatchHistory() {
        return this.matchHistory;
    }

    public boolean hasPendingRestart() {
        return this.pendingRestart;
    }

    public void setPendingRestart(boolean pendingRestart) {
        this.pendingRestart = pendingRestart;
    }

    // TODO clean up this by putting some logic in the match cycle event listeners
    public void cycle() {
        // reset all the game settings
        this.resetGameSettings();

        // reset teams back to default
        for (MatchTeam team : MatchTeam.values()) team.reset();

        // clear participants
        this.participants.clear();

        // make sure voting is no longer in a canceled state
        // TODO move this to cycle event in voting module
        CGM.getVotingManager().setCanceled(false);

        // update the join state
        if (ServerUtils.isPrivate() || ServerUtils.isRanked()) JoinState.setState(JoinState.EVERYONE);
        else JoinState.setState(JoinState.LOCKED);

        if (this.currentMatch != null) {
            // clean up past match
            MapUtils.deleteWorld(this.currentMatch.getMap().getWorld());
        }

        if (this.hasNextMatch()) {
            this.currentMatch = this.getNextMatch();
            this.matchHistory.add(this.currentMatch);

            MatchState.setState(MatchState.STARTING);
            Bukkit.getOnlinePlayers().forEach(player -> player.performCommand("match"));

            ScoreboardManager.setDisplayName(ChatColor.AQUA + "play.purelic.net");
            ScoreboardManager.resetScores(0);

            this.currentMatch.load();

            // TODO make this called every time and handle cased where there is no cycled map
            Commons.callEvent(new MatchCycleEvent(this.currentMatch));

            DatabaseUtils.updateStatus(
                ServerStatus.STARTED,
                this.currentMatch.getMap().getName(),
                this.currentMatch.getGameMode().getName()
            );

            TaskUtils.runAsync(MatchStart::updateParties);
        } else {
            this.currentMatch = null;

            MatchState.setState(MatchState.WAITING);

            if (CGM.getVotingManager().shouldStartVoting()) {
                MatchState.setState(MatchState.VOTING);
            }

            if (ServerUtils.isRanked()) LeagueManager.reset();
            else DatabaseUtils.updateStatus(ServerStatus.STARTING, null, null);
        }

        Bukkit.getOnlinePlayers().forEach(MatchTeam.OBS::addPlayer);
        CGM.getTabManager().updateTime(NumberSetting.TIME_LIMIT.value() * 60);
        CGM.getTabManager().reset();
        ScoreboardManager.updateTeamBoard();

        // check if we need to schedule a restart
        this.pendingRestart = this.matchHistory.size() >= MATCH_RESTART_THRESHOLD
            && !ServerUtils.isPrivate()
            && !this.hasNextMatch();
    }

    private void resetGameSettings() {
        this.resetGameSettings(ToggleSetting.values());
        this.resetGameSettings(NumberSetting.values());
        this.resetGameSettings(EnumSetting.values());
    }

    private void resetGameSettings(GameSetting[] settings) {
        for (GameSetting setting : settings) setting.reset();
    }

}
