package net.purelic.cgm.core.gamemodes;

public enum ToggleSetting implements GameSetting {

    FRIENDLY_FIRE(GameSettingType.GENERAL, false),
    DYNAMIC_REGEN(GameSettingType.KILL_REWARDS, false),
    TEAM_SWITCHING(GameSettingType.GENERAL, true),
    ATTACK_DEFENSE(GameSettingType.GENERAL, false),

    BLACKOUT_RESPAWN(GameSettingType.RESPAWN, false),

    DROP_TRADED_ITEMS(GameSettingType.DEATH_DROPS, false),

    PLAYER_HELMET_LOCKED(GameSettingType.PLAYER_HELMET, false),
    PLAYER_CHESTPLATE_LOCKED(GameSettingType.PLAYER_CHESTPLATE, false),
    PLAYER_LEGGINGS_LOCKED(GameSettingType.PLAYER_LEGGINGS, false),
    PLAYER_BOOTS_LOCKED(GameSettingType.PLAYER_BOOTS, false),

    INFECTED_HELMET_LOCKED(GameSettingType.INFECTED_HELMET, true),
    INFECTED_CHESTPLATE_LOCKED(GameSettingType.INFECTED_CHESTPLATE, true),
    INFECTED_LEGGINGS_LOCKED(GameSettingType.INFECTED_LEGGINGS, true),
    INFECTED_BOOTS_LOCKED(GameSettingType.INFECTED_BOOTS, true),

    PLAYER_HELMET_UNBREAKABLE(GameSettingType.PLAYER_HELMET, true),
    PLAYER_CHESTPLATE_UNBREAKABLE(GameSettingType.PLAYER_CHESTPLATE, true),
    PLAYER_LEGGINGS_UNBREAKABLE(GameSettingType.PLAYER_LEGGINGS, true),
    PLAYER_BOOTS_UNBREAKABLE(GameSettingType.PLAYER_BOOTS, true),

    INFECTED_HELMET_UNBREAKABLE(GameSettingType.INFECTED_HELMET, true),
    INFECTED_CHESTPLATE_UNBREAKABLE(GameSettingType.INFECTED_CHESTPLATE, true),
    INFECTED_LEGGINGS_UNBREAKABLE(GameSettingType.INFECTED_LEGGINGS, true),
    INFECTED_BOOTS_UNBREAKABLE(GameSettingType.INFECTED_BOOTS, true),

    PLAYER_HELMET_AQUA_AFFINITY(GameSettingType.PLAYER_HELMET, false),
    INFECTED_HELMET_AQUA_AFFINITY(GameSettingType.INFECTED_HELMET, false),

    PLAYER_SWORD_UNBREAKABLE(GameSettingType.PLAYER_SWORD, true),
    PLAYER_SWORD_LOCKED(GameSettingType.PLAYER_SWORD, true),
    PLAYER_SWORD_INSTANT_KILL(GameSettingType.PLAYER_SWORD, false),

    INFECTED_SWORD_UNBREAKABLE(GameSettingType.INFECTED_SWORD, true),
    INFECTED_SWORD_LOCKED(GameSettingType.INFECTED_SWORD, true),
    INFECTED_SWORD_INSTANT_KILL(GameSettingType.INFECTED_SWORD, false),

    PLAYER_BOW_INFINITY(GameSettingType.PLAYER_BOW, false),
    PLAYER_BOW_UNBREAKABLE(GameSettingType.PLAYER_BOW, true),
    PLAYER_BOW_LOCKED(GameSettingType.PLAYER_BOW, true),
    PLAYER_BOW_INSTANT_KILL(GameSettingType.PLAYER_BOW, false),

    INFECTED_BOW_INFINITY(GameSettingType.INFECTED_BOW, false),
    INFECTED_BOW_UNBREAKABLE(GameSettingType.INFECTED_BOW, true),
    INFECTED_BOW_LOCKED(GameSettingType.INFECTED_BOW, true),
    INFECTED_BOW_INSTANT_KILL(GameSettingType.INFECTED_BOW, false),

    PLAYER_SHEARS_ENABLED(GameSettingType.PLAYER_SHEARS, false),
    INFECTED_SHEARS_ENABLED(GameSettingType.INFECTED_SHEARS, false),

    PLAYER_COMPASS_ENABLED(GameSettingType.PLAYER_COMPASS, false),
    PLAYER_COMPASS_DISPLAY(GameSettingType.PLAYER_COMPASS, false),
    PLAYER_COMPASS_SPAWN_WITH(GameSettingType.PLAYER_COMPASS, true),

    INFECTED_COMPASS_ENABLED(GameSettingType.INFECTED_COMPASS, false),
    INFECTED_COMPASS_DISPLAY(GameSettingType.INFECTED_COMPASS, false),
    INFECTED_COMPASS_SPAWN_WITH(GameSettingType.INFECTED_COMPASS, true),

    PLAYER_PICKAXE_LOCKED(GameSettingType.PLAYER_TOOLS, true),
    PLAYER_AXE_LOCKED(GameSettingType.PLAYER_TOOLS, true),

    PLAYER_NATURAL_REGEN(GameSettingType.PLAYER_HEALTH, true),
    PLAYER_IMMUNE_TO_MELEE(GameSettingType.PLAYER_HEALTH, false),
    PLAYER_IMMUNE_TO_PROJECTILES(GameSettingType.PLAYER_HEALTH, false),
    PLAYER_IMMUNE_TO_FALL_DAMAGE(GameSettingType.PLAYER_HEALTH, false),

