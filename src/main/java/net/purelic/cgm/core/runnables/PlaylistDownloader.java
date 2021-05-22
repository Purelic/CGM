package net.purelic.cgm.core.runnables;

import net.purelic.cgm.CGM;
import net.purelic.cgm.core.gamemodes.CustomGameMode;
import net.purelic.cgm.core.managers.GameModeManager;
import net.purelic.cgm.core.managers.MapManager;
import net.purelic.cgm.core.managers.VoteManager;
import net.purelic.cgm.core.maps.CustomMap;
import net.purelic.cgm.core.maps.MapYaml;
import net.purelic.commons.runnables.MapLoader;
import net.purelic.commons.utils.DatabaseUtils;
import net.purelic.commons.utils.MapUtils;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class PlaylistDownloader extends BukkitRunnable {

    private static final String PURELIC_UUID = "57014d5f-1d26-4986-832b-a0e7a4e41088";
    private final String playlist;

    public PlaylistDownloader(String playlist) {
        if (playlist == null) playlist = CGM.getPlugin().getConfig().getString("fallback_playlist");
        this.playlist = playlist;
    }

    @Override
    public void run() {
        // Download all public maps
        String[] maps = MapUtils.downloadPublicMaps();

        // Download all public game modes
        GameModeManager.loadGameModes(DatabaseUtils.getGameModes(PURELIC_UUID));

        // Download the playlist
        Map<String, Object> playlist = MapUtils.downloadPlaylist(this.playlist);
        Map<String, Set<String>> playlistMaps = this.getMaps(playlist);

        for (String map : maps) {
            // Ignore reserved maps (e.g. lobby)
            if (MapUtils.isReservedMapName(map)) continue;

            Map<String, Object> mapData = MapUtils.getMapYaml(map);

            // Failed to download map
            if (mapData.isEmpty()) continue;

            // Create the custom map
            MapYaml mapYaml = new MapYaml(mapData);
            CustomMap customMap = new CustomMap(map, mapYaml);

            Set<String> modes = playlistMaps.get(map);

            if (modes == null) {
                MapManager.addMap(customMap);
            } else {
                // Add the map and game mode combinations to the playlist
                for (String mode : modes) {
                    CustomGameMode gameMode = GameModeManager.getGameModeByExactName(mode);
                    if (gameMode == null) continue;
                    MapManager.addMap(customMap, gameMode, true);
                }
            }
        }

        // Load all map/gamemode combinations
        MapManager.getMaps().values().forEach(MapManager::addMap);

        // Updates min player limit for to start matches
        VoteManager.setMinPlayers((Integer) playlist.getOrDefault("min_players", 2));

        // Set the server to a ready state
        System.out.println(this.playlist + " Playlist Downloaded!");
        CGM.setReady();

        new PersonalContentDownloader().runTaskAsynchronously(CGM.getPlugin());
        new MapLoader("Lobby").runTaskAsynchronously(CGM.getPlugin());
    }

    private Map<String, Set<String>> getMaps(Map<String, Object> playlist) {
        List<Map<String, Object>> playlistMaps = (List<Map<String, Object>>) playlist.get("maps");
        Map<String, Set<String>> maps = new HashMap<>();

        for (Map<String, Object> map : playlistMaps) {
            String name = (String) map.get("name");
            Set<String> gameModes = new HashSet<>((List<String>) map.get("modes"));
            maps.put(name, gameModes);
        }

        return maps;
    }

    private Set<String> getGameModes(Map<String, Set<String>> maps) {
        Set<String> modes = new HashSet<>();
        maps.values().forEach(modes::addAll);
        return modes;
    }

}
