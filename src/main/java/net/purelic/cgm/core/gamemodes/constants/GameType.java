package net.purelic.cgm.core.gamemodes.constants;

import net.purelic.cgm.core.maps.hill.constants.HillType;

import java.util.Arrays;
import java.util.HashSet;

public enum GameType {

    DEATHMATCH("Deathmatch", "Kill the most players to win!", HillType.SCOREBOX),
    HEAD_HUNTER("Head Hunter", "Kill other players and collect their head for points!", HillType.HH_GOAL),
    KING_OF_THE_HILL("King of the Hill", "Capture hills for points!", HillType.KOTH_HILL),
    BED_WARS("Bed Wars", "Destroy enemy beds and then eliminate them!", null),
    CAPTURE_THE_FLAG("Capture the Flag", "Capture flags and bring them back to your base!", HillType.CTF_GOAL),
    SURVIVAL_GAMES("Survival Games", "Find gear and eliminate the other players!", null),
    INFECTION("Infection", "Infect the survivors!", null),
    UHC("Ultra Hardcore", "Survive till the end!", null),
    DESTROY("Destroy", "Destroy the enemy monument!", null),
    ;

    private final String name;
    private final String description;
    private final HillType hillType;

    GameType(String name, String description, HillType hillType) {
        this.name = name;
        this.description = description;
        this.hillType = hillType;
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

    public boolean isType(GameType... gameTypes) {
        return (new HashSet<>(Arrays.asList(gameTypes))).contains(this);
    }

}
