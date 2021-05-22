package net.purelic.cgm.listeners.modules;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.purelic.cgm.core.constants.MatchTeam;
import net.purelic.cgm.events.modules.ChatEvent;
import net.purelic.commons.Commons;
import net.purelic.commons.utils.ChatUtils;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class TeamChatModule implements Listener {

    @EventHandler
    public void onChat(ChatEvent event) {
        Player player = event.getPlayer();
        MatchTeam team = MatchTeam.getTeam(player);
        boolean global = event.isGlobal();

        // staff in obs

        if (!global) {
            ComponentBuilder prefix = new ComponentBuilder("")
                .event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(team.getName() + " Team Chat").color(team.getColor()).create()))
                .append("[").color(ChatColor.DARK_GRAY)
                .append(team.getName()).color(team.getColor())
                .append("]").color(ChatColor.DARK_GRAY)
                .append(" ").reset();

            Set<Player> players = new HashSet<>(team.getPlayers());
            players.addAll(this.getStaffObs());
            ChatUtils.sendFancyChatMessage(player, event.getMessage(), prefix, players);
        } else {
            ChatUtils.sendFancyChatMessage(player, event.getMessage());
        }
    }

    private Set<Player> getStaffObs() {
        return MatchTeam.OBS.getPlayers()
            .stream().filter(player -> Commons.getProfile(player).isStaff())
            .collect(Collectors.toSet());
    }

}
