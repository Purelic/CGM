package net.purelic.cgm.core.gamemodes;

import net.md_5.bungee.api.ChatColor;
import net.purelic.cgm.core.gamemodes.constants.GameType;
import net.purelic.cgm.kit.KitType;
import net.purelic.cgm.kit.MatchKit;
import shaded.com.google.cloud.Timestamp;
import shaded.com.google.cloud.firestore.QueryDocumentSnapshot;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class CustomGameMode {

    private final QueryDocumentSnapshot doc;
    private final String id;
    private final UUID author;
    private final String name;
    private final String alias;
    private final String description;
    private final GameType gameType;
    private final boolean isPublic;
    private final Timestamp createdAt;
    private final Map<KitType, MatchKit> kits;

    public CustomGameMode(QueryDocumentSnapshot doc) {
        this.doc = doc;
        this.id = doc.getId();
        this.author = UUID.fromString(this.getOrDefault("author", "1935c8bf-3fdc-44d6-b358-85a7206ad8ac"));
        this.name = doc.getString("name");
        this.alias = this.getOrDefault("alias", this.name);
        this.gameType = GameType.valueOf(this.getOrDefault("game_type", GameType.DEATHMATCH.name()));
        this.description = this.getOrDefault("description", this.gameType.getDescription());
        this.isPublic = doc.getBoolean("public");
        this.createdAt = doc.getTimestamp("created");
        this.kits = new HashMap<>();
        this.setKits();
    }

    public String getId() {
        return this.id;
    }

    public UUID getAuthor() {
        return this.author;
    }

    public String getName() {
        return this.name;
    }

    public String getColoredName() {
        return ChatColor.GOLD + this.name + ChatColor.RESET;
    }

    public String getColoredNameWithAlias() {
        return this.getColoredName() + ChatColor.GRAY + " (" + this.alias + ")" + ChatColor.RESET;
    }

    public String getAlias() {
        return this.alias;
    }

    public String getDescription() {
        return this.description;
    }

    public GameType getGameType() {
        return this.gameType;
    }

    public boolean isPublic() {
        return this.isPublic;
    }

    public Timestamp getCreatedAt() {
        return this.createdAt;
    }

    private Object getSetting(GameSetting setting) {
        return this.doc.get("settings." + setting.getSettingType().getKey() + "." + setting.getKey());
    }

    public int getNumberSetting(NumberSetting setting) {
        Object value = this.getSetting(setting);
        return value == null ? setting.defaultValue() : (int) ((long) value);
    }

    public boolean getToggleSetting(ToggleSetting setting) {
        Object value = this.getSetting(setting);
        return value == null ? setting.isDefaultEnabled() : (boolean) value;
    }

    public String getEnumSetting(EnumSetting setting) {
        Object value = this.getSetting(setting);
        return value == null ? setting.getDefaultValue() : (String) value;
    }

    private String getOrDefault(String field, String defaultValue) {
        String value = this.doc.getString(field);
        return value == null ? defaultValue : value;
    }

    public void loadSettings() {
        this.loadSettings(ToggleSetting.values(), this.doc);
        this.loadSettings(NumberSetting.values(), this.doc);
        this.loadSettings(EnumSetting.values(), this.doc);
    }

    private void loadSettings(GameSetting[] settings, QueryDocumentSnapshot doc) {
        for (GameSetting setting : settings) {
            Map<String, Object> values = (Map<String, Object>) doc.get("settings." + setting.getSettingType().getKey());
            if (values != null) setting.setValue(values.get(setting.getKey()));
        }
        EnumSetting.GAME_TYPE.setValue(this.gameType);
    }

    public void reset() {
        this.reset(ToggleSetting.values());
        this.reset(NumberSetting.values());
        this.reset(EnumSetting.values());
    }

    private void reset(GameSetting[] settings) {
        for (GameSetting setting : settings) setting.reset();
    }

    public MatchKit getKit(KitType kitType) {
        return this.kits.get(kitType);
    }

    private void setKits() {
        for (KitType kitType : KitType.values()) {
            this.kits.put(kitType, new MatchKit(this, kitType));
        }
    }

}
