package net.purelic.cgm.league;

import net.md_5.bungee.api.ChatColor;
import net.purelic.cgm.CGM;
import net.purelic.cgm.commands.league.ReRollCommand;
import net.purelic.cgm.commands.league.ReadyCommand;
import net.purelic.cgm.core.constants.MatchTeam;
import net.purelic.cgm.core.gamemodes.CustomGameMode;
import net.purelic.cgm.core.gamemodes.EnumSetting;
import net.purelic.cgm.core.gamemodes.constants.TeamType;
import net.purelic.cgm.core.managers.MatchManager;
import net.purelic.cgm.core.maps.CustomMap;
import net.purelic.cgm.core.rewards.RewardBuilder;
import net.purelic.cgm.core.stats.MatchStats;
import net.purelic.cgm.core.stats.leaderboard.Leaderboard;
import net.purelic.cgm.events.match.MatchCycleEvent;
import net.purelic.cgm.events.match.RoundStartEvent;
import net.purelic.cgm.events.participant.MatchTeamEliminateEvent;
import net.purelic.cgm.events.participant.ParticipantEliminateEvent;
import net.purelic.cgm.listeners.match.MatchEnd;
import net.purelic.cgm.listeners.modules.DynamicModule;
import net.purelic.cgm.listeners.modules.stats.MatchStatsModule;
import net.purelic.cgm.utils.MatchUtils;
import net.purelic.cgm.utils.PlaceUtils;
import net.purelic.commons.Commons;
import net.purelic.commons.profile.Profile;
import net.purelic.commons.profile.Rank;
import net.purelic.commons.utils.DatabaseUtils;
import net.purelic.commons.utils.PlayerUtils;
import net.purelic.commons.utils.ServerUtils;
import net.purelic.commons.utils.constants.ServerStatus;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import shaded.com.google.api.core.ApiFuture;
import shaded.com.google.cloud.firestore.DocumentReference;
import shaded.com.google.cloud.firestore.DocumentSnapshot;
import shaded.com.google.cloud.firestore.ListenerRegistration;

import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

@SuppressWarnings("unchecked")
public class LeagueModule implements DynamicModule {

    private static LeagueModule instance;

    private final Map<MatchTeam, LeagueTeam> teams;
    private final Map<UUID, LeagueTeam> players;
    private List<Map<String, Object>> rawPlayerData;
    private ListenerRegistration registration;
    private Map<UUID, Integer> ratings;

    public LeagueModule() {
        this.teams = new HashMap<>();
        this.players = new HashMap<>();
        this.rawPlayerData = new ArrayList<>();
        this.registration = null;
        this.ratings = new HashMap<>();

        instance = this;
    }

    public static LeagueModule get() {
        return instance;
    }

    public boolean isPlaying(Player player) {
        return this.players.containsKey(player.getUniqueId());
    }

    public MatchTeam getTeam(Player player) {
        return this.getTeam(player.getUniqueId());
    }

    public MatchTeam getTeam(UUID uuid) {
        return this.players.get(uuid).getMatchTeam();
    }

    public Set<UUID> getPlayers() {
        return this.players.keySet();
    }

    public void loadListenerRegistration() {
        this.registration = DatabaseUtils.serverDoc.addSnapshotListener((snapshot, error) -> {
            if (error != null) {
                System.err.println("Listen failed!");
                error.printStackTrace();
                return;
            }

            if (snapshot != null && snapshot.exists() && snapshot.getData() != null) {
                Map<String, Object> data = snapshot.getData();
                boolean shutdown = (boolean) data.get("shutdown");
                List<Map<String, Object>> rankedPlayers = (List<Map<String, Object>>) data.getOrDefault("ranked_players", new ArrayList<>());

                if (shutdown) {
                    this.registration.remove();
                    return;
                }

                // Only load teams if we haven't loaded any yet (teams list is empty)
                if (!rankedPlayers.isEmpty() && this.teams.isEmpty()) {
                    // set the raw player/team data
                    this.rawPlayerData = rankedPlayers;

                    // cycle map so we can load the game settings
                    this.cycleRandom();

                    // load players into match teams
                    this.loadPlayers();
                }
            }
        });
    }

