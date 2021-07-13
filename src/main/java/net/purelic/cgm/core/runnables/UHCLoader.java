package net.purelic.cgm.core.runnables;

import net.purelic.cgm.core.maps.CustomMap;
import net.purelic.cgm.core.maps.MapYaml;
import net.purelic.cgm.utils.WorldGenCaves;
import net.purelic.commons.Commons;
import net.purelic.commons.runnables.MapLoader;
import net.purelic.commons.utils.CommandUtils;
import net.purelic.commons.utils.MapUtils;
import net.purelic.commons.utils.TaskUtils;
import org.bukkit.Difficulty;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.WorldType;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.UUID;

public class UHCLoader extends BukkitRunnable {

    private final CustomMap map;
    private final World.Environment environment;
    private final WorldType type;
    private final boolean pregen;

    public UHCLoader(CustomMap map) {
        this(map, null);
    }

    public UHCLoader(CustomMap map, WorldType worldType) {
        this.map = map;
        this.environment = World.Environment.NORMAL;
        this.type = worldType;
        this.pregen = worldType != null;
    }

    @Override
    public void run() {
        if (this.pregen) {
            WorldCreator wc = new WorldCreator("UHC");
            wc.generateStructures(true);
            wc.environment(this.environment);
            wc.type(this.type);
            wc.generateStructures(true);

            World world = wc.createWorld();
            world.setDifficulty(Difficulty.PEACEFUL);
            world.setAutoSave(false);
            WorldGenCaves.loadForWorld(world, 3); // load 3x the normal number of caves

            // copy default uhc map yaml file into new world dir
            try {
                Files.copy(Paths.get(Commons.getRoot() + "Maps/UHC/map.yml"), Paths.get(Commons.getRoot() + "UHC/map.yml"));
            } catch (IOException e) {
                e.printStackTrace();
            }

            this.map.setNextWorld(world);
            TaskUtils.run(new ChunkLoader(world));
        } else {
            String downloaded = MapUtils.downloadUHCMap();

            if (downloaded == null) {
                CommandUtils.broadcastErrorMessage("There was an error downloading a UHC map! Please try again.");
                return;
            }

            MapYaml yaml = new MapYaml(MapUtils.getMapYaml(downloaded));
            CustomMap map = new CustomMap(downloaded, yaml);
            TaskUtils.runAsync(new MapLoader(map.getName(), UUID.randomUUID().toString()));
        }
    }

}
