package net.purelic.cgm.listeners;

import net.purelic.cgm.CGM;
import net.purelic.cgm.core.constants.MatchState;
import net.purelic.cgm.core.managers.MatchManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.ServerListPingEvent;

public class ServerListPing implements Listener {

    private final String header = ChatColor.AQUA + "" + ChatColor.STRIKETHROUGH +
            "               " + ChatColor.WHITE + ChatColor.BOLD + " PuRelic" +
            ChatColor.RESET + ChatColor.GRAY + " [1.7-1.16] " +
            ChatColor.AQUA + ChatColor.STRIKETHROUGH + "               \n" + ChatColor.RESET;

//    private final String header = ChatColor.WHITE + "" + ChatColor.BOLD +
//            "PuRelic Network " + ChatColor.RESET + ChatColor.GRAY + "[1.7 - 1.16]\n";

    @EventHandler
    public void onServerListPing(ServerListPingEvent event) {
        if (Bukkit.hasWhitelist()) {
            event.setMotd(this.header + ChatColor.RED + "Server whitelisted for testing!");
            event.setMaxPlayers(0);
            return;
        }

        if (!CGM.isReady()) {
            event.setMotd(this.header + ChatColor.RED + "Server starting up...");
            event.setMaxPlayers(0);
            return;
        }

        String motd = this.header + ChatColor.GRAY + " » " + MatchState.getState().toString() + ChatColor.GRAY + " « ";

        if (MatchState.isActive() || MatchState.isState(MatchState.ENDED)) {
            MatchManager matchManager = CGM.get().getMatchManager();
            motd += matchManager.getCurrentGameMode().getColoredName() + ChatColor.WHITE + " on " + matchManager.getCurrentMap().getColoredName();
        }

        event.setMotd(motd);
        event.setMaxPlayers(20);
    }

}
