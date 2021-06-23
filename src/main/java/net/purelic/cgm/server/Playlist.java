package net.purelic.cgm.server;

import net.purelic.cgm.CGM;
import net.purelic.cgm.core.gamemodes.CustomGameMode;
import net.purelic.cgm.core.maps.CustomMap;
import net.purelic.cgm.core.maps.MapYaml;
import net.purelic.cgm.voting.VotingSettings;
import net.purelic.commons.Commons;
import net.purelic.commons.utils.*;
import shaded.com.google.cloud.firestore.QueryDocumentSnapshot;

import javax.annotation.Nullable;
import java.util.*;

@SuppressWarnings("unchecked")
public class Playlist {

    private final Map<String, Object> yaml;
    private final VotingSettings votingSettings;
    private final Map<String, CustomMap> maps;
    private final Map<String, CustomGameMode> gameModes;
    private final Map<String, String> gameModesByAlias;
    private final Map<CustomMap, List<CustomGameMode>> repo;
    private final Map<CustomMap, List<CustomGameMode>> pool;

    public Playlist() {
        this(ServerUtils.getPlaylist() == null ?
            CGM.get().getConfig().getString("fallback_playlist") : ServerUtils.getPlaylist());
    }

    private Playlist(String name) {
        this(MapUtils.downloadPlaylist(name));
    }

    private Playlist(Map<String, Object> yaml) {
        this.yaml = yaml;
        this.votingSettings = new VotingSettings((Map<String, Object>) yaml.getOrDefault("settings", new HashMap<>()));
        this.maps = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        this.gameModes = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        this.gameModesByAlias = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        this.repo = new HashMap<>();
        this.pool = new HashMap<>();
        this.download();
    }

    public VotingSettings getVotingSettings() {
        return this.votingSettings;
    }

    public Map<String, CustomMap> getMaps() {
        return this.maps;
    }

    public Map<CustomMap, List<CustomGameMode>> getRepo() {
        return this.repo;
    }

    public Map<CustomMap, List<CustomGameMode>> getPool() {
        return this.pool;
    }

    public List<CustomGameMode> getGameModes(CustomMap map) {
        return this.repo.get(map);
    }

    public Collection<CustomGameMode> getGameModes() {
        return this.gameModes.values();
    }

    public CustomGameMode getGameModeByName(String exactName) {
        return this.gameModes.get(exactName.toLowerCase());
    }

    public CustomGameMode getGameModeByAlias(String alias) {
        String gameModeName = this.gameModesByAlias.get(alias.toLowerCase());
        return gameModeName == null ? null : getGameModeByName(gameModeName);
    }

    @Nullable
    public CustomGameMode getGameMode(String search) {
        CustomGameMode exact = null;
        CustomGameMode guess = null;

        for (CustomGameMode customGameMode : this.gameModes.values()) {
            if (customGameMode.getName().equalsIgnoreCase(search)) {
                exact = customGameMode;
                break;
            } else if (customGameMode.getAlias().equalsIgnoreCase(search)) {
                exact = customGameMode;
                break;
            } else if (customGameMode.getName().toLowerCase().startsWith(search.toLowerCase())) {
                guess = customGameMode;
            }
        }

        return exact != null ? exact : guess;
    }

    public CustomMap getMapByName(String exactName) {
        return this.maps.get(exactName.toLowerCase());
    }

    @Nullable
    public CustomMap getMap(String search) {
        CustomMap map = null;

        for (CustomMap customMap : this.repo.keySet()) {
            if (customMap.getName().equalsIgnoreCase(search)) {
                return customMap;
            } else if (customMap.getName().toLowerCase().startsWith(search.toLowerCase())) {
                map = customMap;
            }
        }

        return map;
    }

    public void download() {
        this.downloadGameModes(Fetcher.PURELIC_UUID);
        if (Commons.hasOwner()) this.downloadGameModes(Commons.getOwnerId());

        this.downloadMaps();
        if (Commons.hasOwner()) this.downloadPlayerMaps(Commons.getOwnerId());
    }

    private void downloadGameModes(UUID uuid) {
        DatabaseUtils.getGameModes(uuid).forEach(this::addGameMode);
    }

    public void loadGameMode(CustomGameMode gameMode) {
        this.addGameMode(gameMode);
        this.maps.values().forEach(map -> this.addMap(map, gameMode, false));
    }

    private void addGameMode(QueryDocumentSnapshot document) {
        this.addGameMode(new CustomGameMode(document));
    }

    private void addGameMode(CustomGameMode gameMode) {
        String name = gameMode.getName().toLowerCase();
        String alias = gameMode.getAlias().toLowerCase();

        if (!gameMode.isPublic()
            || this.gameModes.containsKey(name)
            || this.gameModesByAlias.containsKey(alias)) return;

        this.gameModes.put(name, gameMode);
        this.gameModesByAlias.put(alias, name);
    }

    private void downloadMaps() {
        Map<String, List<String>> rawPlaylist = new HashMap<>();
        List<Map<String, Object>> rawMaps = (List<Map<String, Object>>) this.yaml.get("maps");

        for (Map<String, Object> map : rawMaps) {
            String mapName = (String) map.get("name");
            List<String> gameModes = (List<String>) map.get("modes");
            rawPlaylist.put(mapName, gameModes);
        }

        String[] publicMaps = MapUtils.downloadPublicMaps();

        for (String mapName : publicMaps) {
            // ignore reserved maps (e.g. lobbies)
            if (MapUtils.isReservedMapName(mapName)) continue;

            Map<String, Object> mapData = MapUtils.getMapYaml(mapName);

            // failed to download map
            if (mapData.isEmpty()) continue;

            // create the custom map
            MapYaml mapYaml = new MapYaml(mapData);
            CustomMap map = new CustomMap(mapName, mapYaml);

            // get the playlist game modes for this map (if there are any)
            List<String> gameModes = rawPlaylist.getOrDefault(mapName, new ArrayList<>());

            for (CustomGameMode gameMode : this.gameModes.values()) {
                this.addMap(map, gameMode, gameModes.contains(gameMode.getName()));
            }
        }
    }

    public void loadMap(CustomMap map) {
        for (CustomGameMode gameMode : this.gameModes.values()) {
            this.addMap(map, gameMode, false);
        }
    }

    private void addMap(CustomMap map, CustomGameMode gameMode, boolean playlist) {
        this.maps.putIfAbsent(map.getName().toLowerCase(), map);
        this.repo.putIfAbsent(map, new ArrayList<>());
        if (playlist) this.pool.putIfAbsent(map, new ArrayList<>());

        if (!map.supportsGameMode(gameMode)) return;

        this.repo.get(map).add(gameMode);
        if (playlist) this.pool.get(map).add(gameMode);
    }

    private void downloadPlayerMaps(UUID uuid) {
        // TODO do filtering/skipping of maps with the same name already downloaded here instead of in Map Utils
        List<String> maps = MapUtils.downloadPublishedMaps(uuid, this.maps.keySet());

        for (String mapName : maps) {
            MapYaml yaml = new MapYaml(MapUtils.getMapYaml(mapName));
            CustomMap map = new CustomMap(mapName, yaml);
            this.loadMap(map);
        }
    }

}
