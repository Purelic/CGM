package net.purelic.cgm.core.stats;

import net.purelic.cgm.CGM;
import net.purelic.cgm.core.constants.MatchTeam;
import net.purelic.cgm.core.gamemodes.CustomGameMode;
import net.purelic.cgm.core.gamemodes.EnumSetting;
import net.purelic.cgm.core.gamemodes.NumberSetting;
import net.purelic.cgm.core.gamemodes.constants.GameType;
import net.purelic.cgm.core.gamemodes.constants.TeamType;
import net.purelic.cgm.core.managers.MatchManager;
import net.purelic.cgm.core.maps.CustomMap;
import net.purelic.cgm.core.match.Participant;
import net.purelic.cgm.core.stats.constants.MatchResult;
import net.purelic.cgm.core.stats.leaderboard.Leaderboard;
import net.purelic.cgm.league.LeagueModule;
import net.purelic.commons.Commons;
import net.purelic.commons.profile.Profile;
import net.purelic.commons.utils.DatabaseUtils;
import net.purelic.commons.utils.ServerUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import shaded.com.google.cloud.Timestamp;
import shaded.com.google.cloud.firestore.FieldValue;
import shaded.com.google.cloud.firestore.SetOptions;

import java.text.DecimalFormat;
import java.util.*;

public class MatchStats {

    private final String matchId;
    private final Timestamp started;
    private Timestamp ended;
    private final String playlist;
    private final CustomMap map;
    private final CustomGameMode gameMode;
    private final GameType gameType;
    private final TeamType teamType;
    private int rounds;
    private final boolean roundBased;
    private Map<UUID, MatchPlacement> placements;
    private final Map<UUID, PlayerStats> stats;
    private MatchTeam winner;

    public MatchStats() {
        this(MatchManager.getCurrentMap(), MatchManager.getCurrentGameMode());
    }

    public MatchStats(CustomMap map, CustomGameMode gameMode) {
        this.matchId = UUID.randomUUID().toString();
        this.started = Timestamp.now();
        this.ended = this.started;
        this.playlist = ServerUtils.getPlaylist() == null ? CGM.get().getConfig().getString("fallback_playlist") : ServerUtils.getPlaylist();
        this.map = map;
        this.gameMode = gameMode;
        this.gameType = gameMode.getGameType();
        this.teamType = TeamType.valueOf(gameMode.getEnumSetting(EnumSetting.TEAM_TYPE));
        this.rounds = gameMode.getNumberSetting(NumberSetting.ROUNDS);
        this.roundBased = this.rounds > 1 && this.teamType != TeamType.SOLO;
        this.placements = new HashMap<>();
        this.stats = new HashMap<>();
        this.winner = null;
    }

    public void setWinner(MatchTeam winner) {
        this.winner = winner;
    }

    public void setPlacements(Map<UUID, MatchPlacement> placements) {
        this.placements = placements;
        this.ended = Timestamp.now();
        this.rounds = MatchManager.getRound();
    }

    public boolean hasStats(Player player) {
        return this.stats.containsKey(player.getUniqueId());
    }

    public PlayerStats getStats(Player player) {
        this.stats.putIfAbsent(player.getUniqueId(), new PlayerStats(player));
        return this.stats.get(player.getUniqueId());
    }

