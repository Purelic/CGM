package net.purelic.cgm.core.maps.flag.events;

import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.purelic.cgm.core.constants.MatchTeam;
import net.purelic.cgm.core.maps.flag.Flag;
import net.purelic.cgm.events.Broadcastable;
import org.bukkit.ChatColor;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import java.util.Set;

public class FlagsCollectedEvent extends Event implements Broadcastable {

    private static final HandlerList HANDLERS = new HandlerList();

    private final MatchTeam team;
    private final Set<Flag> flags;

    public FlagsCollectedEvent(MatchTeam team, Set<Flag> flags) {
        this.team = team;
        this.flags = flags;
    }

    public MatchTeam getTeam() {
        return this.team;
    }

    public Set<Flag> getFlags() {
        return this.flags;
    }

    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

    @Override
    public BaseComponent[] getBroadcastMessage() {
        int collected = this.flags.size();
        return new ComponentBuilder(FlagEvent.BROADCAST_PREFIX + ChatColor.AQUA + collected + ChatColor.RESET + " flag" + (collected == 1 ? " was" : "s were") + " collected by " + this.team.getColoredName() + "!").create();
    }

}
