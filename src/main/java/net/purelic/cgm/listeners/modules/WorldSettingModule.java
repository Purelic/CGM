package net.purelic.cgm.listeners.modules;

import net.minecraft.server.v1_8_R3.BiomeBase;
import net.purelic.cgm.CGM;
import net.purelic.cgm.core.gamemodes.EnumSetting;
import net.purelic.cgm.core.gamemodes.ToggleSetting;
import net.purelic.cgm.core.gamemodes.constants.GameType;
import net.purelic.cgm.events.match.MatchCycleEvent;
import net.purelic.cgm.events.match.MatchEndEvent;
import net.purelic.cgm.events.match.MatchStartEvent;
import net.purelic.cgm.uhc.UHCScenario;
import net.purelic.cgm.uhc.scenarios.DragonRushScenario;
import net.purelic.cgm.uhc.scenarios.NetherScenario;
import net.purelic.commons.Commons;
import net.purelic.commons.utils.TaskUtils;
import org.apache.commons.io.FileUtils;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.world.WorldInitEvent;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;

public class WorldSettingModule implements Listener {

    public WorldSettingModule() {
        if (CGM.getPlaylist().isUHC()) {
            this.replaceOceanBiomes();
            this.generateDimensions(true);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onMatchStart(MatchStartEvent event) {
        World world = event.getMap().getWorld();
        world.setGameRuleValue("doMobSpawning", "" + ToggleSetting.MOB_SPAWNING.isEnabled());
        world.setGameRuleValue("mobGriefing", "" + (ToggleSetting.MOB_SPAWNING.isEnabled() || EnumSetting.GAME_TYPE.is(GameType.BED_WARS))); // this will also disable fireballs
        world.setGameRuleValue("doFireTick", "" + ToggleSetting.FIRE_SPREAD.isEnabled());
        world.setGameRuleValue("doTileDrops", "" + ToggleSetting.BLOCK_DROPS.isEnabled());
        world.setGameRuleValue("doMobLoot", "" + ToggleSetting.ENTITY_DROPS.isEnabled());
        world.setGameRuleValue("doEntityDrops", "" + ToggleSetting.ENTITY_DROPS.isEnabled());
        world.setGameRuleValue("doDaylightCycle", "" + ToggleSetting.DAYLIGHT_CYCLE.isEnabled());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onMatchEnd(MatchEndEvent event) {
        World world = event.getMap().getWorld();
        world.setGameRuleValue("doMobSpawning", "" + false);
        world.setGameRuleValue("mobGriefing", "" + false);
        world.setGameRuleValue("doMobLoot", "" + false);
        world.setGameRuleValue("doFireTick", "" + false);
        world.setGameRuleValue("doTileDrops", "" + false);
        world.setGameRuleValue("doEntityDrops", "" + false);
        world.setGameRuleValue("doDaylightCycle", "" + false);
    }

    @EventHandler
    public void onWorldInit(WorldInitEvent event) {
        World world = event.getWorld();
        if (world.getEnvironment() != World.Environment.NORMAL) {
            world.setSpawnLocation(0, 0, 0);
            world.setKeepSpawnInMemory(true);
            world.setAutoSave(false);
            world.loadChunk(0, 0);
        }
    }

    private void replaceOceanBiomes() {
        try {
            Field biomesField = BiomeBase.class.getDeclaredField("biomes");
            biomesField.setAccessible(true);

            if (biomesField.get(null) instanceof BiomeBase[]) {
                BiomeBase[] biomes = (BiomeBase[]) biomesField.get(null);
                biomes[BiomeBase.FROZEN_OCEAN.id] = BiomeBase.COLD_TAIGA;
                biomes[BiomeBase.DEEP_OCEAN.id] = BiomeBase.FOREST;
                biomes[BiomeBase.OCEAN.id] = BiomeBase.PLAINS;
                biomesField.set(null, biomes);
            }
        } catch (Exception ignored) { }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onMatchCycle(MatchCycleEvent event) {
        if (CGM.getPlaylist().isUHC() && !event.hasMap()) {
            this.generateDimensions(false);
        }
    }

    private void generateDimensions(boolean force) {
        if (force || ((NetherScenario) UHCScenario.NETHER.getModule()).isNetherEntered()) {
            this.regenerateWorld(World.Environment.NETHER);
            ((NetherScenario) UHCScenario.NETHER.getModule()).setNetherEntered(false);
        }

        if (force || ((DragonRushScenario) UHCScenario.DRAGON_RUSH.getModule()).isEndEntered()) {
            this.regenerateWorld(World.Environment.THE_END);
            ((DragonRushScenario) UHCScenario.DRAGON_RUSH.getModule()).setEndEntered(false);
        }
    }

    private void regenerateWorld(World.Environment env) {
        String name = env == World.Environment.NETHER ? "uhc_nether" : "uhc_the_end";
        World world = Bukkit.getWorld(name);

        TaskUtils.runAsync(() -> {
            if (world != null) {
                File file = new File(Commons.getRoot() + world);
                Bukkit.unloadWorld(world, false);

                if (file.exists()) {
                    try {
                        FileUtils.cleanDirectory(file);
                        file.delete();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

            TaskUtils.run(() ->  new WorldCreator(name).environment(env).createWorld());
        });
    }

}
