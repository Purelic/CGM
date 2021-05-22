package net.purelic.cgm.core.maps;

import net.md_5.bungee.api.ChatColor;
import net.purelic.cgm.core.constants.MatchTeam;
import net.purelic.cgm.core.gamemodes.CustomGameMode;
import net.purelic.cgm.core.gamemodes.EnumSetting;
import net.purelic.cgm.core.gamemodes.NumberSetting;
import net.purelic.cgm.core.gamemodes.ToggleSetting;
import net.purelic.cgm.core.gamemodes.constants.GameType;
import net.purelic.cgm.core.gamemodes.constants.TeamType;
import net.purelic.cgm.core.managers.ScoreboardManager;
import net.purelic.cgm.core.maps.bed.Bed;
import net.purelic.cgm.core.maps.chest.LootChest;
import net.purelic.cgm.core.maps.flag.Flag;
import net.purelic.cgm.core.maps.hill.Hill;
import net.purelic.cgm.utils.EntityUtils;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.EntityType;

import java.util.List;
import java.util.stream.Collectors;

public class CustomMap {

    private final String name;
    private final MapYaml yaml;
    private World world;
    private World nextWorld;

    public CustomMap(String name, MapYaml yaml) {
        this.name = name;
        this.yaml = yaml;
    }

    public String getName() {
        return this.name;
    }

    public MapYaml getYaml() {
        return this.yaml;
    }

    public World getWorld() {
        return this.world;
    }

    public void setNextWorld(World world) {
        this.nextWorld = world;
        if (this.world == null) this.world = world;
    }

    public void loadObjectives() {
        this.world = this.nextWorld;

        this.loadHills();
        this.loadFlags();
        this.loadBeds();
        this.loadSpawners();
        this.loadChests();
        this.loadJumpPads();

        if (EnumSetting.GAME_TYPE.is(GameType.BED_WARS)) {
            this.world.getEntities().stream()
                    .filter(entity -> entity.getType() == EntityType.VILLAGER)
                    .forEach(villager -> EntityUtils.setAi(villager, false));
        }
    }

    private void loadHills() {
        this.yaml.getHills().forEach(Hill::reset);

        GameType gameType = EnumSetting.GAME_TYPE.get();

        if (gameType == GameType.KING_OF_THE_HILL
                || (gameType == GameType.HEAD_HUNTER && ToggleSetting.HEAD_COLLECTION_HILLS.isEnabled())
                || (gameType == GameType.CAPTURE_THE_FLAG && ToggleSetting.FLAG_GOALS.isEnabled())) {
            boolean neutralHills = ToggleSetting.NEUTRAL_HILLS.isEnabled();
            TeamType teamType = EnumSetting.TEAM_TYPE.get();
            List<MatchTeam> teams = teamType.getTeams();
            int totalHills = NumberSetting.TOTAL_HILLS.value();
            boolean movingHills = NumberSetting.HILL_MOVE_INTERVAL.value() > 0;

            // filter on hills valid for this game mode
            List<Hill> hills = this.yaml.getHills().stream().filter(hill ->
                    (neutralHills ? hill.isNeutral() : !hill.isNeutral() && teams.contains(hill.getOwner()))
                            && hill.getType() == gameType.getHillType()
            ).collect(Collectors.toList());

            // if total hills = 0, load all hills
            if (totalHills <= 0) {
                totalHills = hills.size();
            }

            int startingRow = teamType == TeamType.SOLO ? 0 : teams.size() + 1;

            if (ToggleSetting.PERMANENT_HILLS.isEnabled()) {
                startingRow = -1;
            } else {
                ScoreboardManager.setScore(teamType == TeamType.SOLO ? startingRow + 1 : startingRow - 1, "");
            }

            if (neutralHills) {
                for (int i = 0; i < totalHills; i++) {
                    Hill hill = hills.get(i);
                    boolean active = !movingHills || i == 0;
                    int scoreboardRow = movingHills || startingRow == -1 ? startingRow : startingRow + i;
                    hill.setWorld(scoreboardRow, this.world, active);
                }
            } else {
                int i = 0;
                for (MatchTeam team : teams) {
                    int c = 0;
                    for (Hill hill : hills) {
                        if (c == totalHills) break; // load the same number of hills for each team
                        if (hill.getOwner() == team) { // only load hills teams valid for this game mode
                            hill.setWorld(startingRow == -1 ? startingRow : startingRow + i, this.world, true);
                            c++;
                            i++;
                        }
                    }
                }
            }
        }
    }

    public List<Hill> getLoadedHills() {
        return this.yaml.getHills().stream().filter(Hill::isLoaded).collect(Collectors.toList());
    }