    public Map<String, Object> exportMatch() {
        Map<String, Object> data = new HashMap<>();
        data.put("id", this.matchId);
        data.put("title", this.gameMode.getName() + " on " + this.map.getName());
        data.put("started", this.started);
        data.put("ended", this.ended);
        data.put("length", this.ended.getSeconds() - this.started.getSeconds());
        data.put("playlist", this.playlist);
        data.put("map", this.map.getName());
        data.put("game_mode", this.gameMode.getName());
        data.put("game_type", this.gameType.name().toLowerCase());
        data.put("team_type", this.teamType.name().toLowerCase());
        data.put("round_based", this.roundBased);
        data.put("rounds", this.rounds);

        if (this.teamType == TeamType.SOLO) {
            List<Participant> orderedParticipants = MatchManager.getOrderedParticipants(true);
            Participant winner = MatchManager.getTopParticipant(true);

            List<Map<String, Object>> playerData = new ArrayList<>();
            boolean draw = winner == null;
            boolean useScore = orderedParticipants.get(0).getTotalScore() != 0;

            List<PlayerStats> ordered = new ArrayList<>(this.stats.values());
            this.orderStats(ordered, true);

            for (PlayerStats stats : ordered) {
                playerData.add(this.exportStats(stats));
            }

            data.put("show_scores", useScore);
            data.put("draw", draw);
            data.put("players", playerData);
            data.put("player_count", playerData.size());
        } else {
            Map<MatchTeam, Integer> ordered = MatchTeam.getOrderedScores(this.roundBased);
            List<MatchTeam> teams = new ArrayList<>(ordered.keySet());
            MatchTeam winner = MatchTeam.getTopTeam(ordered);

            List<Map<String, Object>> teamData = new ArrayList<>();
            boolean draw = winner == null;
            boolean useScore = teams.get(0).getScore() != 0;

            for (MatchTeam team : teams) {
                Map<String, Object> teamSummary = this.exportTeamSummary(team);
                List<Map<String, Object>> players = new ArrayList<>();
                List<PlayerStats> playerStats = new ArrayList<>();

                // get the players on the team
                for (Map.Entry<UUID, PlayerStats> entry : this.stats.entrySet()) {
                    PlayerStats stats = entry.getValue();
                    Player player = stats.getPlayer();
                    MatchTeam playerTeam = this.placements.containsKey(player.getUniqueId()) ? this.placements.get(player.getUniqueId()).getTeam() : stats.getTeam();
                    if (playerTeam == team) playerStats.add(this.stats.get(entry.getKey()));
                }

                // order the players by stats
                this.orderStats(playerStats, useScore);

                // add the player stats data to the team summary
                for (PlayerStats ps : playerStats) {
                    players.add(this.exportStats(ps));
                }

                teamSummary.put("players", players);
                teamSummary.put("player_count", players.size());
                teamData.add(teamSummary);
            }

            data.put("show_scores", useScore);
            data.put("draw", draw);
            data.put("teams", teamData);
        }

        data.put("top_performers", this.getTopPerformers());
        return data;
    }

    public Map<String, Object> exportMatchSummary(Player player) {
        MatchPlacement placement = this.placements.get(player.getUniqueId());
        Map<String, Object> data = new HashMap<>();

        data.put("match_id", this.matchId);
        data.put("started", this.started);
        data.put("ended", this.ended);
        data.put("length", this.ended.getSeconds() - this.started.getSeconds());
        data.put("playlist", this.playlist);
        data.put("map", this.map.getName());
        data.put("game_mode", this.gameMode.getName());
        data.put("game_type", this.gameType.name().toLowerCase());
        data.put("team_type", this.teamType.name().toLowerCase());
        data.put("team", placement.getTeam().name().toLowerCase());
        data.put("place", placement.getPlace());
        data.put("place_suffix", placement.getSuffix());
        data.put("match_result", placement.getRealResult().name().toLowerCase());
        data.put("stat_result", placement.getStatResult().name().toLowerCase());

        List<Map<String, Object>> teamScores = this.getTeamScores();
        data.put("scores", teamScores);
        data.put("show_scores", (int) teamScores.get(0).get("score") != 0);

        return data;
    }

    private List<Map<String, Object>> getTeamScores() {
        List<Map<String, Object>> scores = new ArrayList<>();
        this.teamType.getTeams().forEach(team -> scores.add(this.exportTeamSummary(team)));
        return scores;
    }

