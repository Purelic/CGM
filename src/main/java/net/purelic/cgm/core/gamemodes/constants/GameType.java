package net.purelic.cgm.core.gamemodes.constants;

import net.purelic.cgm.core.maps.hill.constants.HillType;

import java.util.Arrays;
import java.util.HashSet;

public enum GameType {

    DEATHMATCH("Deathmatch", "Kill the most players to win!", null, true),
    HEAD_HUNTER("Head Hunter", "Kill other players and collect their head for points!", HillType.HH_GOAL, true),
    KING_OF_THE_HILL("King of the Hill", "Capture hills for points!", HillType.KOTH_HILL, true),
    BED_WARS("Bed Wars", "Destroy enemy beds and then eliminate them!", null, false),
    CAPTURE_THE_FLAG("Capture the Flag", "Capture flags and bring them back to your base!", HillType.CTF_GOAL, false),
    SURVIVAL_GAMES("Survival Games", "Find gear and eliminate the other players!", null, false),
    INFECTION("Infection", "Infect the survivors!", null, true),
    UHC("Ultra Hardcore", "Survive till the end!", null, false),
    // SPEED_RUN("Speed Run", "Beat the game as fast as possible!"),
    ;

    private final String name;
    private final String description;
    private final HillType hillType;
    private final boolean killScoring;

    GameType(String name, String description, HillType hillType, boolean killScoring) {
        this.name = name;
        this.description = description;
        this.hillType = hillType;
        this.killScoring = killScoring;
    }

    public String getName() {
        return this.name;
    }

    public String getDescription() {
        return this.description;
    }

    public HillType getHillType() {
        return this.hillType;
    }

    public boolean hasKillScoring() {
        return this.killScoring;
    }

    public boolean isType(GameType... gameTypes) {
        return (new HashSet<>(Arrays.asList(gameTypes))).contains(this);
    }

}