    private void loadFlags() {
        GameType gameType = EnumSetting.GAME_TYPE.get();

        if (gameType!= GameType.CAPTURE_THE_FLAG) return;

        boolean neutralFlags = ToggleSetting.NEUTRAL_FLAGS.isEnabled();
        TeamType teamType = EnumSetting.TEAM_TYPE.get();
        List<MatchTeam> teams = teamType.getTeams();
        int totalFlags = NumberSetting.TOTAL_FLAGS.value();
        boolean movingFlag = ToggleSetting.MOVING_FLAG.isEnabled();

        // filter on flags valid for this game mode
        List<Flag> flags = this.yaml.getFlags().stream().filter(flag ->
                (neutralFlags ? flag.isNeutral() : !flag.isNeutral() && teams.contains(flag.getOwner()))
        ).collect(Collectors.toList());

        // if total flags = 0, load all flags
        if (totalFlags <= 0) {
            totalFlags = flags.size();
        }

        int hillOffset = (int) this.getLoadedHills().stream().filter(hill -> hill.getScoreboardRow() != -1).count();
        hillOffset = hillOffset == 0 ? 0 : hillOffset + 1;

        int startingRow = (teamType == TeamType.SOLO ? 0 : teams.size() + 1) + hillOffset;

        if (hillOffset > 0) ScoreboardManager.setScore(startingRow - 1, ""); // blank line between hills and flags
        ScoreboardManager.setScore(teamType == TeamType.SOLO ? (movingFlag ? startingRow + 1 : totalFlags) : startingRow - 1, "");

        if (neutralFlags) {
            for (int i = 0; i < Math.min(totalFlags, flags.size()); i++) {
                Flag flag = flags.get(i);
                boolean place = !movingFlag || i == 0;
                int scoreboardRow = movingFlag ? startingRow : startingRow + i;
                flag.setWorld(this.world, place, scoreboardRow);
            }
        } else {
            int i = 0;
            for (MatchTeam team : teams) {
                int c = 0;
                for (Flag flag : flags) {
                    if (c == totalFlags) break; // load the same number of flags for each team
                    if (flag.getOwner() == team) {
                        flag.setWorld(this.world, true, startingRow + i);
                        c++;
                        i++;
                    }
                }
            }
        }
    }

    public List<Bed> getLoadedBeds() {
        return this.yaml.getBeds().stream().filter(Bed::isLoaded).collect(Collectors.toList());
    }

    private void loadBeds() {
        this.yaml.getBeds().forEach(Bed::reset);

        GameType gameType = EnumSetting.GAME_TYPE.get();

        if (gameType!= GameType.BED_WARS) return;

        TeamType teamType = EnumSetting.TEAM_TYPE.get();
        List<MatchTeam> teams = teamType.getTeams();

        // filter on beds valid for this game mode
        List<Bed> beds = this.yaml.getBeds().stream().filter(bed ->
                (teams.contains(bed.getOwner()))
        ).collect(Collectors.toList());

        for (MatchTeam team : teams) {
            // currently only allowed to load one bed per team
            beds.stream().filter(bed -> bed.getOwner() == team).findFirst().ifPresent(bed -> bed.place(this.world));
        }
    }

    public List<Flag> getLoadedFlags() {
        return this.yaml.getFlags().stream().filter(Flag::isLoaded).collect(Collectors.toList());
    }

    private void loadSpawners() {
        GameType gameType = EnumSetting.GAME_TYPE.get();
        boolean oneHitKill = ToggleSetting.PLAYER_SWORD_INSTANT_KILL.isEnabled();
        boolean oneShotKill = ToggleSetting.PLAYER_BOW_INSTANT_KILL.isEnabled();

        for (Spawner spawner : this.yaml.getSpawners()) {
            Material material = spawner.getMaterial();

            if (oneHitKill && material == Material.GOLDEN_APPLE) continue;
            if (oneShotKill && material == Material.ARROW) continue;
            if (!gameType.isType(GameType.HEAD_HUNTER) && material == Material.SKULL_ITEM) continue;
            if (this.overlapsObjective(spawner)) continue;

            spawner.create(this.world);
        }
    }

    private void loadChests() {
        if (!EnumSetting.GAME_TYPE.is(GameType.SURVIVAL_GAMES)) return;

        for (LootChest lootChest : this.yaml.getLootChests()) {
            lootChest.setWorld(this.world, EnumSetting.LOOT_TYPE.get());
        }
    }

    private void loadJumpPads() {
        if (!ToggleSetting.JUMP_PADS.isEnabled()) return;

        for (JumpPad jumpPad : this.yaml.getJumpPads()) {
            jumpPad.create(this.world);
        }
    }

    private boolean overlapsObjective(Spawner spawner) {
        Location spawnerLoc = spawner.getLocation();
        int spawnerX = spawnerLoc.getBlockX();
        int spawnerY = spawnerLoc.getBlockY();
        int spawnerZ = spawnerLoc.getBlockZ();

        for (Flag flag : this.getLoadedFlags()) {
            int[] coords = flag.getCoords();

            if (coords[0] == spawnerX
                && (coords[1] - 1) == spawnerY
                && coords[2] == spawnerZ) return true;
        }

        for (Hill hill : this.getLoadedHills()) {
            int[] coords = hill.getCoords();

            if (coords[0] == spawnerX
                    && coords[1] == spawnerY
                    && coords[2] == spawnerZ) return true;
        }

        return false;
    }