    private Map<String, Object> exportTeamSummary(MatchTeam team) {
        Map<String, Object> data = new HashMap<>();
        data.put("team_id", team.name().toLowerCase());
        data.put("team_name", team.getName());
        data.put("team_color", team.getColor().name().toLowerCase());
        data.put("score", this.roundBased ? team.getRoundsWon() : team.getScore());
        return data;
    }

    public Map<String, Object> exportStats(UUID uuid, boolean winStreak) {
        PlayerStats stats = this.stats.get(uuid);
        KillStats killStats = stats.getKillStats();
        boolean winner = this.isWinner(uuid);
        Map<String, Object> data = new HashMap<>();

        // match
        data.put("play_time", FieldValue.increment(this.ended.getSeconds() - stats.getCreated().getSeconds()));
        data.put("games_played", FieldValue.increment(1));
        if (!winner) {
            data.put("losses", FieldValue.increment(1));
            if (winStreak) data.put("win_streak", 0);
        } else {
            data.put("wins", FieldValue.increment(1));
            if (winStreak) data.put("win_streak", FieldValue.increment(1));
        }

        // combat
        data.put("kills", FieldValue.increment(killStats.getKills()));
        data.put("final_kills", FieldValue.increment(killStats.getFinalKills()));
        data.put("deaths", FieldValue.increment(killStats.getDeaths()));
        data.put("final_deaths", FieldValue.increment(killStats.getFinalDeaths()));
        data.put("suicides", FieldValue.increment(killStats.getSuicides()));
        data.put("assists", FieldValue.increment(stats.getAssists()));
        data.put("damage_received", FieldValue.increment(stats.getDamageReceived()));
        data.put("damage_dealt", FieldValue.increment(stats.getDamageDealt()));
        data.put("arrows_shot", FieldValue.increment(stats.getArrowsShot()));
        data.put("arrows_hit", FieldValue.increment(stats.getArrowsHit()));
        data.put("longest_shot", FieldValue.increment(stats.getLongestShot()));
        data.put("longest_kill", FieldValue.increment(stats.getLongestKill()));

        // kills
        Map<String, Object> killTypes = new HashMap<>();
        killStats.getKillTypes().forEach((killType, amount) -> killTypes.put(killType.name().toLowerCase(), FieldValue.increment(amount)));
        data.put("kill_types", killTypes);

        // deaths
        Map<String, Object> deathTypes = new HashMap<>();
        killStats.getDeathTypes().forEach((deathType, amount) -> deathTypes.put(deathType.name().toLowerCase(), FieldValue.increment(amount)));
        data.put("death_types", deathTypes);

        // weapons
        Map<String, Object> weapons = new HashMap<>();
        killStats.getWeapons().forEach((weapon, amount) -> weapons.put(weapon.name().toLowerCase(), FieldValue.increment(amount)));
        data.put("weapons", weapons);

        // medals
        Map<String, Object> medals = new HashMap<>();
        stats.getMedals().forEach((medal, amount) -> medals.put(medal.name().toLowerCase(), FieldValue.increment(amount)));
        data.put("medals", medals);

        // objectives
        data.put("beds", FieldValue.increment(stats.getBeds()));
        data.put("flags", FieldValue.increment(stats.getFlags()));
        data.put("heads_collected", FieldValue.increment(stats.getHeadsCollected()));
        data.put("heads_stolen", FieldValue.increment(stats.getHeadsStolen()));
        data.put("heads_recovered", FieldValue.increment(stats.getHeadsRecovered()));

        return data;
    }

