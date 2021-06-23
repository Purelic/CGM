package net.purelic.cgm.core.managers;

import net.md_5.bungee.api.ChatColor;
import net.purelic.cgm.CGM;
import net.purelic.cgm.commands.league.ReRollCommand;
import net.purelic.cgm.commands.league.ReadyCommand;
import net.purelic.cgm.core.constants.MatchTeam;
import net.purelic.cgm.core.gamemodes.CustomGameMode;
import net.purelic.cgm.core.maps.CustomMap;
import net.purelic.cgm.core.rewards.RewardBuilder;
import net.purelic.cgm.core.stats.MatchStats;
import net.purelic.cgm.core.stats.leaderboard.Leaderboard;
import net.purelic.cgm.listeners.modules.stats.MatchStatsModule;
import net.purelic.commons.Commons;
import net.purelic.commons.profile.Profile;
import net.purelic.commons.profile.Rank;
import net.purelic.commons.utils.DatabaseUtils;
import net.purelic.commons.utils.ServerUtils;
import net.purelic.commons.utils.constants.ServerStatus;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import shaded.com.google.api.core.ApiFuture;
import shaded.com.google.cloud.firestore.DocumentReference;
import shaded.com.google.cloud.firestore.DocumentSnapshot;
import shaded.com.google.cloud.firestore.ListenerRegistration;

import java.util.*;
import java.util.concurrent.ExecutionException;

public class LeagueManager {

    private static List<Map<String, Object>> teamData = new ArrayList<>();
    private static Map<MatchTeam, List<UUID>> teams = new HashMap<>();
    private static Map<UUID, MatchTeam> players = new HashMap<>();
    private static ListenerRegistration registration = null;
    private static Leaderboard leaderboard = null;

    public static void loadListenerRegistration() {
        registration = DatabaseUtils.serverDoc.addSnapshotListener((snapshot, e) -> {
            if (e != null) {
                System.err.println("Listen failed: " + e);
                return;
            }

            if (snapshot != null && snapshot.exists()) {
                Map<String, Object> data = snapshot.getData();
                boolean shutdown = (boolean) data.get("shutdown");
                List<Map<String, Object>> rankedPlayers = (List<Map<String, Object>>) data.getOrDefault("ranked_players", new ArrayList<>());

                if (shutdown) {
                    registration.remove();
                    return;
                }

                if (!rankedPlayers.isEmpty()) {
                    loadTeams(rankedPlayers);
                }
            }
        });
    }

    public static boolean isPlaying(Player player) {
        return getTeam(player) != null;
    }

    public static MatchTeam getTeam(Player player) {
        return getTeam(player.getUniqueId());
    }

    public static MatchTeam getTeam(UUID uuid) {
        return players.get(uuid);
    }

    public static Map<UUID, MatchTeam> getPlayers() {
        return players;
    }

    public static void loadTeams(List<Map<String, Object>> teams) {
        if (!teamData.isEmpty()) return;
        teamData = teams;
        loadTeams();
    }

    public static void loadTeams() {
        for (Map<String, Object> teamData : teamData) {
            MatchTeam team = MatchTeam.valueOf((String) teamData.get("id"));
            List<String> players = (List<String>) teamData.get("players");
            LeagueManager.teams.put(team, new ArrayList<>());
            players.forEach(player -> {
                UUID uuid = UUID.fromString(player);
                LeagueManager.teams.get(team).add(uuid);
                LeagueManager.players.put(uuid, team);
            });
        }

        // cycle map
        cycleRandom();
    }

    private static void updateTeams() {
        for (Map<String, Object> teamData : teamData) {
            // Update the team
            MatchTeam team = MatchTeam.valueOf((String) teamData.get("id"));
            String name = (String) teamData.getOrDefault("name", team.getDefaultName());
            team.setName(name);
        }

        for (Map.Entry<UUID, MatchTeam> entry : players.entrySet()) {
            UUID uuid = entry.getKey();
            MatchTeam team = entry.getValue();
            Player player = Bukkit.getPlayer(uuid);
            if (player == null || !player.isOnline() || MatchTeam.getTeam(player) == team) continue;
            player.performCommand("join " + team.name());
        }
    }

