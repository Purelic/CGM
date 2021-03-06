package net.purelic.cgm.core.constants;

import net.md_5.bungee.api.ChatColor;
import net.purelic.cgm.core.gamemodes.EnumSetting;
import net.purelic.cgm.core.gamemodes.NumberSetting;
import net.purelic.cgm.core.gamemodes.constants.GameType;
import net.purelic.cgm.core.gamemodes.constants.TeamType;
import net.purelic.cgm.core.managers.MatchManager;
import net.purelic.cgm.core.managers.ScoreboardManager;
import net.purelic.cgm.core.managers.TabManager;
import net.purelic.cgm.core.match.Participant;
import net.purelic.cgm.core.runnables.RoundCountdown;
import net.purelic.cgm.events.match.RoundEndEvent;
import net.purelic.cgm.events.match.SpectatorJoinEvent;
import net.purelic.cgm.listeners.match.MatchEnd;
import net.purelic.cgm.utils.BedUtils;
import net.purelic.cgm.utils.ColorConverter;
import net.purelic.cgm.utils.MatchUtils;
import net.purelic.commons.Commons;
import net.purelic.commons.profile.preferences.ArmorColor;
import net.purelic.commons.utils.NickUtils;
import net.purelic.commons.utils.ServerUtils;
import net.purelic.commons.utils.TaskUtils;
import org.bukkit.entity.Player;

import java.util.*;

public enum MatchTeam {

    OBS("Spectators", ChatColor.AQUA),
    SOLO("FFA", ChatColor.YELLOW),
    BLUE("Blue", ChatColor.BLUE),
    RED("Red", ChatColor.RED),
    GREEN("Green", ChatColor.GREEN),
    YELLOW("Yellow", ChatColor.YELLOW),
    AQUA("Aqua", ChatColor.AQUA),
    PINK("Pink", ChatColor.LIGHT_PURPLE),
    GRAY("Gray", ChatColor.GRAY),
    WHITE("White", ChatColor.WHITE),
    ;

    private static final Map<Player, MatchTeam> PLAYER_MAP = new HashMap<>();

    private final String defaultName;
    private final ChatColor defaultColor;
    private String name;
    private ChatColor color;
    private final List<Player> players;
    private final Set<UUID> allowed;
    private int score;
    private int roundsWon;

    MatchTeam(String name, ChatColor color) {
        this.defaultName = name;
        this.defaultColor = color;
        this.name = name;
        this.color = color;
        this.players = new ArrayList<>();
        this.allowed = new HashSet<>();
        this.score = 0;
        this.roundsWon = 0;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
        ScoreboardManager.updateTeamBoard();
    }

    public String getDefaultName() {
        return this.defaultName;
    }

    public String getColoredName() {
        return this.color + this.name + ChatColor.RESET;
    }

    public ChatColor getColor() {
        return this.color;
    }

    public ArmorColor getArmorColor() {
        return ColorConverter.getArmorColor(this.color);
    }

    public void setColor(ChatColor color) {
        this.color = color;
    }

    public void resetScore() {
        this.score = 0;
        ScoreboardManager.updateTeamBoard();
    }

    public void reset() {
        // this.name = this.defaultName;
        this.color = this.defaultColor;
        this.score = 0;
        this.roundsWon = 0;
        this.players.clear();
        this.allowed.clear();
    }

    public List<Player> getPlayers() {
        return this.players;
    }

    public boolean isAllowed(Player player) {
        return this.allowed.contains(player.getUniqueId());
    }

    public void addPlayer(Player player) {
        this.addPlayer(player, false);
    }

    public void addPlayer(Player player, boolean initialJoin) {
        removePlayer(player);
        this.players.add(player);
        PLAYER_MAP.put(player, this);
        this.allowed.add(player.getUniqueId());
        player.setDisplayName(this.color + NickUtils.getRealName(player) + ChatColor.RESET);
        player.setPlayerListName(Commons.getProfile(player).getFlairs() + player.getDisplayName());
        ScoreboardManager.updateTeam(player, this);
        TabManager.updateTeam(this, true);

        if (this == MatchTeam.OBS) {
            Commons.callEvent(new SpectatorJoinEvent(player, initialJoin));
        }
    }

