package net.purelic.cgm.voting;

public enum VotingModifiers {

    MIN_PLAYERS, // min players to auto-start the voting countdown
    VOTE_ITEM, // material for the voting item
    VOTED_ITEM, // material for the voting item when voted
    VOTING_OPTIONS, // max number of voting options
    REPEAT_MAPS, // allow maps to repeat in the voting options after just being played
    REPEAT_GAME_MODES, // allow game modes to repeat in the voting options after just being played
    DUPLICATE_MAPS, // allow maps to show up on more than one voting option
    DUPLICATE_GAME_MODES, // allow game modes to show up on more than one voting option
    VOTING_DURATION, // duration in seconds for the voting countdown
    CYCLE_DURATION, // duration in seconds for the cycle countdown after the voting countdown ends
    MULTI_SELECT, // allow players to vote for multiple options
    RANDOM_OPTION, // allow voting for a random option
    DYNAMIC, // dynamically adjust the voting options to fit the number of online players
    OFFLINE_VOTES, // include the votes of players who disconnected during the voting countdown

}
