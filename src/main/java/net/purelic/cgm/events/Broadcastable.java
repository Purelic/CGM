package net.purelic.cgm.events;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.purelic.cgm.core.constants.MatchTeam;
import net.purelic.cgm.utils.SoundUtils;
import net.purelic.commons.utils.ChatUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.Set;

public interface Broadcastable {

    default SoundUtils.SFX getSFX() {
        return SoundUtils.SFX.DEFAULT_SFX;
    }

    default void broadcast() {
        ChatUtils.sendMessageAll(this.getBroadcastMessage());
        if (getSFX() != null) this.getSFX().playAll();
    }

    default void broadcast(MatchTeam team) {
        BaseComponent[] message = ArrayUtils.addAll(this.getPrefix(team), this.getBroadcastMessage());
        this.getAudience(team).forEach(player -> player.sendMessage(message));
        if (getSFX() != null) this.getSFX().play(team);
    }

    default BaseComponent[] getPrefix(MatchTeam team) {
        return new ComponentBuilder("[").color(ChatColor.DARK_GRAY)
                .append(team.getName()).color(team.getColor())
                .append("]").color(ChatColor.DARK_GRAY)
                .append(" ").reset()
                .create();
    }

    default Set<Player> getAudience(MatchTeam team) {
        Set<Player> players = new HashSet<>(team.getPlayers());
        players.addAll(MatchTeam.OBS.getPlayers());
        return players;
    }

    BaseComponent[] getBroadcastMessage();

}
