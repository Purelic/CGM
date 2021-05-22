package net.purelic.cgm.core.managers;

import net.purelic.cgm.CGM;
import net.purelic.cgm.core.gamemodes.CustomGameMode;
import net.purelic.cgm.core.maps.CustomMap;
import net.purelic.cgm.core.runnables.VotingCountdown;
import net.purelic.commons.utils.ItemCrafter;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class VoteManager {

    private static final Material VOTE_ITEM = Material.SLIME_BALL;
    private static final Material VOTED_ITEM = Material.MAGMA_CREAM;
    private static final int NUM_OPTIONS = 5;
    private static int minPlayers = 2;
    private static boolean canceled = false;

    private final Map<Player, String> voted;
    private final Map<String, Integer> votes;
    private static final Map<String, Map<CustomMap, CustomGameMode>> options = new HashMap<>();
    private final Set<String> modes = new HashSet<>();
    private VotingCountdown votingCountdown;

    public VoteManager(CGM plugin) {
        this.voted = new HashMap<>();
        this.votes = new HashMap<>(NUM_OPTIONS);
    }

    public void startVoting(int seconds, boolean forced) {
        this.voted.clear();
        this.votes.clear();
        options.clear();
        this.modes.clear();

        Map<CustomMap, Set<CustomGameMode>> playlist = MapManager.getPlaylist();
        List<CustomMap> maps = new ArrayList<>(playlist.keySet());
        Collections.shuffle(maps);

        for (int i = 0; i < Math.min(MapManager.getPlaylist().size(), NUM_OPTIONS); i++) {
            CustomMap map = maps.get(i);
            CustomGameMode gameMode = this.getRandomGameMode(new ArrayList<>(playlist.get(map)));

            Map<CustomMap, CustomGameMode> option = new HashMap<>();
            option.put(map, gameMode);

            options.put(map.getName(), option);
            this.votes.put(map.getName(), 0);
            this.modes.add(gameMode.getName());
        }

        if (this.votingCountdown != null) {
            this.votingCountdown.cancel();
            VotingCountdown.clearVotingItems();
        }

        this.votingCountdown = new VotingCountdown(seconds, forced);
        this.votingCountdown.runTaskTimer(CGM.getPlugin(), 0, 20);
        Bukkit.getOnlinePlayers().forEach(VoteManager::getVotingItems);

        ScoreboardManager.resetScores(0);
        ScoreboardManager.setDisplayName("Voting");
        this.updateScoreboard();
    }

    public static void setMinPlayers(int players) {
        minPlayers = players;
    }

    public static int getMinPlayers() {
        return minPlayers;
    }

    public static boolean isCanceled() {
        return canceled;
    }

    public static void setCanceled(boolean canceled) {
        VoteManager.canceled = canceled;
    }

    public VotingCountdown getVotingCountdown() {
        return this.votingCountdown;
    }

    private CustomGameMode getRandomGameMode(List<CustomGameMode> gameModes) {
        CustomGameMode gameMode = gameModes.get(new Random().nextInt(gameModes.size()));

        if (this.modes.contains(gameMode.getName())) return gameModes.get(new Random().nextInt(gameModes.size())); // try again
        else return gameMode;
    }

    public static void getVotingItems(Player player) {
        int index = 2;

        for (Map<CustomMap, CustomGameMode> option : options.values()) {
            for (CustomMap map : option.keySet()) {
                ItemStack item = new ItemCrafter(VOTE_ITEM)
                        .name(option.get(map).getColoredName() + " on " + map.getColoredName())
                        .setTag("map", map.getName())
                        .craft();
                player.getInventory().setItem(index++, item);
            }
        }
    }

    private void updateScoreboard() {
        int i = 0;
        for (Map<CustomMap, CustomGameMode> option : options.values()) {
            for (CustomMap map : option.keySet()) {
                String mapName = map.getName();
                ScoreboardManager.setScore(
                        i++,
                        this.getVotesOf(mapName) + "  " + map.getColoredName());
            }
        }
    }

    public Map<CustomMap, CustomGameMode> getTopOption() {
        String mapName = this.votes.entrySet().stream()
                .min(Map.Entry.comparingByValue(Comparator.reverseOrder())).get().getKey();
        return options.get(mapName);
    }

    public int getVotesOf(String map) {
        // TODO Return a string instead. Green number if the map is in first place
        // Or have an event that updates the scoreboard after voting ends to change the row with the top map to green
        return this.votes.get(map);
    }

    public boolean votedFor(Player player, String map) {
        if (!this.voted.containsKey(player)) return false;
        return this.voted.get(player).equals(map);
    }

    public void removeVote(Player player) {
        if (this.voted.containsKey(player)) {
            this.votes.put(this.voted.get(player), this.votes.get(this.voted.get(player)) - 1);
            this.voted.remove(player);
            this.updateScoreboard();
        }
    }

    public void vote(Player player, String map) {
        if (this.votedFor(player, map)) return;

        for (ItemStack item : player.getInventory().getContents()) {
            if (!new ItemCrafter(item).getTag("map").isEmpty()) {
                item.setType(VOTE_ITEM);
            }
        }

        player.getItemInHand().setType(VOTED_ITEM);
        player.updateInventory();
        player.playSound(player.getLocation(), Sound.ORB_PICKUP, 1.0F, 1.0F);

        if (!this.voted.containsKey(player)) {
            this.voted.put(player, map);
            this.votes.put(map, this.votes.get(map) + 1);
        } else {
            this.votes.put(map, this.votes.get(map) + 1);
            this.votes.put(this.voted.get(player), this.votes.get(this.voted.get(player)) - 1);
            this.voted.put(player, map);
        }

        this.updateScoreboard();
    }

}
