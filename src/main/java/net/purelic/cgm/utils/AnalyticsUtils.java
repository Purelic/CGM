package net.purelic.cgm.utils;

import org.bukkit.entity.Player;

public class AnalyticsUtils {

    public static String urlBuilder(Player player, String url, String content, String... utms) {
        StringBuilder urlBuilder = new StringBuilder(url)
            .append("?uuid=").append(player.getUniqueId().toString())
            .append("&utm_source=server")
            .append("&utm_medium=chat")
            .append("&utm_content=").append(content);

        for (String utm : utms) {
            urlBuilder.append("&").append(utm);
        }

        return urlBuilder.toString();
    }

}
