package net.purelic.cgm.core.gamemodes;

import net.purelic.cgm.core.gamemodes.constants.*;

import java.util.Arrays;
import java.util.HashSet;

public enum EnumSetting implements GameSetting {

    GAME_TYPE(GameSettingType.GENERAL, GameType.class, GameType.DEATHMATCH),
    TEAM_TYPE(GameSettingType.GENERAL, TeamType.class, TeamType.SOLO),
    TEAM_SIZE(GameSettingType.GENERAL, TeamSize.class, TeamSize.NORMAL),

    GAPPLE_DROP_TYPE(GameSettingType.DEATH_DROPS, DropType.class, DropType.NONE),
    ARROW_DROP_TYPE(GameSettingType.DEATH_DROPS, DropType.class, DropType.HALF),
    WOOL_DROP_TYPE(GameSettingType.DEATH_DROPS, DropType.class, DropType.NONE),
    EMERALD_DROP_TYPE(GameSettingType.DEATH_DROPS, DropType.class, DropType.ALL),
    PEARL_DROP_TYPE(GameSettingType.DEATH_DROPS, DropType.class, DropType.NONE),

    PLAYER_HELMET_TYPE(GameSettingType.PLAYER_HELMET, ArmorType.class, ArmorType.LEATHER),
    PLAYER_CHESTPLATE_TYPE(GameSettingType.PLAYER_CHESTPLATE, ArmorType.class, ArmorType.LEATHER),
    PLAYER_LEGGINGS_TYPE(GameSettingType.PLAYER_LEGGINGS, ArmorType.class, ArmorType.LEATHER),
    PLAYER_BOOTS_TYPE(GameSettingType.PLAYER_BOOTS, ArmorType.class, ArmorType.LEATHER),

    INFECTED_HELMET_TYPE(GameSettingType.INFECTED_HELMET, ArmorType.class, ArmorType.NONE),
    INFECTED_CHESTPLATE_TYPE(GameSettingType.INFECTED_CHESTPLATE, ArmorType.class, ArmorType.LEATHER),
    INFECTED_LEGGINGS_TYPE(GameSettingType.INFECTED_LEGGINGS, ArmorType.class, ArmorType.LEATHER),
    INFECTED_BOOTS_TYPE(GameSettingType.INFECTED_BOOTS, ArmorType.class, ArmorType.LEATHER),

    PLAYER_SWORD_TYPE(GameSettingType.PLAYER_SWORD, SwordType.class, SwordType.GOLD),
    PLAYER_BOW_TYPE(GameSettingType.PLAYER_BOW, BowType.class, BowType.BOW),

    INFECTED_SWORD_TYPE(GameSettingType.INFECTED_SWORD, SwordType.class, SwordType.GOLD),
    INFECTED_BOW_TYPE(GameSettingType.INFECTED_BOW, BowType.class, BowType.NONE),

    PLAYER_COMPASS_TYPE(GameSettingType.PLAYER_COMPASS, CompassTrackingType.class, CompassTrackingType.PLAYER),

    LOOT_TYPE(GameSettingType.SURVIVAL_GAMES, LootType.class, LootType.SG_NORMAL),
    ;

    private final GameSettingType settingType;
    private final String key;
    private final Class clazz;
    private final Object defaultValue;
    private Object value;

    EnumSetting(GameSettingType settingType, Class<? extends Enum<?>> clazz, Object defaultValue) {
        this.settingType = settingType;
        this.key = this.name().toLowerCase();
        this.clazz = clazz;
        this.defaultValue = defaultValue;
        this.value = defaultValue;
    }

    public <E extends Enum<E>> E get() {
        return (E) this.value;
    }

    public boolean is(Object o) {
        return this.value.equals(o);
    }

    public boolean is(Object... o) {
        return (new HashSet<>(Arrays.asList(o))).contains(this.value);
    }

    public void setValue(String value) {
        this.setValue(this.clazz, value);
    }

    public String getDefaultValue() {
        return this.defaultValue.toString();
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
        if (value == null) {
            this.value = this.defaultValue;
        } else {
            this.setValue(this.clazz, value.toString());
        }
    }

    @Override
    public boolean isDefault() {
        return this.defaultValue == this.value;
    }

    private <E extends Enum<E>> void setValue(Class<E> c, String s) {
        this.value = Enum.valueOf(c, s);
    }

    @Override
    public void reset() {
        this.value = this.defaultValue;
    }

}