    private void loadPlayers() {
        this.teams.clear();
        this.players.clear();

        boolean solo = EnumSetting.TEAM_TYPE.is(TeamType.SOLO);
        int index = 2; // skips obs and ffa team

        // set players to match/league teams
        for (Map<String, Object> teamData : this.rawPlayerData) {
            List<String> players = (List<String>) teamData.get("players");
            List<UUID> uuids = players.stream().map(UUID::fromString).collect(Collectors.toList());
            String name = (String) teamData.get("name"); // custom team name

            LeagueTeam leagueTeam;

            if (solo) {
                MatchTeam matchTeam = MatchTeam.SOLO;

                // combine with existing solo team
                if (this.teams.containsKey(matchTeam)) {
                    leagueTeam = this.teams.get(matchTeam);
                    leagueTeam.addPlayers(uuids);
                } else {
                    leagueTeam = new LeagueTeam(matchTeam, uuids);
                    this.teams.put(matchTeam, leagueTeam);
                }
            } else {
                MatchTeam matchTeam = MatchTeam.values()[index];
                leagueTeam = new LeagueTeam(matchTeam, uuids, name);
                this.teams.put(matchTeam, leagueTeam);
                index++;
            }

            uuids.forEach(uuid -> this.players.put(uuid, leagueTeam));
        }

        // handle custom team names for non-solo matches
        if (!solo) {
            for (LeagueTeam leagueTeam : this.teams.values()) {
                leagueTeam.getMatchTeam().setName(leagueTeam.getName());
            }
        }

        // join all players that are online
        for (Map.Entry<UUID, LeagueTeam> entry : this.players.entrySet()) {
            UUID uuid = entry.getKey();
            MatchTeam team = entry.getValue().getMatchTeam();
            Player player = Bukkit.getPlayer(uuid);
            if (player == null || !player.isOnline() || MatchTeam.getTeam(player) == team) continue;
            PlayerUtils.performCommand(player, "join " + team.getName());
        }
    }

    public void cycleRandom() {
        ReadyCommand.TEAMS_READY.clear();
        ReadyCommand.PLAYERS_READY.clear();
        ReRollCommand.TEAMS_VOTED.clear();
        ReRollCommand.PLAYERS_VOTED.clear();

        Map<CustomMap, List<CustomGameMode>> playlist = CGM.getPlaylist().getPool();
        List<CustomMap> maps = new ArrayList<>(playlist.keySet());
        Collections.shuffle(maps);

        CustomMap currentMap = MatchManager.getCurrentMap();

        for (int i = 0; i < playlist.size(); i++) {
            CustomMap map = maps.get(i);

            if (map == currentMap && i != playlist.size() - 1) continue;

            List<CustomGameMode> gameModes = new ArrayList<>(playlist.get(map));
            CustomGameMode gameMode = gameModes.get(new Random().nextInt(gameModes.size()));
            MatchManager.setNext(map, gameMode);
            break;
        }
    }

    public Leaderboard getLeaderboard() {
        String id = ServerUtils.getRankedSeason() + "__" + ServerUtils.getPlaylistId();

        DocumentReference docRef = DatabaseUtils.getFirestore().collection("leaderboards").document(id);
        ApiFuture<DocumentSnapshot> future = docRef.get();
        DocumentSnapshot document;

        try {
            document = future.get();

            if (document.exists() && document.getData() != null) {
                return new Leaderboard(id, document.getData());
            } else {
                return new Leaderboard(id, ServerUtils.getPlaylist());
            }
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
            return null;
        }
    }