    public Map<String, Object> exportStats(PlayerStats stats) {
        KillStats killStats = stats.getKillStats();
        Map<String, Object> data = this.exportPlayer(stats.getPlayer());

        // combat
        data.put("score", stats.getScore());
        data.put("kills", killStats.getKills());
        data.put("final_kills", killStats.getFinalKills());
        data.put("deaths", killStats.getDeaths());
        data.put("final_deaths", killStats.getFinalDeaths());
        data.put("suicides", killStats.getSuicides());
        data.put("assists", stats.getAssists());
        data.put("damage_received", this.formatRatio(stats.getDamageReceived(), false));
        data.put("damage_dealt", this.formatRatio(stats.getDamageDealt(), false));
        data.put("arrows_shot", stats.getArrowsShot());
        data.put("arrows_hit", stats.getArrowsHit());
        data.put("longest_shot", (int) stats.getLongestShot());
        data.put("longest_kill", (int) stats.getLongestKill());

        // ratios
        data.put("kd_ratio", this.formatRatio(stats.getKillDeathRatio(), false));
        data.put("kda_ratio", this.formatRatio(stats.getKillDeathAssistRatio(), false));
        data.put("fkd_ratio", this.formatRatio(stats.getFinalKillDeathRatio(), false));
        data.put("net_damage_ratio", this.formatRatio(stats.getNetDamageRatio(), false));
        data.put("net_damage", this.formatRatio(stats.getNetDamage(), false));
        data.put("arrow_accuracy", this.formatRatio(stats.getArrowAccuracy(), true));

        // kills
        Map<String, Object> killTypes = new HashMap<>();
        killStats.getKillTypes().forEach((killType, amount) -> killTypes.put(killType.name().toLowerCase(), amount));
        data.put("kill_types", killTypes);

        // deaths
        Map<String, Object> deathTypes = new HashMap<>();
        killStats.getDeathTypes().forEach((deathType, amount) -> deathTypes.put(deathType.name().toLowerCase(), amount));
        data.put("death_types", deathTypes);

        // weapons
        Map<String, Object> weapons = new HashMap<>();
        killStats.getWeapons().forEach((weapon, amount) -> weapons.put(weapon.name().toLowerCase(), amount));
        data.put("weapons", weapons);

        // killed
        List<Map<String, Object>> killedData = new ArrayList<>();
        killStats.getKilled().forEach((player, amount) -> {
            Map<String, Object> killed = this.exportPlayer(player);
            killed.put("amount", amount);
            killedData.add(killed);
        });
        data.put("kill_counts", killedData);
        if (killStats.getKilledMost() != null) data.put("killed_most", this.exportPlayer(killStats.getKilledMost()));

        // killed by
        List<Map<String, Object>> killedByData = new ArrayList<>();
        killStats.getKilledBy().forEach((player, amount) -> {
            Map<String, Object> killedBy = this.exportPlayer(player);
            killedBy.put("amount", amount);
            killedByData.add(killedBy);
        });
        data.put("death_counts", killedByData);
        if (killStats.getKilledByMost() != null)
            data.put("killed_by_most", this.exportPlayer(killStats.getKilledByMost()));

        // medals // TODO name, key, value
        Map<String, Object> medals = new HashMap<>();
        stats.getMedals().forEach((medal, amount) -> medals.put(medal.name().toLowerCase(), amount));
        data.put("medals", medals);

        data.put("best_killstreak", stats.getBestKillstreak());
        data.put("best_multi_kill", stats.getBestMultiKill());

        // objectives
        data.put("beds", stats.getBeds());
        data.put("flags", stats.getFlags());
        data.put("heads_collected", stats.getHeadsCollected());
        data.put("heads_stolen", stats.getHeadsStolen());
        data.put("heads_recovered", stats.getHeadsRecovered());

        return data;
    }