    PLAYER_INVISIBILITY(GameSettingType.PLAYER_EFFECTS, false),
    PLAYER_FIRE_RESISTANCE(GameSettingType.PLAYER_EFFECTS, false),
    PLAYER_BLINDNESS(GameSettingType.PLAYER_EFFECTS, false),

    PLAYER_NAME_VISIBLE(GameSettingType.PLAYER_APPEARANCE, true),

    INFECTED_PICKAXE_LOCKED(GameSettingType.INFECTED_TOOLS, true),
    INFECTED_AXE_LOCKED(GameSettingType.INFECTED_TOOLS, true),

    INFECTED_NATURAL_REGEN(GameSettingType.INFECTED_HEALTH, true),
    INFECTED_IMMUNE_TO_MELEE(GameSettingType.INFECTED_HEALTH, false),
    INFECTED_IMMUNE_TO_PROJECTILES(GameSettingType.INFECTED_HEALTH, false),
    INFECTED_IMMUNE_TO_FALL_DAMAGE(GameSettingType.INFECTED_HEALTH, false),

    INFECTED_INVISIBILITY(GameSettingType.INFECTED_EFFECTS, false),
    INFECTED_FIRE_RESISTANCE(GameSettingType.INFECTED_EFFECTS, false),
    INFECTED_BLINDNESS(GameSettingType.INFECTED_EFFECTS, false),

    COLLECT_HEADS_INSTANTLY(GameSettingType.HEAD_HUNTER, true),
    HEAD_COLLECTION_HILLS(GameSettingType.HEAD_HUNTER, false),
    PLAYERS_DROP_HEADS(GameSettingType.HEAD_HUNTER, true),

    DEATHMATCH_SCOREBOXES(GameSettingType.DEATHMATCH, false),

    NEUTRAL_HILLS(GameSettingType.KING_OF_THE_HILL, true),
    RANDOM_HILLS(GameSettingType.KING_OF_THE_HILL, false),
    CAPTURE_LOCK(GameSettingType.KING_OF_THE_HILL, true),
    PERMANENT_HILLS(GameSettingType.KING_OF_THE_HILL, false),
    CAPTURED_HILLS_TELEPORT(GameSettingType.KING_OF_THE_HILL, false),
    SEQUENTIAL_HILLS(GameSettingType.KING_OF_THE_HILL, false), // TODO impl
    SINGLE_CAPTURE_HILLS(GameSettingType.KING_OF_THE_HILL, false),
    ALL_HILLS_WIN(GameSettingType.KING_OF_THE_HILL, false),

    FLAG_CARRIER_DISABLE_SPRINTING(GameSettingType.FLAG_CARRIER_MOVEMENT, false),
    NEUTRAL_FLAGS(GameSettingType.CAPTURE_THE_FLAG, false),
    FLAG_GOALS(GameSettingType.CAPTURE_THE_FLAG, true),
    MOVING_FLAG(GameSettingType.CAPTURE_THE_FLAG, false),
    FLAG_AT_HOME(GameSettingType.CAPTURE_THE_FLAG, false),
    TELEPORT_ON_CAPTURE(GameSettingType.CAPTURE_THE_FLAG, false),
    RESPAWN_ON_DROP(GameSettingType.CAPTURE_THE_FLAG, false),
    SINGLE_CAPTURE_FLAGS(GameSettingType.CAPTURE_THE_FLAG, true), // TODO impl
    ALL_FLAGS_WIN(GameSettingType.KING_OF_THE_HILL, true), // TODO impl

    SPAWN_PROTECTION(GameSettingType.WORLD, true),
    MOB_SPAWNING(GameSettingType.WORLD, false),
    FIRE_SPREAD(GameSettingType.WORLD, false),
    JUMP_PADS(GameSettingType.WORLD, true),
    BLOCK_DROPS(GameSettingType.WORLD, true),
    ENTITY_DROPS(GameSettingType.WORLD, true),
    LEAVES_DECAY(GameSettingType.WORLD, false),
    DAYLIGHT_CYCLE(GameSettingType.WORLD, false),
    INSTANT_TNT(GameSettingType.WORLD, true),
    RESET_BLOCKS(GameSettingType.WORLD, false);
    ;

    private final GameSettingType settingType;
    private final String key;
    private final boolean defaultValue;
    private boolean value;

    ToggleSetting(GameSettingType settingType,boolean defaultValue) {
        this.settingType = settingType;
        this.key = this.name().toLowerCase();
        this.defaultValue = defaultValue;
        this.value = defaultValue;
    }

    public boolean isDefaultEnabled() {
        return this.defaultValue;
    }

    public boolean isEnabled() {
        return this.value;
    }

    public void toggle() {
        this.value = !this.value;
    }

    @Override
    public GameSettingType getSettingType() {
        return this.settingType;
    }

    @Override
    public String getKey() {
        return this.key;
    }

    @Override
    public String getName() {
        return null;
    }

    @Override
    public String getDescription() {
        return null;
    }

    @Override
    public void setValue(Object value) {
        this.value = value == null ? this.defaultValue : (boolean) value;
    }

    @Override
    public boolean isDefault() {
        return this.defaultValue == this.value;
    }

    @Override
    public void reset() {
        this.value = this.defaultValue;
    }

}
