package net.purelic.cgm.core.managers;

import net.purelic.cgm.core.gamemodes.CustomGameMode;
import net.purelic.cgm.core.maps.CustomMap;

import java.util.*;

public class MapManager {

    private static final Map<String, CustomMap> MAPS = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
    private static final Map<CustomMap, Set<CustomGameMode>> REPO = new HashMap<>();
    private static final Map<CustomMap, Set<CustomGameMode>> PLAYLIST = new HashMap<>();

    public static Map<String, CustomMap> getMaps() {
        return MAPS;
    }

    public static Map<CustomMap, Set<CustomGameMode>> getRepo() {
        return REPO;
    }

    public static Map<CustomMap, Set<CustomGameMode>> getPlaylist() {
        return PLAYLIST;
    }

    public static Set<CustomGameMode> getGameModes(CustomMap map) {
        return REPO.get(map);
    }

    public static void addGameMode(CustomGameMode mode) {
        MAPS.values().forEach(map -> addMap(map, mode, false));
    }

    public static void addMap(CustomMap map) {
        GameModeManager.getGameModes().forEach(mode -> addMap(map, mode, false));
    }

    public static void addMap(CustomMap map, CustomGameMode gameMode, boolean playlist) {
        MAPS.putIfAbsent(map.getName(), map);

        if (playlist) PLAYLIST.putIfAbsent(map, new HashSet<>());
        else REPO.putIfAbsent(map, new HashSet<>());

        if (!map.supportsGameMode(gameMode)) return;

        if (playlist) PLAYLIST.get(map).add(gameMode);
        else REPO.get(map).add(gameMode);
    }

    public static CustomMap getMapByExactName(String name) {
        return REPO.keySet().stream()
                .filter(map -> map.getName().equalsIgnoreCase(name))
                .findFirst().orElse(null);
    }

    public static CustomMap getMapByName(String name) {
        CustomMap map = null;

        for (CustomMap customMap : REPO.keySet()) {
            if (customMap.getName().equalsIgnoreCase(name)) {
                return customMap;
            } else if (customMap.getName().toLowerCase().startsWith(name.toLowerCase())) {
                map = customMap;
            }
        }

        return map;
    }

}
