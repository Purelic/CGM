package net.purelic.cgm.analytics;

import net.purelic.cgm.core.gamemodes.CustomGameMode;
import net.purelic.commons.analytics.AnalyticsEvent;
import org.bukkit.entity.Player;

public class GameModeDownloadedEvent extends AnalyticsEvent {

    public GameModeDownloadedEvent(Player player, CustomGameMode gameMode) {
        super("Game Mode Downloaded", player);
        this.properties.put("game_mode_id", gameMode.getId());
        this.properties.put("game_mode_name", gameMode.getName());
        this.properties.put("author_id", gameMode.getAuthor().toString());
        this.properties.put("game_type", gameMode.getGameType().name());
        this.properties.put("game_type_name", gameMode.getGameType().getName());
        this.properties.put("created", gameMode.getCreatedAt().toDate());
        this.properties.put("public", gameMode.isPublic());
    }

}