    public double getRatingWeight(UUID player, boolean win) {
        // get each team's avg rating
        double teamAvg;
        double enemyAvg;

        if (this.teams.size() == 1) {
            int rating = Commons.getProfile(player).getRating();
            teamAvg = rating;

            int total = this.teams.get(MatchTeam.SOLO).getTotalRating() - rating;
            enemyAvg = total / (double) (this.teams.get(MatchTeam.SOLO).getPlayers().size() - 1);
        } else {
            MatchTeam team = this.players.get(player).getMatchTeam();
            teamAvg = this.teams.get(team).getAverageRating();

            List<LeagueTeam> enemies = new ArrayList<>(this.teams.values());
            enemies.remove(this.teams.get(team));
            enemyAvg = this.getAverageEnemyRating(enemies);
        }

        // apply weight based on difference between the avg ratings
        if (win) return Math.max(enemyAvg, 100) / Math.max(teamAvg, 100);
        else return Math.max(teamAvg, 100) / Math.max(enemyAvg, 100);
    }

    public int getTotalPlaces() {
        return this.teams.size() == 1 ? this.teams.get(MatchTeam.SOLO).getPlayers().size() : this.teams.size();
    }

    private double getAverageEnemyRating(List<LeagueTeam> teams) {
        int total = teams.stream().mapToInt(LeagueTeam::getTotalRating).sum();
        return total / (double) teams.size();
    }

    public int getRating(UUID uuid, int place) {
        if (this.ratings.containsKey(uuid)) return this.ratings.get(uuid);

        Player player = Bukkit.getPlayer(uuid);
        Profile profile = Commons.getProfile(uuid);
        int rating = profile.getRating();
        Rank rank = profile.getLeagueRank();

        double points = 0;
        int totalPlaces = this.getTotalPlaces();
        boolean odd = totalPlaces % 2 != 0;
        double topPlacementRange = totalPlaces / 2D;
        boolean win = (odd && place + 0.5D == topPlacementRange) || place <= topPlacementRange;

        double ratingWeight = this.getRatingWeight(uuid, win);

        // points for win/loss
        if (win) {
            if (rank == Rank.IRON) points += 35;
            else if (rank == Rank.GOLD_LEAGUE) points += 30;
            else if (rank == Rank.DIAMOND) points += 25;
            else if (rank == Rank.EMERALD) points += 20;
            else if (rank == Rank.QUARTZ) points += 15;
        } else {
            if (rank == Rank.IRON) points -= 15;
            else if (rank == Rank.GOLD_LEAGUE) points -= 20;
            else if (rank == Rank.DIAMOND) points -= 25;
            else if (rank == Rank.EMERALD) points -= 30;
            else if (rank == Rank.QUARTZ) points -= 35;
        }

        // apply the rating weight
        points *= ratingWeight;

        // apply the placement weight
        double basePercent = 1D / (int) topPlacementRange;

        // odd number of placements
        if (odd && place + 0.5D == topPlacementRange) {
            // middle place
            points *= basePercent;
        } else if (place <= topPlacementRange) { // top half
            int dist = ((int) topPlacementRange) - place + 1;
            points *= (basePercent * dist);
        } else { // bottom half
            int dist = place - ((int) topPlacementRange);
            points *= (basePercent * dist);
        }

        // Example placement weights
        // 1st - 100%
        // 2nd - 66%
        // 3rd - 33%
        // 4th - 33%
        // 5th - 66%
        // 6th - 100%

        // 1st - 100%
        // 2nd - 100%

        // 1st - 100%
        // 2nd - 50%
        // 3rd - 50%
        // 4th - 100%

        // set min/max points to be +/- 5-50
        if (win) {
            points = Math.min(points, 50);
            points = Math.max(points, 5);
        } else {
            points = Math.max(points, -50);
            points = Math.min(points, -5);
        }

        // set the new rating
        int newRating = Math.max(rating + ((int) points), 0);
        profile.setRating(newRating);

        if (player != null && player.isOnline()) {
            // send the player a reward message
            String placement = totalPlaces == 2 ? (win ? "Win" : "Loss") : place + PlaceUtils.getPlaceSuffix(place) + " Place";
            new RewardBuilder(player, ((int) points), "ELO", "League " + placement, true).reward();
            Rank newRank = profile.getLeagueRank();

            // update the player if their rank changed
            if (newRank != rank) {
                if (newRating < rating) {
                    player.sendMessage("" + ChatColor.RED + ChatColor.BOLD + " DEMOTED " + ChatColor.RESET + ChatColor.GRAY + "» " +
                        ChatColor.RESET + "You've been demoted from " + rank.getFlair() + ChatColor.RESET + " " + rank.getName(false) +
                        " to " + newRank.getFlair() + ChatColor.RESET + " " + newRank.getName(false) + "!");
                } else {
                    player.sendMessage("" + ChatColor.GREEN + ChatColor.BOLD + " PROMOTED " + ChatColor.RESET + ChatColor.GRAY + "» " +
                        ChatColor.RESET + "You've been promoted from " + rank.getFlair() + ChatColor.RESET + " " + rank.getName(false) +
                        " to " + newRank.getFlair() + ChatColor.RESET + " " + newRank.getName(false) + "!");
                }
            }
        }

        this.ratings.put(uuid, newRating);
        return newRating;
    }

