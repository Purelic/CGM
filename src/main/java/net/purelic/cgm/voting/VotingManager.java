package net.purelic.cgm.voting;

import net.purelic.cgm.core.constants.MatchState;
import net.purelic.cgm.core.gamemodes.CustomGameMode;
import net.purelic.cgm.core.managers.ScoreboardManager;
import net.purelic.cgm.core.maps.CustomMap;
import net.purelic.cgm.kit.VotingKit;
import net.purelic.cgm.server.Playlist;
import net.purelic.cgm.utils.SoundUtils;
import net.purelic.commons.utils.ItemCrafter;
import net.purelic.commons.utils.ServerUtils;
import net.purelic.commons.utils.TaskUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.*;
import java.util.stream.Collectors;

public class VotingManager {

    private static final String NBT_TAG = "voting_item";

    private final VotingSettings settings;
    private final Map<String, VotingOption> options;
    private final List<VotingOption> selected;
    private final VotingKit kit;
    private CustomMap lastPlayedMap;
    private CustomGameMode lastPlayedGameMode;
    private VotingCountdown countdown;
    private boolean enabled;
    private boolean canceled;

    public VotingManager(Playlist playlist) {
        this.settings = playlist.getVotingSettings();
        this.options = new HashMap<>();
        this.selected = new ArrayList<>();
        this.kit = new VotingKit(this, NBT_TAG);
        this.lastPlayedMap = null;
        this.lastPlayedGameMode = null;
        this.countdown = null;
        this.enabled = true;
        this.canceled = false;
        this.setVotingOptions(playlist);
    }

    public void setVotingOptions(Playlist playlist) {
        for (Map.Entry<CustomMap, List<CustomGameMode>> entry : playlist.getPool().entrySet()) {
            CustomMap map = entry.getKey();

            for (CustomGameMode gameMode : entry.getValue()) {
                VotingOption option = new VotingOption(map, gameMode);
                this.options.put(option.getId(), option);
            }
        }
    }

    public VotingSettings getSettings() {
        return this.settings;
    }

    public void setCanceled(boolean canceled) {
        this.canceled = canceled;

        if (canceled && TaskUtils.isRunning(this.countdown)) {
            this.countdown.cancel();
        }
    }

    public void toggleEnabled() {
        this.enabled = !this.enabled;
    }

    public boolean isEnabled() {
        return this.enabled;
    }

    public VotingCountdown getCountdown() {
        return this.countdown;
    }

    public List<VotingOption> getSelected() {
        return this.selected;
    }

    public VotingKit getKit() {
        return this.kit;
    }

    public void setLastPlayed(CustomMap map, CustomGameMode gameMode) {
        this.lastPlayedMap = map;
        this.lastPlayedGameMode = gameMode;
    }

    public boolean shouldStartVoting() {
        return MatchState.isState(MatchState.WAITING)
            && Bukkit.getOnlinePlayers().size() >= this.settings.getMinPlayers()
            && !this.canceled
            && this.enabled
            && !ServerUtils.isRanked();
    }

    public void startVoting(int seconds, boolean forced) {
        this.clearVotes();

        // shuffle the voting options
        List<VotingOption> options = new ArrayList<>(this.options.values());
        Collections.shuffle(options);

        // filter out the last played maps (if applicable)
        if (this.lastPlayedMap != null && !this.settings.allowRepeatMaps()) {
            options = options.stream()
                .filter(option -> option.getMap() != this.lastPlayedMap)
                .collect(Collectors.toList());
        }

        // filter out the last played game modes (if applicable)
        if (this.lastPlayedGameMode != null && !this.settings.allowRepeatGameModes()) {
            options = options.stream()
                .filter(option -> option.getGameMode() != this.lastPlayedGameMode)
                .collect(Collectors.toList());
        }

        this.selected.clear();
        List<CustomMap> selectedMaps = new ArrayList<>();
        List<CustomGameMode> selectedGameModes = new ArrayList<>();

        for (VotingOption option : options) {
            // break once we have enough selected voting options
            if (this.selected.size() == this.settings.getVotingOptions()) {
                break;
            }

            CustomMap map = option.getMap();
            CustomGameMode gameMode = option.getGameMode();

            // skip already selected maps (if applicable)
            if (!this.settings.allowDuplicateMaps() && selectedMaps.contains(map)) {
                continue;
            }

            // skip already selected game modes (if applicable)
            if (!this.settings.allowDuplicateGameModes() && selectedGameModes.contains(gameMode)) {
                continue;
            }

            // add the option to our list of selected options
            this.selected.add(option);
            selectedMaps.add(map);
            selectedGameModes.add(gameMode);
        }

        // give players the selected voting items
        for (Player player : Bukkit.getOnlinePlayers()) {
            this.kit.apply(player);
        }

        // start the voting countdown
        seconds = forced ? seconds : this.settings.getVotingDuration();
        this.countdown = new VotingCountdown(this, seconds, forced);
        TaskUtils.runTimerAsync(this.countdown);

        // update the scoreboard sidebar
        ScoreboardManager.resetScores(0);
        ScoreboardManager.setDisplayName("Voting");
        this.updateSidebar();
    }

    public VotingOption getMostVotedOption() {
        // we shuffle the options one more time to randomize which one gets selected if there's a tie
        List<VotingOption> shuffled = new ArrayList<>(this.selected);
        Collections.shuffle(shuffled);
        return shuffled.stream()
            .max(Comparator.comparing(VotingOption::getVotes)).orElse(null);
    }

    public void clearVotes() {
        for (VotingOption option : this.options.values()) option.clearVotes();
    }

    public void clearVotingItems() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            this.kit.remove(player);
        }
    }

    private void updateSidebar() {
        int row = 0;

        for (VotingOption option : this.selected) {
            String votes = option.getVotes() + "  ";
            String display = option.getMap().getColoredName() + ChatColor.GRAY + " (" + option.getGameMode().getAlias() + ")";

            // if random option is enabled and it's the last voting option
            if (this.settings.hasRandomOption() && row == this.selected.size() - 1) {
                display = ChatColor.YELLOW + "Random";
            }

            ScoreboardManager.setScore(row++, votes + display);
        }
    }

    public void vote(Player player, String optionId) {
        VotingOption option = this.options.get(optionId);

        // if multi select is disabled, reset all their votes
        if (!this.settings.allowMultiSelect()) {
            for (ItemStack item : player.getInventory().getContents()) {
                ItemCrafter itemCrafter = new ItemCrafter(item);

                if (itemCrafter.hasTag(NBT_TAG)) {
                    this.options.get(itemCrafter.getTag(NBT_TAG)).unvote(player);
                    item.setType(this.settings.getVoteItem());
                }
            }
        }

        if (option.voted(player)) { // remove vote
            option.unvote(player);
            player.getItemInHand().setType(this.settings.getVoteItem());
            SoundUtils.SFX.UNVOTE.play(player);
        } else { // add vote
            option.vote(player);
            player.getItemInHand().setType(this.settings.getVotedItem());
            SoundUtils.SFX.VOTE.play(player);
        }

        player.updateInventory();
        this.updateSidebar();
    }

    public void clearVotes(Player player) {
        for (VotingOption option : this.options.values()) option.unvote(player);
        this.updateSidebar();
    }

}