    private Map<String, Object> getTopPerformers() {
        Map<String, Object> data = new HashMap<>();

        Player topMultiKill = Collections.max(this.stats.entrySet(), Comparator.comparingInt(entry -> entry.getValue().getBestMultiKill())).getValue().getPlayer();
        Map<String, Object> topMultiKillData = this.exportPlayer(topMultiKill);
        topMultiKillData.put("amount", this.stats.get(topMultiKill.getUniqueId()).getBestMultiKill());
        data.put("multi_kill", topMultiKillData);

        Player topKillstreak = Collections.max(this.stats.entrySet(), Comparator.comparingInt(entry -> entry.getValue().getBestKillstreak())).getValue().getPlayer();
        Map<String, Object> topKillstreakData = this.exportPlayer(topKillstreak);
        topKillstreakData.put("amount", this.stats.get(topKillstreak.getUniqueId()).getBestKillstreak());
        data.put("killstreak", topKillstreakData);

        Player topKiller = Collections.max(this.stats.entrySet(), Comparator.comparingInt(entry -> entry.getValue().getKills())).getValue().getPlayer();
        Map<String, Object> topKillerData = this.exportPlayer(topKiller);
        topKillerData.put("amount", this.stats.get(topKiller.getUniqueId()).getKills());
        data.put("killer", topKillerData);

        Player topDamager = Collections.max(this.stats.entrySet(), Comparator.comparingDouble(entry -> entry.getValue().getDamageDealt())).getValue().getPlayer();
        Map<String, Object> topDamagerData = this.exportPlayer(topDamager);
        topDamagerData.put("amount", this.formatRatio(this.stats.get(topDamager.getUniqueId()).getDamageDealt(), false));
        data.put("damager", topDamagerData);

        Player topArrowAccuracy = Collections.max(this.stats.entrySet(), Comparator.comparingDouble(entry -> entry.getValue().getArrowAccuracy())).getValue().getPlayer();
        Map<String, Object> topArrowAccuracyData = this.exportPlayer(topArrowAccuracy);
        topArrowAccuracyData.put("amount", this.formatRatio(this.stats.get(topArrowAccuracy.getUniqueId()).getArrowAccuracy(), true));
        data.put("arrow_accuracy", topArrowAccuracyData);

        Player longestShot = Collections.max(this.stats.entrySet(), Comparator.comparingDouble(entry -> entry.getValue().getLongestShot())).getValue().getPlayer();
        Map<String, Object> longestShotData = this.exportPlayer(longestShot);
        longestShotData.put("amount", this.formatRatio(this.stats.get(longestShot.getUniqueId()).getLongestShot(), false));
        data.put("longest_shot", longestShotData);

        Player longestKill = Collections.max(this.stats.entrySet(), Comparator.comparingDouble(entry -> entry.getValue().getLongestKill())).getValue().getPlayer();
        Map<String, Object> longestKillData = this.exportPlayer(longestKill);
        longestKillData.put("amount", this.formatRatio(this.stats.get(longestKill.getUniqueId()).getLongestKill(), false));
        data.put("longest_kill", longestKillData);

        return data;
    }

    private Map<String, Object> exportPlayer(Player player) {
        Map<String, Object> data = new HashMap<>();
        data.put("uuid", player.getUniqueId().toString());
        data.put("name", player.getName());
        return data;
    }

    private void orderStats(List<PlayerStats> stats, boolean score) {
        if (score) stats.sort(Comparator.comparingInt(PlayerStats::getScore).reversed());
        else stats.sort(Comparator.comparingInt(PlayerStats::getKills).reversed());
    }

    private String formatRatio(double ratio, boolean percentage) {
        if (percentage) ratio *= 100;
        return new DecimalFormat("0.0").format(ratio) + (percentage ? "%" : "");
    }