    public static void removePlayer(Player player) {
        MatchTeam team = MatchTeam.getTeam(player);
        if (team.getPlayers().remove(player))
            TabManager.updateTeam(team, true);
    }

    public boolean hasPlayer(Player player) {
        return this.players.contains(player);
    }

    public int playing() {
        return this.players.size();
    }

    public static MatchTeam getTeamFromString(String team) {
        MatchTeam finalTeam = null;
        team = team.toLowerCase().trim();

        // Look for teams that match the provided String
        for (MatchTeam matchTeam : MatchTeam.values()) {
            String teamName = matchTeam.getName().toLowerCase();
            if (teamName.equalsIgnoreCase(team)) {
                finalTeam = matchTeam;
                break;
            }
        }

        // If no team found, check for teams that start with the provided String
        if (finalTeam == null) {
            for (MatchTeam matchTeam : MatchTeam.values()) {
                String teamName = matchTeam.getName().toLowerCase();
                if (teamName.startsWith(team)) {
                    finalTeam = matchTeam;
                    break;
                }
            }
        }

        // If still no team found, check the default names
        if (finalTeam == null) {
            for (MatchTeam matchTeam : MatchTeam.values()) {
                String teamName = matchTeam.getDefaultName().toLowerCase();
                if (teamName.equalsIgnoreCase(team) || teamName.startsWith(team)) {
                    finalTeam = matchTeam;
                    break;
                }
            }
        }

        return finalTeam;
    }

    public static MatchTeam getTeam(Participant participant) {
        return MatchTeam.getTeam(participant.getPlayer());
    }

    public static MatchTeam getTeam(Player player) {
        return PLAYER_MAP.getOrDefault(player, OBS);
    }

    public static int totalPlaying() {
        int c = 0;
        for (MatchTeam team : MatchTeam.values()) {
            if (team == MatchTeam.OBS) continue;
            c += team.getPlayers().size();
        }
        return c;
    }

    // Returns if 2 teams have at least 1 player
    public static boolean hasMinPlayers() {
        if (EnumSetting.TEAM_TYPE.is(TeamType.SOLO)) {
            return MatchTeam.SOLO.playing() >= 2;
        }

        int c = 0;
        for (MatchTeam team : MatchTeam.values()) {
            if (team == MatchTeam.OBS) continue;
            c += team.getPlayers().size() > 0 ? 1 : 0;
        }
        return c >= 2;
    }

    public boolean isFull() {
        return this.players.size() >= MatchUtils.getMaxTeamPlayers();
    }

    // needs to filter out teams eliminated
    public static MatchTeam getSmallestTeam(TeamType teamType, boolean forced) {
        boolean doubles = MatchUtils.getMaxTeamPlayers() == 2 && MatchManager.getParticipants().size() > 2;
        MatchTeam smallest = null;
        int players = 0;

        for (MatchTeam team : teamType.getTeams()) {
            if (EnumSetting.GAME_TYPE.is(GameType.BED_WARS) && BedUtils.isBedDestroyed(team)) continue;

            if (!team.isFull() || forced) {
                if (doubles) return team;

                int playing = team.playing();

                if (smallest == null) {
                    smallest = team;
                    players = playing;
                } else if (playing < players) {
                    smallest = team;
                    players = playing;
                }
            }
        }

        return smallest;
    }

    public boolean isStacked(TeamType teamType, MatchTeam currentTeam) {
        // Can't stack solo games, observers, or private server games
        if (teamType == TeamType.SOLO
            || this == MatchTeam.OBS
            || ServerUtils.isPrivate()) {
            return false;
        }

        MatchTeam smallestTeam = MatchTeam.getSmallestTeam(teamType, false);
        if (smallestTeam == null) return true;
        int smallestSize = smallestTeam.playing();

        // Return if this team will be at a +2 or more player advantage
        if (currentTeam == MatchTeam.OBS) {
            return ((this.playing() + 1) - smallestSize) >= 2;
        } else {
            return (this.playing() + 1) - Math.min(currentTeam.playing() - 1, smallestSize) >= 2;
        }
    }

    public int getScore() {
        return this.score;
    }

    public void addScore(int score) {
        this.addScore(score, false);
    }

