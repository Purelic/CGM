package net.purelic.cgm.match.countdowns;

import net.md_5.bungee.api.ChatColor;
import net.purelic.commons.utils.ChatUtils;
import org.bukkit.Bukkit;

public class RestartCountdown extends Countdown {

    public RestartCountdown() {
        super(15, "Restarting server");
        ChatUtils.broadcastTitle("", ChatColor.AQUA + "Server Restarting...", 100);
    }

    @Override
    public void complete() {
        Bukkit.getServer().shutdown();
    }

}
