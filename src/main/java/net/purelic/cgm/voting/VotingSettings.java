package net.purelic.cgm.voting;

import org.bukkit.Material;

import java.util.Map;

public class VotingSettings {

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
        this.minPlayers = (int) yaml.getOrDefault(VotingModifiers.MIN_PLAYERS.name().toLowerCase(), 2);
        this.voteItem = Material.valueOf(((String) yaml.getOrDefault(VotingModifiers.VOTE_ITEM.name().toLowerCase(), Material.SLIME_BALL.name())).toUpperCase());
        this.votedItem = Material.valueOf(((String) yaml.getOrDefault(VotingModifiers.VOTED_ITEM.name().toLowerCase(), Material.MAGMA_CREAM.name())).toUpperCase());
        this.votingOptions = (int) yaml.getOrDefault(VotingModifiers.VOTING_OPTIONS.name().toLowerCase(), 5);
        this.repeatMaps = (boolean) yaml.getOrDefault(VotingModifiers.REPEAT_MAPS.name().toLowerCase(), false);
        this.repeatGameModes = (boolean) yaml.getOrDefault(VotingModifiers.REPEAT_GAME_MODES.name().toLowerCase(), true);
        this.duplicateMaps = (boolean) yaml.getOrDefault(VotingModifiers.DUPLICATE_MAPS.name().toLowerCase(), false);
        this.duplicateGameModes = (boolean) yaml.getOrDefault(VotingModifiers.DUPLICATE_GAME_MODES.name().toLowerCase(), true);
        this.votingDuration = (int) yaml.getOrDefault(VotingModifiers.VOTING_DURATION.name().toLowerCase(), 20);
        this.cycleDuration = (int) yaml.getOrDefault(VotingModifiers.CYCLE_DURATION.name().toLowerCase(), 10);
        this.multiSelect = (boolean) yaml.getOrDefault(VotingModifiers.MULTI_SELECT.name().toLowerCase(), true);
        this.randomOption = (boolean) yaml.getOrDefault(VotingModifiers.RANDOM_OPTION.name().toLowerCase(), true);
        this.dynamic = (boolean) yaml.getOrDefault(VotingModifiers.DYNAMIC.name().toLowerCase(), true);
        this.offlineVotes = (boolean) yaml.getOrDefault(VotingModifiers.OFFLINE_VOTES.name().toLowerCase(), false);
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
