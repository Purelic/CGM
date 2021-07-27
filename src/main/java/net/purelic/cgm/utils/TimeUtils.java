package net.purelic.cgm.utils;

import net.md_5.bungee.api.ChatColor;
import net.purelic.cgm.core.gamemodes.NumberSetting;

public class TimeUtils {

    public static String getFormattedTime(int time) {
        ChatColor color = getTimeColor(time);

        int minutes = time / 60;
        int seconds = time % 60;
        String timeString = minutes + ":" + seconds;

        if (seconds <= 9) return "" + color + minutes + ":0" + seconds;
        return color + timeString;
    }

    private static ChatColor getTimeColor(int time) {
        if (NumberSetting.TIME_LIMIT.value() == 0) return ChatColor.WHITE;

        int timeLimit = NumberSetting.TIME_LIMIT.value() * 60;

        if (timeLimit < 0) {
            return ChatColor.GRAY;
        }

        double percent = (double) time / (double) timeLimit;

        if (percent <= 0.05D) return ChatColor.RED;
        else if (percent <= 0.15D) return ChatColor.GOLD;
        else if (percent <= 0.25D) return ChatColor.YELLOW;
        else return ChatColor.WHITE;
    }

}
