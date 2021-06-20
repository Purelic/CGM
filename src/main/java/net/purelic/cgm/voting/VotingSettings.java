package net.purelic.cgm.voting;

import net.purelic.cgm.utils.YamlObject;
import org.bukkit.Material;

import java.util.Map;

public class VotingSettings extends YamlObject<VotingModifiers> {

    private final int minPlayers;
    private final Material voteItem;
    private final Material votedItem;
    private final int votingOptions;
    private final boolean repeatMaps;
    private final boolean repeatGameModes;
    private final boolean duplicateMaps;
    private final boolean duplicateGameModes;
    private final int votingDuration;
    private final int cycleDuration;
    private final boolean multiSelect;
    private final boolean randomOption;
    private final boolean dynamic;
    private final boolean offlineVotes;

    public VotingSettings(Map<String, Object> yaml) {
        super(yaml);
        this.minPlayers = this.get(VotingModifiers.MIN_PLAYERS, 2);
        this.voteItem = this.get(VotingModifiers.VOTE_ITEM, Material.SLIME_BALL);
        this.votedItem = this.get(VotingModifiers.VOTED_ITEM, Material.MAGMA_CREAM);
        this.votingOptions = this.get(VotingModifiers.VOTING_OPTIONS, 5);
        this.repeatMaps = this.get(VotingModifiers.REPEAT_MAPS, false);
        this.repeatGameModes = this.get(VotingModifiers.REPEAT_GAME_MODES, true);
        this.duplicateMaps = this.get(VotingModifiers.DUPLICATE_MAPS, false);
        this.duplicateGameModes = this.get(VotingModifiers.DUPLICATE_GAME_MODES, true);
        this.votingDuration = this.get(VotingModifiers.VOTING_DURATION, 20);
        this.cycleDuration = this.get(VotingModifiers.CYCLE_DURATION, 10);
        this.multiSelect = this.get(VotingModifiers.MULTI_SELECT, true);
        this.randomOption = this.get(VotingModifiers.RANDOM_OPTION, true);
        this.dynamic = this.get(VotingModifiers.DYNAMIC, true);
        this.offlineVotes = this.get(VotingModifiers.OFFLINE_VOTES, false);
    }

    public int getMinPlayers() {
        return this.minPlayers;
    }

    public Material getVoteItem() {
        return this.voteItem;
    }

    public Material getVotedItem() {
        return this.votedItem;
    }

    public int getVotingOptions() {
        return this.votingOptions;
    }

    public boolean allowRepeatMaps() {
        return this.repeatMaps;
    }

    public boolean allowRepeatGameModes() {
        return this.repeatGameModes;
    }

    public boolean allowDuplicateMaps() {
        return this.duplicateMaps;
    }

    public boolean allowDuplicateGameModes() {
        return this.duplicateGameModes;
    }

    public int getVotingDuration() {
        return this.votingDuration;
    }

    public int getCycleDuration() {
        return this.cycleDuration;
    }

    public boolean allowMultiSelect() {
        return this.multiSelect;
    }

    public boolean hasRandomOption() {
        return this.randomOption;
    }

    public boolean isDynamic() {
        return this.dynamic;
    }

    public boolean allowOfflineVotes() {
        return this.offlineVotes;
    }

}
