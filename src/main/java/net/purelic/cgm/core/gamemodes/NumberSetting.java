package net.purelic.cgm.core.gamemodes;

public enum NumberSetting implements GameSetting {

    SCORE_LIMIT(GameSettingType.GENERAL, 25),
    ROUNDS(GameSettingType.GENERAL, 1),
    TIME_LIMIT(GameSettingType.GENERAL, 10),
    OVERTIME(GameSettingType.GENERAL, 30),

    DEATHMATCH_KILL_POINTS(GameSettingType.DEATHMATCH, 1),
    DEATHMATCH_DEATH_POINTS(GameSettingType.DEATHMATCH, 0),
    DEATHMATCH_SUICIDE_POINTS(GameSettingType.DEATHMATCH, -1),

    KILL_REWARD_GAPPLES(GameSettingType.KILL_REWARDS, 1),
    KILL_REWARD_ARROWS(GameSettingType.KILL_REWARDS, 0),
    KILL_REWARD_EMERALDS(GameSettingType.KILL_REWARDS, 0),
    KILL_REWARD_PEARLS(GameSettingType.KILL_REWARDS, 0),

    PLAYER_HELMET_PROT(GameSettingType.PLAYER_HELMET, 3),
    PLAYER_CHESTPLATE_PROT(GameSettingType.PLAYER_CHESTPLATE, 3),
    PLAYER_LEGGINGS_PROT(GameSettingType.PLAYER_LEGGINGS, 3),
    PLAYER_BOOTS_PROT(GameSettingType.PLAYER_BOOTS, 3),
    PLAYER_BOOTS_FF(GameSettingType.PLAYER_BOOTS, 1),

    PLAYER_SWORD_SHARP(GameSettingType.PLAYER_SWORD, 2),
    PLAYER_SWORD_KB(GameSettingType.PLAYER_SWORD, 0),

    PLAYER_BOW_POWER(GameSettingType.PLAYER_BOW, 1),
    PLAYER_BOW_PUNCH(GameSettingType.PLAYER_BOW, 0),

    PLAYER_SHEAR_EFF(GameSettingType.PLAYER_SHEARS, 3),
    // PLAYER_FLINT_USES(GameSettingType.PLAYER_FLINT, 0),
    // PLAYER_ROD_USES(GameSettingType.PLAYER_ROD, 0),

    PLAYER_WOOL(GameSettingType.PLAYER_ITEMS, 0),
    PLAYER_ARROWS(GameSettingType.PLAYER_ITEMS, 8),
    PLAYER_GAPPLES(GameSettingType.PLAYER_ITEMS, 1),
    PLAYER_EMERALDS(GameSettingType.PLAYER_ITEMS, 0),
    PLAYER_PEARLS(GameSettingType.PLAYER_ITEMS, 0),

    PLAYER_RESISTANCE(GameSettingType.PLAYER_EFFECTS, 0),
    PLAYER_SPEED(GameSettingType.PLAYER_EFFECTS, 0),
    PLAYER_JUMP_BOOST(GameSettingType.PLAYER_EFFECTS, 0),
    PLAYER_HASTE(GameSettingType.PLAYER_EFFECTS, 0),
    PLAYER_STRENGTH(GameSettingType.PLAYER_EFFECTS,0),

    LIVES_PER_ROUND(GameSettingType.RESPAWN, 0),
    RESPAWN_TIME(GameSettingType.RESPAWN, 5),
    RESPAWN_SUICIDE_PENALTY(GameSettingType.RESPAWN, 5),
    RESPAWN_TIME_GROWTH(GameSettingType.RESPAWN, 0),
    RESPAWN_MAX_TIME(GameSettingType.RESPAWN, 15),

    HILL_MOVE_INTERVAL(GameSettingType.KING_OF_THE_HILL, 0),
    HILL_CAPTURE_DELAY(GameSettingType.KING_OF_THE_HILL, 5),
    TOTAL_HILLS(GameSettingType.KING_OF_THE_HILL, 0),
    HILL_CAPTURE_MULTIPLIER(GameSettingType.KING_OF_THE_HILL, 50),
    HILL_CAPTURE_POINTS(GameSettingType.KING_OF_THE_HILL, 1),

    HEAD_COLLECTED_POINTS(GameSettingType.HEAD_HUNTER, 1),
    HEAD_RECOVERED_POINTS(GameSettingType.HEAD_HUNTER, 1),
    HEAD_STOLEN_POINTS(GameSettingType.HEAD_HUNTER, 1),
    HEAD_COLLECTION_INTERVAL(GameSettingType.HEAD_HUNTER, 0),

    FLAG_CARRIER_MAX_HEALTH(GameSettingType.FLAG_CARRIER_HEALTH, 10),
    FLAG_CARRIER_RESISTANCE(GameSettingType.FLAG_CARRIER_HEALTH, 0),
    FLAG_CARRIER_MELEE_MODIFIER(GameSettingType.FLAG_CARRIER_DAMAGE, 100),
    FLAG_CARRIER_SPEED(GameSettingType.FLAG_CARRIER_MOVEMENT, 0),
    FLAG_CARRIER_SLOWNESS(GameSettingType.FLAG_CARRIER_MOVEMENT, 0),
    FLAG_CARRIER_JUMP_BOOST(GameSettingType.FLAG_CARRIER_MOVEMENT, 0),

    TOTAL_FLAGS(GameSettingType.CAPTURE_THE_FLAG, 0),
    FLAG_RESET_DELAY(GameSettingType.CAPTURE_THE_FLAG, 30),
    FLAG_RETURN_DELAY(GameSettingType.CAPTURE_THE_FLAG, 15),
    FLAG_RESPAWN_DELAY(GameSettingType.CAPTURE_THE_FLAG, 10),
    FLAG_VOIDED_DELAY(GameSettingType.CAPTURE_THE_FLAG, 0), // not available in game dev
    FLAG_RETURN_MULTIPLIER(GameSettingType.CAPTURE_THE_FLAG, 50),
    FLAG_CARRIER_POINTS(GameSettingType.CAPTURE_THE_FLAG, 0),
    FLAG_COLLECTION_INTERVAL(GameSettingType.CAPTURE_THE_FLAG, 0),

    BED_WARS_SUDDEN_DEATH(GameSettingType.BED_WARS, 5),

    // Survival Games
    REFILL_EVENT(GameSettingType.SURVIVAL_GAMES, 3),

    // World Border
    WB_MAX_SIZE(GameSettingType.WORLD_BORDER, 500),
    WB_MIN_SIZE(GameSettingType.WORLD_BORDER, 500),
    WB_SHRINK_DELAY(GameSettingType.WORLD_BORDER, 0),
    WB_SHRINK_SPEED(GameSettingType.WORLD_BORDER, 0),
    ;

    private final GameSettingType settingType;
    private final String key;
    private final int defaultValue;
    private int value;

    NumberSetting(GameSettingType settingType, int defaultValue) {
        this.settingType = settingType;
        this.key = this.name().toLowerCase();
        this.defaultValue = defaultValue;
        this.value = defaultValue;
    }

    public int defaultValue() {
        return this.defaultValue;
    }

    public int value() {
        return this.value;
    }

    public void setValue(int value) {
        this.value = value;
    }

    public void addValue(int value) {
        this.value += value;
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
        this.value = value == null ? this.defaultValue : (int) ((long) value);
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