    public static void reset() {
        Map<String, Object> data = new HashMap<>();
        data.put("status", ServerStatus.STARTING.name());
        data.put("map", null);
        data.put("game_mode", null);
        data.put("whitelisted", false);
        data.put("ranked_players", new ArrayList<>());
        ServerUtils.update(data);

        Bukkit.setWhitelist(false);
        Bukkit.getWhitelistedPlayers().clear();

        teamData.clear();
        teams.clear();
        players.clear();
        ReadyCommand.READY.clear();
        ReRollCommand.VOTED.clear();
    }

    public static void cycleRandom() {
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

        new BukkitRunnable() {
            @Override
            public void run() {
                MatchManager.cycle();
                MatchStatsModule.setCurrent(new MatchStats());
                updateTeams();
            }
        }.runTaskLater(CGM.get(), 20L);
    }

    public static double getEloWeight(MatchTeam team, boolean win) {
        // get each team's avg rating
        double teamAvg = getAverageRating(team);
        double enemyAvg = getAverageRating(team == MatchTeam.BLUE ? MatchTeam.RED : MatchTeam.BLUE);

        // apply weight based on difference between the avg ratings
        if (win) return Math.max(enemyAvg, 100) / Math.max(teamAvg, 100);
        else return Math.max(teamAvg, 100) / Math.max(enemyAvg, 100);
    }

    public static int getRating(UUID uuid, boolean win, double weight) {
        Player player = Bukkit.getPlayer(uuid);
        Profile profile = Commons.getProfile(uuid);
        int rating = profile.getRating();
        Rank rank = profile.getLeagueRank();

        int points = 0;

        // points for win/loss
        if (win) {
            if (rank == Rank.IRON) points += 35;
            else if (rank == Rank.GOLD) points += 30;
            else if (rank == Rank.DIAMOND) points += 25;
            else if (rank == Rank.EMERALD) points += 20;
            else if (rank == Rank.QUARTZ) points += 15;
        } else {
            if (rank == Rank.IRON) points -= 15;
            else if (rank == Rank.GOLD) points -= 20;
            else if (rank == Rank.DIAMOND) points -= 25;
            else if (rank == Rank.EMERALD) points -= 30;
            else if (rank == Rank.QUARTZ) points -= 35;
        }

        points *= weight;

        if (win) {
            points = Math.min(points, 50);
            points = Math.max(points, 5);
        } else {
            points = Math.max(points, -50);
            points = Math.min(points, -5);
        }

        // set the new rating
        int newRating = Math.max(rating + points, 0);
        profile.setRating(newRating);

        if (player != null && player.isOnline()) {
            new RewardBuilder(player, points, "ELO","League " + (win ? "Win" : "Loss"), true).reward();
            Rank newRank = profile.getLeagueRank();
            if (newRank != rank) {
                if (newRating < rating) player.sendMessage("" + ChatColor.RED + ChatColor.BOLD + " DEMOTED " + ChatColor.RESET + ChatColor.GRAY + "» " + ChatColor.RESET + "You've been demoted from " + rank.getFlair() + ChatColor.RESET + " " + rank.getName(false) + " to " + newRank.getFlair() + ChatColor.RESET + " " + newRank.getName(false) + "!");
                else player.sendMessage("" + ChatColor.GREEN + ChatColor.BOLD + " PROMOTED " + ChatColor.RESET + ChatColor.GRAY + "» " + ChatColor.RESET + "You've been promoted from " + rank.getFlair() + ChatColor.RESET + " " + rank.getName(false) + " to " + newRank.getFlair() + ChatColor.RESET + " " + newRank.getName(false) + "!");
            }
        }

        return newRating;
    }

    private static double getAverageRating(MatchTeam team) {
        List<UUID> players = teams.get(team);
        int total = players.stream().mapToInt(player -> Commons.getProfile(player).getRating()).sum();
        return total / (double) players.size();
    }

    public static void updateLeaderboard(Map<UUID, Integer> ratings) {
        if (leaderboard == null) {
            String id = ServerUtils.getRankedSeason() + "__" + ServerUtils.getPlaylistId();

            DocumentReference docRef = DatabaseUtils.getFirestore().collection("leaderboards").document(id);
            ApiFuture<DocumentSnapshot> future = docRef.get();
            DocumentSnapshot document;

            try {
                document = future.get();

                if (document.exists()) {
                    leaderboard = new Leaderboard(id, document.getData());
                } else {
                    leaderboard = new Leaderboard(id, ServerUtils.getPlaylist());
                }
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        }

        leaderboard.updateLeaders(ratings);
    }

}