    public void addScore(int score, boolean skipEvent) {
        if (!MatchState.isState(MatchState.STARTED) || TaskUtils.isRunning(RoundCountdown.getCountdown())) {
            return;
        }

        this.score += score;
        ScoreboardManager.updateTeamBoard();

        int scoreLimit = NumberSetting.SCORE_LIMIT.value();

        if (!skipEvent && this.score >= scoreLimit && scoreLimit > 0 && !MatchUtils.isElimination()) {
            Commons.callEvent(new RoundEndEvent());
        }
    }

    public int getRoundsWon() {
        return this.roundsWon;
    }

    public void addRoundWin() {
        this.roundsWon++;

        for (Player player : this.getPlayers()) {
            MatchManager.getParticipant(player).addRoundWin();
        }
    }

    public int getAlive() {
        return MatchUtils.isElimination() ? MatchUtils.getAlive(this) : 0;
    }

    public int getEliminatedScore() {
        int score = MatchEnd.ELIMINATED_TEAMS.indexOf(this);
        return score == -1 ? MatchEnd.ELIMINATED_TEAMS.size() : score;
    }

    public static Map<MatchTeam, Integer> getOrderedScores(boolean roundsWon) {
        TeamType teamType = EnumSetting.TEAM_TYPE.get();
        List<MatchTeam> ordered = new ArrayList<>(teamType.getTeams());

        if (roundsWon) {
            ordered.sort(Comparator.comparing(MatchTeam::getScore)
                .thenComparing(MatchTeam::getAlive)
                .thenComparing(MatchTeam::getEliminatedScore)
                .reversed());
        } else {
            ordered.sort(Comparator.comparing(MatchTeam::getRoundsWon)
                .thenComparing(MatchTeam::getAlive)
                .thenComparing(MatchTeam::getEliminatedScore)
                .reversed());
        }

        Map<MatchTeam, Integer> scores = new LinkedHashMap<>();

        for (MatchTeam team : ordered) {
            scores.put(team, team.getScore() + team.getAlive() + team.getEliminatedScore() + team.getRoundsWon());
        }

        return scores;
    }

    public static MatchTeam getTopTeam(Map<MatchTeam, Integer> scores) {
        int i = 0;
        MatchTeam team = null;
        int first = 0;
        int second = 0;

        for (Map.Entry<MatchTeam, Integer> entry : scores.entrySet()) {
            i++;

            if (i == 1) {
                first = entry.getValue();
                team = entry.getKey();
            } else if (i == 2) {
                second = entry.getValue();
            } else if (i == 3) {
                break;
            }
        }

        boolean tied = first == second;
        return tied ? null : team;
    }

    public static List<MatchTeam> getOrderedTeams(boolean roundsWon) {
        TeamType teamType = EnumSetting.TEAM_TYPE.get();
        List<MatchTeam> ordered = new ArrayList<>(teamType.getTeams());
        if (roundsWon) ordered.sort(Comparator.comparingInt(MatchTeam::getRoundsWon).reversed());
        else ordered.sort(Comparator.comparingInt(MatchTeam::getScore).reversed());

        boolean scoreTied = ordered.get(0).getScore() == ordered.get(1).getScore();

        if (scoreTied && MatchUtils.isElimination()) {
            return MatchTeam.getAliveOrdered();
        } else {
            return ordered;
        }
    }

    public static List<MatchTeam> getAliveOrdered() {
        TeamType teamType = EnumSetting.TEAM_TYPE.get();
        List<MatchTeam> ordered = new ArrayList<>(teamType.getTeams());
        ordered.sort(Comparator.comparingInt(MatchTeam::getAlive).reversed());
        return ordered;
    }

    public static MatchTeam getTopTeam(boolean roundsWon) {
        List<MatchTeam> ordered = MatchTeam.getOrderedTeams(roundsWon);
        boolean tied = ordered.get(0).getScore() == ordered.get(1).getScore();
        return tied ? null : ordered.get(0);
    }

    public static boolean isSameTeam(Player player1, Player player2) {
        return !EnumSetting.TEAM_TYPE.is(TeamType.SOLO) && MatchTeam.getTeam(player1) == MatchTeam.getTeam(player2);
    }

    public static MatchTeam getAllowedTeam(Player player) {
        for (MatchTeam team : MatchTeam.values()) {
            if (team == MatchTeam.OBS) continue;
            if (team.isAllowed(player)) return team;
        }

        return null;
    }

}