    private boolean supportsTeamType(TeamType teamType) {
        if (teamType == TeamType.SOLO) return this.supportsSolo();
        if (teamType == TeamType.TEAMS) return this.supportsTeams();
        if (teamType == TeamType.MULTI_TEAM) return this.supportsMultiTeam();
        if (teamType == TeamType.SQUADS) return this.supportsSquads();
        else return false;
    }

    private boolean supportsSolo() {
        return !this.yaml.getSoloSpawns().isEmpty();
    }

    private boolean supportsTeams() {
        return !this.yaml.getBlueSpawns().isEmpty() && !this.yaml.getRedSpawns().isEmpty();
    }

    private boolean supportsMultiTeam() {
        return this.supportsTeams() && !this.yaml.getGreenSpawns().isEmpty() && !this.yaml.getYellowSpawns().isEmpty();
    }

    private boolean supportsSquads() {
        return this.supportsMultiTeam() && !this.yaml.getAquaSpawns().isEmpty() && !this.yaml.getPinkSpawns().isEmpty()
                && !this.yaml.getWhiteSpawns().isEmpty() && !this.yaml.getGraySpawns().isEmpty();
    }

    private boolean supportsHills(CustomGameMode gameMode, GameType gameType) {
        List<Hill> hills = this.yaml.getHills().stream().filter(hill -> hill.getType() == gameType.getHillType()).collect(Collectors.toList());
        int totalHills = gameMode.getNumberSetting(NumberSetting.TOTAL_HILLS);

        boolean supports = hills.size() > 0 && (totalHills == 0 || hills.size() >= totalHills);

        TeamType teamType = TeamType.valueOf(gameMode.getEnumSetting(EnumSetting.TEAM_TYPE));
        int teams = teamType.getTeams().size();

        if (!gameMode.getToggleSetting(ToggleSetting.NEUTRAL_HILLS)) {
            int ownedRegions = hills.stream().filter(hill -> !hill.isNeutral()).toArray().length;
            supports = supports && ownedRegions % teams == 0 && ownedRegions > 0;
        } else {
            int neutralHills = hills.stream().filter(Hill::isNeutral).toArray().length;
            supports = supports && neutralHills >= totalHills;
        }

        if (gameMode.getNumberSetting(NumberSetting.HILL_MOVE_INTERVAL) != 0) {
            supports = supports && hills.size() > 1;
        }

        return supports;
    }

    private boolean supportsFlags(CustomGameMode gameMode) {
        List<Flag> flags = this.yaml.getFlags();
        boolean supports = flags.size() > 0;

        int totalFlags = gameMode.getToggleSetting(ToggleSetting.MOVING_FLAG) ? 1 : gameMode.getNumberSetting(NumberSetting.TOTAL_FLAGS);

        if (gameMode.getToggleSetting(ToggleSetting.NEUTRAL_FLAGS)) {
            int neutralFlags = flags.stream().filter(Flag::isNeutral).toArray().length;
            supports = supports && neutralFlags >= totalFlags && neutralFlags > 0;
        } else {
            int totalTeams = TeamType.valueOf(gameMode.getEnumSetting(EnumSetting.TEAM_TYPE)).getTeams().size();
            int ownedFlags = flags.stream().filter(flag -> !flag.isNeutral()).toArray().length;
            supports = supports && ownedFlags > 0 && ownedFlags >= (totalTeams * totalFlags);
            // TODO this isn't perfect because what if there were 3 red flags and 1 blue flag, then multi-team would be supported
        }

        return supports;
    }

    private boolean hasBeds(CustomGameMode gameMode) {
        return this.yaml.getBeds().size() > 0; // TODO this needs to be a lot better
    }

    private boolean hasChests(CustomGameMode gameMode) {
        return this.yaml.getLootChests().size() > 0;
    }

    public boolean supportsGameMode(CustomGameMode gameMode) {
        boolean supports = this.supportsTeamType(TeamType.valueOf(gameMode.getEnumSetting(EnumSetting.TEAM_TYPE)));

        GameType gameType = gameMode.getGameType();

        if (gameType == GameType.KING_OF_THE_HILL
            || (gameType == GameType.HEAD_HUNTER && gameMode.getToggleSetting(ToggleSetting.HEAD_COLLECTION_HILLS))) {
            supports = supports && this.supportsHills(gameMode, gameType);
        }

        if (gameType == GameType.CAPTURE_THE_FLAG) {
            supports = supports && this.supportsFlags(gameMode);
        }

        if (gameType == GameType.BED_WARS) {
            supports = supports && this.hasBeds(gameMode);
        }

        if (gameType == GameType.SURVIVAL_GAMES) {
            supports = supports && this.hasChests(gameMode);
        }

        return supports;
    }

    public String getColoredName() {
        return ChatColor.YELLOW + this.name + ChatColor.RESET;
    }

}