    public void reset() {
        Map<String, Object> data = new HashMap<>();
        data.put("status", ServerStatus.STARTING.name());
        data.put("map", null);
        data.put("game_mode", null);
        data.put("whitelisted", false);
        data.put("ranked_players", new ArrayList<>());
        ServerUtils.update(data);

        Bukkit.setWhitelist(false);
        Bukkit.getWhitelistedPlayers().clear();

        this.rawPlayerData.clear();
        this.teams.clear();
        this.players.clear();
        this.ratings.clear();
        ReadyCommand.TEAMS_READY.clear();
        ReadyCommand.PLAYERS_READY.clear();
        ReRollCommand.TEAMS_VOTED.clear();
        ReRollCommand.PLAYERS_VOTED.clear();
        ReRollCommand.REROLLED = false;
    }

    @Override
    public boolean isValid() {
        return ServerUtils.isRanked();
    }

    @EventHandler
    public void onMatchCycle(MatchCycleEvent event) {
        if (event.hasMap()) {
            MatchStatsModule.setCurrent(new MatchStats());
            this.loadPlayers();
        }
    }

    @EventHandler (priority = EventPriority.HIGH) // needs to run after the listener in MatchEnd
    public void onMatchTeamEliminate(MatchTeamEliminateEvent event) {
        // automatic elo doesn't apply to multi-round elimination game modes
        if (!MatchUtils.hasRounds()) return;

        int places = this.getTotalPlaces();
        int place = places - MatchEnd.ELIMINATED_TEAMS.indexOf(event.getTeam());
        this.teams.get(event.getTeam()).getPlayers().forEach(player -> {
            this.getRating(player, place); // automatic elo
        });
    }

    @EventHandler
    public void onRoundStart(RoundStartEvent event) {
        // add all the offline solo players to the eliminated list
        if (MatchUtils.isElimination() && EnumSetting.TEAM_SIZE.is(TeamType.SOLO)) {
            for (UUID player : this.players.keySet()) {
                if (Bukkit.getPlayer(player) == null
                    || !Bukkit.getPlayer(player).isOnline()
                    || MatchManager.getParticipant(Bukkit.getPlayer(player)) == null) {
                    MatchEnd.ELIMINATED_PLAYERS.add(player);
                }
            }
        }
    }

    @EventHandler (priority = EventPriority.HIGH) // needs to run after the listener in MatchEnd
    public void onParticipantEliminate(ParticipantEliminateEvent event) {
        // automatic elo doesn't apply to multi-round elimination game modes
        if (!MatchUtils.hasRounds()) return;

        UUID uuid = event.getPlayer().getUniqueId();
        int places = this.getTotalPlaces();
        int place = places - MatchEnd.ELIMINATED_PLAYERS.indexOf(uuid);
        this.getRating(uuid, place); // automatic elo
    }

}