    public void save() {
//        if (!this.stats.isEmpty()) {
//            for (Player player : Bukkit.getOnlinePlayers()) {
//                String truncated = this.matchId.length() > 15 ? this.matchId.substring(0, 10) + "..." : "";
//                String matchUrl = AnalyticsUtils.urlBuilder(player, "https://purelic.net/matches/" + this.matchId, "post_match_message");
//
//                ChatUtils.sendMessage(player,
//                    new ComponentBuilder("")
//                        .append(" MATCH STATS » ").color(ChatColor.AQUA).bold(true)
//                        .append("Detailed stats and top performers will be uploaded soon to: ").reset()
//                        .append("purelic.net/matches/" + truncated).color(ChatColor.AQUA)
//                        .event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("Click to Open").create()))
//                        .event(new ClickEvent(ClickEvent.Action.OPEN_URL, matchUrl))
//                );
//            }
//        }

        Map<UUID, Integer> leagueRatings = new HashMap<>();

        for (Map.Entry<UUID, PlayerStats> entry : this.stats.entrySet()) {
            UUID uuid = entry.getKey();
            Map<String, Object> data = new HashMap<>();
            Map<String, Object> stats = this.exportStats(uuid, false);
            Map<String, Object> statsDetailed = this.exportStats(uuid, true);

            Map<String, Object> gameTypeStats = new HashMap<>();
            gameTypeStats.put("total", statsDetailed);
            gameTypeStats.put(this.teamType.name().toLowerCase(), stats);

            data.put("total", stats);
            data.put(this.gameType.name().toLowerCase(), gameTypeStats);

            boolean everyoneTied = this.placements.values().stream().allMatch(MatchPlacement::isTied);

            if (ServerUtils.isRanked() && !everyoneTied) {
                MatchPlacement placement = this.placements.get(uuid);
                int place;

                if (placement != null) {
                    place = placement.getPlace();
                } else {
                    int totalPlaces = LeagueModule.get().getTotalPlaces();

                    if (this.teamType == TeamType.SOLO) { // offline solo players get last place
                        place = totalPlaces;
                    } else { // offline team players get placed with their team
                        place = this.placements.values().stream()
                            .filter(matchPlacement -> matchPlacement.getTeam() == this.stats.get(uuid).getTeam())
                            .findAny() // Try to find a placement of a teammate
                            .map(MatchPlacement::getPlace) // Use the teammates place if there is one
                            .orElse(totalPlaces) // Fallback to last place if the entire team disconnected
                            ;
                    }
                }

                int rating = LeagueModule.get().getRating(uuid, place);

                Map<String, Object> playlistStats = new HashMap<>();
                statsDetailed.put("rating", rating);
                playlistStats.put(ServerUtils.getPlaylistId(), statsDetailed);

                Map<String, Object> rankedStats = new HashMap<>();
                rankedStats.put("total", stats);
                rankedStats.put(ServerUtils.getRankedSeason(), playlistStats);

                data.put("ranked", rankedStats);
                leagueRatings.put(uuid, rating);
            }

            Player player = Bukkit.getPlayer(uuid);
            Profile profile = Commons.getProfile(uuid);
            List<Map<String, Object>> recentMatches = profile.getRecentMatches();

            if (player != null && this.placements.containsKey(uuid)) {
                Map<String, Object> summary = this.exportMatchSummary(player);

                recentMatches.add(summary);
                recentMatches.sort(Comparator.comparing(MatchStats::getMatchTimestamp));
                Collections.reverse(recentMatches);

                if (recentMatches.size() > 21) {
                    recentMatches.subList(21, recentMatches.size()).clear();
                }

                profile.setRecentMatches(recentMatches);
            }

            Map<String, Object> finalData = new HashMap<>();
            finalData.put("stats", data);
            finalData.put("recent_matches", recentMatches);
            DatabaseUtils.getFirestore().collection("players").document(uuid.toString()).set(finalData, SetOptions.merge());
        }

        if (!leagueRatings.isEmpty()) {
            Leaderboard leaderboard = LeagueModule.get().getLeaderboard();
            if (leaderboard != null) leaderboard.updateLeaders(leagueRatings);
        }

        if (this.stats.isEmpty()) return;

        Map<String, Object> match = this.exportMatch();
        DatabaseUtils.getFirestore().collection("matches").document(this.matchId).set(match);
    }

    private boolean isWinner(UUID uuid) {
        MatchPlacement placement = this.placements.get(uuid);
        PlayerStats stats = this.stats.get(uuid);
        return placement != null ? placement.getStatResult() == MatchResult.WIN : stats.getTeam() == this.winner;
    }

    private static Timestamp getMatchTimestamp(Map<String, Object> data) {
        return (Timestamp) data.get("started");
    }

}
