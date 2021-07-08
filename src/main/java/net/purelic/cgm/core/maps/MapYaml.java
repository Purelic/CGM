package net.purelic.cgm.core.maps;

import net.purelic.cgm.core.constants.MatchTeam;
import net.purelic.cgm.core.maps.bed.Bed;
import net.purelic.cgm.core.maps.chest.LootChest;
import net.purelic.cgm.core.maps.chest.constants.LootChestTier;
import net.purelic.cgm.core.maps.flag.Flag;
import net.purelic.cgm.core.maps.hill.Hill;
import net.purelic.cgm.core.maps.region.*;

import java.util.*;

@SuppressWarnings("unchecked")
public class MapYaml {

    private final Map<String, Object> yaml;
    private final List<UUID> authors;
    private final Date created;
    private final Date updated;
    private final Map<String, Object> spawns;
    private final SpawnPoint obsSpawn;
    private final List<SpawnPoint> soloSpawns;
    private final List<SpawnPoint> redSpawns;
    private final List<SpawnPoint> blueSpawns;
    private final List<SpawnPoint> greenSpawns;
    private final List<SpawnPoint> yellowSpawns;
    private final List<SpawnPoint> aquaSpawns;
    private final List<SpawnPoint> pinkSpawns;
    private final List<SpawnPoint> graySpawns;
    private final List<SpawnPoint> whiteSpawns;
    private final List<Hill> hills;
    private final List<Flag> flags;
    private final List<Bed> beds;
    private final List<Spawner> spawners;
    private final List<JumpPad> jumpPads;
    private final List<LootChest> lootChests;
    private final List<Region> regions;

    // General Settings
    private final int maxBuildLimit;
    private final int minBuildLimit;
    private final boolean blockProtection;
    private final boolean blockPlacement;
    private final boolean nightVision;
    private final int tickSpeed;

    public MapYaml(Map<String, Object> yaml) {
        this.yaml = yaml;
        this.authors = this.loadAuthors();
        this.created = (Date) yaml.getOrDefault("created", new Date());
        this.updated = (Date) yaml.getOrDefault("updated", this.created);
        this.spawns = (Map<String, Object>) yaml.get("spawns");
        this.obsSpawn = new SpawnPoint(MatchTeam.OBS, (String) spawns.get("obs"));
        this.soloSpawns = this.loadSpawns("solo", MatchTeam.SOLO);
        this.redSpawns = this.loadSpawns("red", MatchTeam.RED);
        this.blueSpawns = this.loadSpawns("blue", MatchTeam.BLUE);
        this.greenSpawns = this.loadSpawns("green", MatchTeam.GREEN);
        this.yellowSpawns = this.loadSpawns("yellow", MatchTeam.YELLOW);
        this.aquaSpawns = this.loadSpawns("aqua", MatchTeam.AQUA);
        this.pinkSpawns = this.loadSpawns("pink", MatchTeam.PINK);
        this.graySpawns = this.loadSpawns("gray", MatchTeam.GRAY);
        this.whiteSpawns = this.loadSpawns("white", MatchTeam.WHITE);
        this.hills = this.loadHills();
        this.flags = this.loadFlags();
        this.beds = this.loadBeds();
        this.spawners = this.loadSpawners();
        this.jumpPads = this.loadJumpPads();
        this.lootChests = this.loadLootChests();
        this.regions = this.loadRegions();
        Map<String, Object> generalSettings = (Map<String, Object>) yaml.getOrDefault("general", new HashMap<>());
        this.maxBuildLimit = (int) generalSettings.getOrDefault("max_build_limit", 100);
        this.minBuildLimit = (int) generalSettings.getOrDefault("min_build_limit", 0);
        this.blockProtection = (boolean) generalSettings.getOrDefault("block_protection", true);
        this.blockPlacement = (boolean) generalSettings.getOrDefault("block_placement", false);
        this.nightVision = (boolean) generalSettings.getOrDefault("night_vision", false);
        this.tickSpeed = (int) generalSettings.getOrDefault("tick_speed", 0);
    }

    public List<UUID> getAuthors() {
        return this.authors;
    }

    public Date getCreated() {
        return this.created;
    }

    public Date getUpdated() {
        return this.updated;
    }

    public SpawnPoint getObsSpawn() {
        return this.obsSpawn;
    }

    public List<SpawnPoint> getSoloSpawns() {
        return this.soloSpawns;
    }

    public List<SpawnPoint> getBlueSpawns() {
        return this.blueSpawns;
    }

    public List<SpawnPoint> getRedSpawns() {
        return this.redSpawns;
    }

    public List<SpawnPoint> getGreenSpawns() {
        return this.greenSpawns;
    }

    public List<SpawnPoint> getYellowSpawns() {
        return this.yellowSpawns;
    }

