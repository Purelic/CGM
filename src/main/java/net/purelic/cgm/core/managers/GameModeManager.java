package net.purelic.cgm.core.managers;

import net.purelic.cgm.core.gamemodes.CustomGameMode;
import shaded.com.google.cloud.firestore.QueryDocumentSnapshot;

import java.util.*;

public class GameModeManager {

    private static final Map<String, CustomGameMode> GAME_MODES = new LinkedHashMap<>();
    private static final Map<String, String> GAME_MODE_ALIASES = new HashMap<>();

    public static void loadGameModes(List<QueryDocumentSnapshot> documents) {
        documents.forEach(GameModeManager::loadGameMode);
    }

    private static void loadGameMode(QueryDocumentSnapshot document) {
        loadGameMode(new CustomGameMode(document));
    }

    public static void loadGameMode(CustomGameMode gameMode) {
        String name = gameMode.getName();
        String alias = gameMode.getAlias();

        GAME_MODES.put(name, gameMode);
        GAME_MODE_ALIASES.put(alias, name);
    }

    public static Collection<CustomGameMode> getGameModes() {
        return GAME_MODES.values();
    }

    public static CustomGameMode getGameModeByExactName(String name) {
        return GAME_MODES.get(name);
    }

    public static CustomGameMode getGameModeByAlias(String alias) {
        return getGameModeByExactName(GAME_MODE_ALIASES.get(alias));
    }

    public static CustomGameMode getGameModeByNameOrAlias(String value) {
        CustomGameMode exact = null;
        CustomGameMode guess = null;

        for (CustomGameMode customGameMode : GAME_MODES.values()) {
            if (customGameMode.getName().equalsIgnoreCase(value)) {
                exact = customGameMode;
                break;
            } else if (customGameMode.getAlias().equalsIgnoreCase(value)) {
                exact = customGameMode;
                break;
            } else if (customGameMode.getName().toLowerCase().startsWith(value.toLowerCase())) {
                guess = customGameMode;
            }
        }

        return exact != null ? exact : guess;
    }

}
