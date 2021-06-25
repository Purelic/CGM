package net.purelic.cgm.core.managers;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.TextComponent;
import net.purelic.cgm.CGM;
import net.purelic.cgm.core.constants.JoinState;
import net.purelic.cgm.core.constants.MatchState;
import net.purelic.cgm.core.constants.MatchTeam;
import net.purelic.cgm.core.gamemodes.*;
import net.purelic.cgm.core.gamemodes.constants.TeamType;
import net.purelic.cgm.core.maps.CustomMap;
import net.purelic.cgm.core.match.Participant;
import net.purelic.cgm.core.match.Round;
import net.purelic.cgm.core.runnables.CycleCountdown;
import net.purelic.cgm.core.runnables.MatchCountdown;
import net.purelic.cgm.events.match.MatchCycleEvent;
import net.purelic.cgm.events.participant.ParticipantRespawnEvent;
import net.purelic.cgm.listeners.match.MatchStart;
import net.purelic.cgm.listeners.modules.stats.MatchStatsModule;
import net.purelic.cgm.utils.MatchUtils;
import net.purelic.cgm.voting.VotingOption;
import net.purelic.commons.Commons;
import net.purelic.commons.runnables.MapLoader;
import net.purelic.commons.utils.*;
import net.purelic.commons.utils.constants.ServerStatus;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;
import java.util.stream.Collectors;

public class MatchManager {

    private static World currentWorld;
    private static CustomMap currentMap;
    private static CustomMap nextMap;
    private static CustomGameMode currentGameMode;
    private static CustomGameMode nextGameMode;
    private static int round;
    private static final List<Round> rounds = new ArrayList<>();
    private static final Map<Player, Participant> PARTICIPANTS = new HashMap<>();
    private static int matches = 1;

    public MatchManager() {
        MatchState.setState(MatchState.WAITING);
    }

    public static CustomMap getCurrentMap() {
        return currentMap;
    }

    public static CustomGameMode getCurrentGameMode() {
        return currentGameMode;
    }

    public static CustomMap getNextMap() {
        return nextMap;
    }

    public CustomGameMode getNextGameMode() {
        return nextGameMode;
    }

    public static void setNext(VotingOption option) {
        setNext(option.getMap(), option.getGameMode());
    }

    public static void setNext(CustomMap map, CustomGameMode gameMode) {
        CommandUtils.broadcastAlertMessage(
            new TextComponent("The next match has been set to "),
            new ComponentBuilder(gameMode.getName()).color(ChatColor.GOLD).create()[0],
            new TextComponent(" on "),
            new ComponentBuilder(map.getName()).color(ChatColor.YELLOW).create()[0]
        );

        if (nextMap != null) {
            MapUtils.deleteWorld(nextMap.getWorld());
        }

        setNextGameMode(gameMode);
        setNextMap(map);

        if (TaskUtils.isRunning(CycleCountdown.getCountdown())) {
            CycleCountdown.setMap(map);
            CycleCountdown.setGameMode(gameMode);
        }
    }

    private static void setNextMap(CustomMap map) {
        nextMap = map;
        new MapLoader(map.getName(), UUID.randomUUID().toString()).runTaskAsynchronously(CGM.get());
    }

    private static void setNextGameMode(CustomGameMode gameMode) {
        nextGameMode = gameMode;
    }

    public static void cycle() {
        MatchManager.resetSettings();

        getParticipants().forEach(Participant::resetScore);

        for (MatchTeam team : MatchTeam.values()) {
            team.reset();
        }

        rounds.clear();
        PARTICIPANTS.clear();

        if (ServerUtils.isPrivate() || ServerUtils.isRanked()) JoinState.setState(JoinState.EVERYONE);
        else JoinState.setState(JoinState.LOCKED);

        CGM.getVotingManager().setCanceled(false);

        if (nextMap == null) {
            MatchState.setState(MatchState.WAITING);
            if (CGM.getVotingManager().shouldStartVoting()) MatchState.setState(MatchState.VOTING);
            currentMap = nextMap;
            if (ServerUtils.isRanked()) LeagueManager.reset();
            else DatabaseUtils.updateStatus(ServerStatus.STARTING, null, null);
        } else {
            MatchState.setState(MatchState.STARTING);

            ScoreboardManager.setDisplayName(ChatColor.AQUA + "play.purelic.net");
            ScoreboardManager.resetScores(0);

            CGM.getVotingManager().setLastPlayed(nextMap, nextGameMode);

            nextGameMode.loadSettings();
            round = 0;
            currentMap = nextMap;

            for (int i = 0; i < NumberSetting.ROUNDS.value(); i++) {
                rounds.add(new Round());
            }

            nextMap.loadObjectives();

            final String mapName = nextMap.getName();
            final String gmName = nextGameMode.getName();

            new BukkitRunnable() {
                @Override
                public void run() {
                    DatabaseUtils.updateStatus(ServerStatus.STARTED, mapName, gmName);
                    MatchStart.updateParties();
                }
            }.runTaskAsynchronously(CGM.get());
        }

        currentGameMode = nextGameMode;
        nextMap = null;
        nextGameMode = null;

        Bukkit.getOnlinePlayers().forEach(MatchTeam.OBS::addPlayer);
        TabManager.updateTime(NumberSetting.TIME_LIMIT.value() * 60);
        // MatchUtils.updateTabAll(NumberSetting.TIME_LIMIT.value() * 60);
        TabManager.reset();
        ScoreboardManager.updateTeamBoard();

        if (currentWorld != null) {
            MapUtils.deleteWorld(currentWorld);
        }

        if (currentMap != null) {
            currentWorld = currentMap.getWorld();
        } else {
            currentWorld = null;
        }

        if (MatchState.isState(MatchState.STARTING)) {
            Bukkit.getOnlinePlayers().forEach(player -> player.performCommand("match"));
        }

        if (currentMap != null && currentGameMode != null) {
            Commons.callEvent(new MatchCycleEvent(currentMap, currentGameMode));
        }
    }

