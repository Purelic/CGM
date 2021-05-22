package net.purelic.cgm.core.rewards;


import net.md_5.bungee.api.ChatColor;

public enum MedalType {

    KILLSTREAK(ChatColor.GREEN),
    MULTI_KILL(ChatColor.GOLD),
    STYLE(ChatColor.AQUA),
    OBJECTIVE(ChatColor.YELLOW);

    private final ChatColor color;

    MedalType(ChatColor color) {
        this.color = color;
    }

    public ChatColor getColor() {
        return color;
    }

}
