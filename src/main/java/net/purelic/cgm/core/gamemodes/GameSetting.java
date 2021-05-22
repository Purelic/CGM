package net.purelic.cgm.core.gamemodes;

public interface GameSetting {

    GameSettingType getSettingType();

    String getKey();

    String getName();

    String getDescription();

    void setValue(Object value);

    boolean isDefault();

    void reset();

}