    public List<SpawnPoint> getAquaSpawns() {
        return this.aquaSpawns;
    }

    public List<SpawnPoint> getPinkSpawns() {
        return this.pinkSpawns;
    }

    public List<SpawnPoint> getGraySpawns() {
        return this.graySpawns;
    }

    public List<SpawnPoint> getWhiteSpawns() {
        return this.whiteSpawns;
    }

    public List<Hill> getHills() {
        return this.hills;
    }

    public List<Flag> getFlags() {
        return this.flags;
    }

    public List<Bed> getBeds() {
        return this.beds;
    }

    public List<Spawner> getSpawners() {
        return this.spawners;
    }

    public List<JumpPad> getJumpPads() {
        return this.jumpPads;
    }

    public List<LootChest> getLootChests() {
        return this.lootChests;
    }

    public List<Region> getRegions() {
        return this.regions;
    }

    public int getMaxBuildLimit() {
        return this.maxBuildLimit;
    }

    public int getMinBuildLimit() {
        return this.minBuildLimit;
    }

    public boolean canPlaceBlocks() {
        return this.blockPlacement;
    }

    public boolean canBreakBlocks() {
        return !this.blockProtection;
    }

    public boolean hasNightVision() {
        return this.nightVision;
    }

    public int getTickSpeed() {
        return this.tickSpeed;
    }

    private List<UUID> loadAuthors() {
        List<UUID> authors = new ArrayList<>();
        ((List<String>) this.yaml.getOrDefault("authors", new ArrayList<String>()))
                .forEach(entry -> authors.add(UUID.fromString(entry)));
        return authors;
    }

    private List<SpawnPoint> loadSpawns(String field, MatchTeam team) {
        List<SpawnPoint> spawns = new ArrayList<>();
        ((List<String>) this.spawns.getOrDefault(field, new ArrayList<String>()))
                .forEach(entry -> spawns.add(new SpawnPoint(team, entry)));
        return spawns;
    }

    private List<Hill> loadHills() {
        List<Hill> hills = new ArrayList<>();
        ((List<Map<String, Object>>) this.yaml.getOrDefault("hills", new ArrayList<Map<String, Object>>()))
                .forEach(entry -> hills.add(new Hill(entry)));
        return hills;
    }

    private List<Flag> loadFlags() {
        List<Flag> flags = new ArrayList<>();
        ((List<Map<String, Object>>) this.yaml.getOrDefault("flags", new ArrayList<Map<String, Object>>()))
                .forEach(entry -> flags.add(new Flag(entry)));
        return flags;
    }

    private List<Bed> loadBeds() {
        List<Bed> beds = new ArrayList<>();
        ((List<Map<String, Object>>) this.yaml.getOrDefault("beds", new ArrayList<Map<String, Object>>()))
                .forEach(entry -> beds.add(new Bed(entry)));
        return beds;
    }

    private List<Spawner> loadSpawners() {
        List<Spawner> spawners = new ArrayList<>();
        ((List<Map<String, Object>>) this.yaml.getOrDefault("spawners", new ArrayList<Map<String, Object>>()))
                .forEach(entry -> spawners.add(new Spawner(entry)));
        return spawners;
    }

    private List<JumpPad> loadJumpPads() {
        List<JumpPad> jumpPads = new ArrayList<>();
        ((List<Map<String, Object>>) this.yaml.getOrDefault("jump_pads", new ArrayList<Map<String, Object>>()))
            .forEach(entry -> jumpPads.add(new JumpPad(entry)));
        return jumpPads;
    }

    private List<LootChest> loadLootChests() {
        List<LootChest> lootChests = new ArrayList<>();
        Map<String, Object> chests = (Map<String, Object>) this.yaml.getOrDefault("chests", new HashMap<>());

        for (LootChestTier tier : LootChestTier.values()) {
            ((List<String>) chests.getOrDefault(tier.name().toLowerCase(), new ArrayList<String>()))
                .forEach(entry -> lootChests.add(new LootChest(entry, tier)));
        }

        return lootChests;
    }

    private List<Region> loadRegions() {
        List<Region> regions = new ArrayList<>();

        ((List<Map<String, Object>>) this.yaml.getOrDefault("regions", new ArrayList<Map<String, Object>>())).forEach(entry -> {
            RegionType type = RegionType.valueOf((String) entry.getOrDefault("type", RegionType.CUBOID.name()));

            if (type == RegionType.CUBOID) {
                regions.add(new CuboidRegion(entry));
            } else if (type == RegionType.CYLINDER) {
                regions.add(new CylinderRegion(entry));
            } else if (type == RegionType.ELLIPSOID) {
                regions.add(new EllipsoidRegion(entry));
            }
        });

        return regions;
    }

}
