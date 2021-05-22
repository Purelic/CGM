package net.purelic.cgm.core.runnables;

import net.purelic.cgm.core.gamemodes.CustomGameMode;
import net.purelic.cgm.core.managers.GameModeManager;
import net.purelic.cgm.core.managers.MapManager;
import net.purelic.cgm.core.maps.CustomMap;
import net.purelic.cgm.core.maps.MapYaml;
import net.purelic.commons.utils.DatabaseUtils;
import net.purelic.commons.utils.MapUtils;
import net.purelic.commons.utils.ServerUtils;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import shaded.com.google.cloud.firestore.QueryDocumentSnapshot;

import java.util.List;
import java.util.UUID;

public class PersonalContentDownloader extends BukkitRunnable {

    private final UUID uuid;

    public PersonalContentDownloader() {
        this(ServerUtils.isPrivate() ? ServerUtils.getId() : null);
    }

    public PersonalContentDownloader(Player player) {
        this(player.getUniqueId());
    }

    public PersonalContentDownloader(String uuid) {
        this(uuid == null || uuid.isEmpty() ? null : UUID.fromString(uuid));
    }

    public PersonalContentDownloader(UUID uuid) {
        this.uuid = uuid;
    }

    @Override
    public void run() {
        if (this.uuid == null) return;

        // Download the player's published maps
        List<String> downloaded = MapUtils.downloadPublishedMaps(this.uuid, MapManager.getMaps().keySet());

        for (String mapName : downloaded) {
            MapYaml yaml = new MapYaml(MapUtils.getMapYaml(mapName));
            CustomMap map = new CustomMap(mapName, yaml);
            MapManager.addMap(map);
        }

        // Download the player's game modes
        List<QueryDocumentSnapshot> gms = DatabaseUtils.getGameModes(this.uuid);

        for (QueryDocumentSnapshot documentSnapshot : gms) {
            CustomGameMode gameMode = new CustomGameMode(documentSnapshot);
            CustomGameMode gameModeByName = GameModeManager.getGameModeByExactName(gameMode.getName());
            CustomGameMode gameModeByAlias = GameModeManager.getGameModeByAlias(gameMode.getAlias());

            // Skip private game modes or game modes with the same name or alias of one already downloaded
            if (!gameMode.isPublic() || gameModeByName != null || gameModeByAlias != null) continue;

            GameModeManager.loadGameMode(gameMode);
            MapManager.addGameMode(gameMode);
        }
    }

}