    public static int getRound() {
        return round;
    }

    public void addRound() {
        round++;
    }

    public void setRoundWinner(MatchTeam team) {
        rounds.get(round - 1).setComplete(team);
        if (team != null && team != MatchTeam.SOLO) team.addRoundWin();
    }

    public static String getRoundsString() {
        StringBuilder s = new StringBuilder();

        for (Round round : rounds) {
            s.append(" ").append(round.toString());
        }

        return s.toString().trim();
    }

    public static Participant getParticipant(Player player) {
        return PARTICIPANTS.get(player);
    }

    public static void addParticipant(Player player, boolean roundStart, boolean forced) {
        Participant participant = new Participant(player);
        PARTICIPANTS.put(player, participant);

        if (!forced && participant.getLives() > 0 && TaskUtils.isRunning(MatchCountdown.getCountdown())) {
            participant.setQueued(true);
        }

        if (MatchStatsModule.hasStats(player)) TabManager.updateStats(participant);

        Commons.callEvent(new ParticipantRespawnEvent(participant, roundStart));
    }

    public void removeParticipant(Player player) {
        if (!isPlaying(player)) return;
        PARTICIPANTS.get(player).reset();
        PARTICIPANTS.remove(player);
        TabManager.removeStats(player);
    }

    public static boolean isPlaying(Player player) {
        return player.isOnline() && PARTICIPANTS.containsKey(player);
    }

    public boolean allEliminated() {
        return MatchUtils.isElimination() && (EnumSetting.TEAM_TYPE.is(TeamType.SOLO) ?
            (this.getParticipantAlive() == 0 || this.getLastParticipantAlive() != null) :
            (this.getTeamsAlive() == 0 || this.getLastTeamAlive() != null));
    }

    public MatchTeam getLastTeamAlive() {
        int teamsAlive = 0;
        MatchTeam lastAlive = null;

        for (MatchTeam team : MatchTeam.values()) {
            if (team == MatchTeam.OBS) continue;

            for (Player player : team.getPlayers()) {
                Participant participant = getParticipant(player);
                if (participant.isQueued()) continue;
                if (!participant.isDead() || participant.getLives() > 0) {
                    teamsAlive++;
                    lastAlive = team;
                    break;
                }
            }
        }

        return teamsAlive != 1 ? null : lastAlive;
    }

    public int getTeamsAlive() {
        int teamsAlive = 0;

        for (MatchTeam team : MatchTeam.values()) {
            if (team == MatchTeam.OBS) continue;

            for (Player player : team.getPlayers()) {
                Participant participant = getParticipant(player);
                if (participant.isQueued()) continue;
                if (!participant.isDead() || participant.getLives() > 0) {
                    teamsAlive++;
                    break;
                }
            }
        }

        return teamsAlive;
    }

    public Participant getLastParticipantAlive() {
        int playersAlive = 0;
        Participant lastAlive = null;

        for (Participant participant : getParticipants()) {
            if (participant.isQueued()) continue;
            if (!participant.isDead() || participant.getLives() > 0) {
                playersAlive++;
                lastAlive = participant;
            }
        }

        return playersAlive != 1 ? null : lastAlive;
    }

    public int getParticipantAlive() {
        int playersAlive = 0;

        for (Participant participant : getParticipants()) {
            if (participant.isQueued()) continue;
            if (!participant.isDead() || participant.getLives() > 0) {
                playersAlive++;
            }
        }

        return playersAlive;
    }

    public static Collection<Participant> getParticipants() {
        return PARTICIPANTS.values();
    }

    public static List<Participant> getOrderedParticipants(boolean totalScore) {
        List<Participant> ordered = new ArrayList<>(PARTICIPANTS.values())
            .stream().filter(participant -> !participant.isQueued()).collect(Collectors.toList());

        if (MatchUtils.isElimination() && !MatchUtils.hasKillScoring()) ordered.sort(Comparator.comparingInt(Participant::getEliminatedScore).thenComparing(Participant::getLives).reversed());
        else if (totalScore) ordered.sort(Comparator.comparingInt(Participant::getTotalScore).reversed());
        else ordered.sort(Comparator.comparingInt(Participant::getScore).reversed());

        return ordered;
    }

    public static Participant getTopParticipant(boolean totalScore) {
        List<Participant> ordered = getOrderedParticipants(totalScore);
        if (ordered.size() == 0) return null;
        if (ordered.size() == 1) return ordered.get(0);
        return ordered.get(0).getScore() == ordered.get(1).getScore() ? null : ordered.get(0);
    }

    public static List<MatchTeam> getOrderedTeams(TeamType teamType) {
        List<MatchTeam> ordered = new ArrayList<>(new ArrayList<>(teamType.getTeams()));
        if (MatchUtils.isElimination() && !MatchUtils.hasKillScoring()) {
            ordered.sort(Comparator.comparingInt(MatchUtils::getAlive).reversed());
        } else {
            ordered.sort(Comparator.comparingInt(MatchTeam::getScore).reversed());
        }
        return ordered;
    }

    public static void resetSettings() {
        resetSettings(ToggleSetting.values());
        resetSettings(NumberSetting.values());
        resetSettings(EnumSetting.values());
    }

    private static void resetSettings(GameSetting[] settings) {
        for (GameSetting setting : settings) setting.reset();
    }

    public static void addMatch() {
        matches++;
    }

    public static int getMatches() {
        return matches;
    }

}
