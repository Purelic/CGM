package net.purelic.cgm.match;

import net.md_5.bungee.api.ChatColor;
import net.purelic.cgm.core.gamemodes.CustomGameMode;
import net.purelic.cgm.core.gamemodes.EnumSetting;
import net.purelic.cgm.core.gamemodes.NumberSetting;
import net.purelic.cgm.core.gamemodes.constants.TeamType;
import net.purelic.cgm.core.maps.CustomMap;
import net.purelic.cgm.voting.VotingOption;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Match {

    private final String id;
    private final CustomMap map;
    private final CustomGameMode gameMode;
    private final List<MatchRound> rounds;
    private int currentRound;
    private final boolean elimination;
    private final TeamType teamType;
    private final int scoreLimit;

    public Match(VotingOption votingOption) {
        this(votingOption.getMap(), votingOption.getGameMode());
    }

    public Match(CustomMap map, CustomGameMode gameMode) {
        this.id = UUID.randomUUID().toString();
        this.map = map;
        this.gameMode = gameMode;
        this.rounds = new ArrayList<>();
        this.currentRound = 1;
        this.elimination =
            this.gameMode.getNumberSetting(NumberSetting.SCORE_LIMIT) == 0
            && this.gameMode.getNumberSetting(NumberSetting.LIVES_PER_ROUND) > 0;
        this.teamType = TeamType.valueOf(this.gameMode.getEnumSetting(EnumSetting.TEAM_TYPE));
        this.scoreLimit = this.gameMode.getNumberSetting(NumberSetting.SCORE_LIMIT);
        this.setRounds();
    }

    private void setRounds() {
        int numRounds = this.gameMode.getNumberSetting(NumberSetting.ROUNDS);

        for (int i = 0; i < numRounds; i++) {
            this.rounds.add(new MatchRound());
        }
    }

    public String getId() {
        return this.id;
    }

    public CustomMap getMap() {
        return this.map;
    }

    public CustomGameMode getGameMode() {
        return this.gameMode;
    }

    public String getMatchTitle() {
        return this.gameMode.getColoredName() + " on " + this.map.getColoredName();
    }

    public void load() {
        this.gameMode.loadSettings();
        this.map.loadObjectives();
    }

    public boolean isRoundBased() {
        return this.rounds.size() > 1;
    }

    public List<MatchRound> getRounds() {
        return this.rounds;
    }

    public int getCurrentRound() {
        return this.currentRound;
    }

    public void incrementRound() {
        this.currentRound++;
    }

    public String getRoundsString(boolean truncate) {
        StringBuilder s = new StringBuilder();

        for (MatchRound round : this.rounds) {
            s.append(" ").append(round.getSymbol());
        }

        return (truncate ? "" : "Rounds: ") + s.toString().trim();
    }

    public boolean isElimination() {
        return this.elimination;
    }

    public String getObjective() {
        return this.isElimination() ? ChatColor.RED + "Eliminate" + ChatColor.RESET + " " + this.getEliminationType()
            : ChatColor.AQUA + "" + this.scoreLimit + ChatColor.RESET + " Point" + (this.scoreLimit == 1 ? "" : "s") + " to Win";
    }

    private String getEliminationType() {
        return this.teamType == TeamType.SOLO ? "Other Players"
            : this.teamType == TeamType.TEAMS ? "Enemy Team" : "Enemy Teams";
    }

}
