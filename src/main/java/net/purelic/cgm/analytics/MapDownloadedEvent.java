package net.purelic.cgm.analytics;

import net.purelic.cgm.core.maps.CustomMap;
import net.purelic.commons.analytics.AnalyticsEvent;
import org.bukkit.entity.Player;

public class MapDownloadedEvent extends AnalyticsEvent {

    public MapDownloadedEvent(Player player, CustomMap map) {
        super("Map Downloaded", player);
        this.properties.put("map_name", map.getName());
        this.properties.put("author_id", map.getYaml().getAuthors().get(0).toString());
    }

}
