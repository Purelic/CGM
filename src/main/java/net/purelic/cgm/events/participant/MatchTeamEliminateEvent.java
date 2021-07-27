package net.purelic.cgm.events.participant;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.purelic.cgm.core.constants.MatchTeam;
import net.purelic.cgm.events.Broadcastable;
import net.purelic.cgm.utils.SoundUtils;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class MatchTeamEliminateEvent extends Event implements Broadcastable {

    private static final HandlerList HANDLERS = new HandlerList();

    private final MatchTeam team;

    public MatchTeamEliminateEvent(MatchTeam team) {
        this.team = team;
    }

    public MatchTeam getTeam() {
        return this.team;
    }

    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

    @Override
    public SoundUtils.SFX getSFX() {
        return SoundUtils.SFX.TEAM_ELIMINATED;
    }

    @Override
    public BaseComponent[] getBroadcastMessage() {
        return new ComponentBuilder(" TEAM ELIMINATED » ")
            .color(ChatColor.RED)
            .bold(true)
            .append(this.team.getName())
            .color(this.team.getColor())
            .append(" has been eliminated!")
            .reset()
            .create();
    }

}
