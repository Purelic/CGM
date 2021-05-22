package net.purelic.cgm.utils;

import net.md_5.bungee.api.ChatColor;
import net.purelic.cgm.core.constants.MatchTeam;
import org.bukkit.Color;
import org.bukkit.DyeColor;
import org.bukkit.entity.Player;

public class ColorConverter {

    public static Color convert(ChatColor chatColor) {
        switch (chatColor) {
            case AQUA:
                return Color.AQUA;
            case BLACK:
                return Color.BLACK;
            case BLUE:
                return Color.BLUE;
            case DARK_AQUA:
                return Color.TEAL;
            case DARK_BLUE:
                return Color.NAVY;
            case DARK_GRAY:
            case GRAY: // prev silver
                return Color.GRAY;
            case DARK_GREEN:
                return Color.GREEN;
            case DARK_PURPLE:
                return Color.PURPLE;
            case DARK_RED:
                return Color.MAROON;
            case GOLD:
                return Color.ORANGE;
            case GREEN:
                return Color.LIME;
            case LIGHT_PURPLE:
                return Color.FUCHSIA;
            case RED:
                return Color.RED;
            case WHITE:
                return Color.WHITE;
            case YELLOW:
                return Color.YELLOW;
        }
        return Color.WHITE;
    }

    public static ChatColor darken(ChatColor color) {
        switch (color) {
            case AQUA:
                return ChatColor.DARK_AQUA;
            case BLUE:
                return ChatColor.DARK_BLUE;
            case GRAY:
                return ChatColor.DARK_GRAY;
            case GREEN:
                return ChatColor.DARK_GREEN;
            case LIGHT_PURPLE:
                return ChatColor.DARK_PURPLE;
            case RED:
                return ChatColor.DARK_RED;
            case WHITE:
                return ChatColor.GRAY;
            case YELLOW:
                return ChatColor.GOLD;
        }
        return ChatColor.GRAY;
    }

    public static DyeColor getDyeColor(Player player) {
        return getDyeColor(MatchTeam.getTeam(player));
    }

    public static DyeColor getDyeColor(MatchTeam team) {
        return getDyeColor(team.getColor());
    }

    public static DyeColor getDyeColor(ChatColor color) {
        switch (color) {
            case YELLOW:
                return DyeColor.YELLOW;
            case RED:
                return DyeColor.RED;
            case BLUE:
                return DyeColor.BLUE;
            case GREEN:
                return DyeColor.LIME;
            case WHITE:
                return DyeColor.WHITE;
            case GRAY:
                return DyeColor.GRAY;
            case AQUA:
                return DyeColor.LIGHT_BLUE;
            case LIGHT_PURPLE:
                return DyeColor.MAGENTA;
            case BLACK:
                return DyeColor.BLACK;
        }
        return DyeColor.WHITE;
    }

}
