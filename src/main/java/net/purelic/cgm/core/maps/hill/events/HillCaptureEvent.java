package net.purelic.cgm.core.maps.hill.events;

import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.purelic.cgm.core.constants.MatchTeam;
import net.purelic.cgm.core.maps.flag.Flag;
import net.purelic.cgm.core.maps.flag.events.FlagEvent;
import net.purelic.cgm.core.maps.hill.Hill;
import net.purelic.cgm.events.Broadcastable;
import net.purelic.cgm.utils.SoundUtils;
import net.purelic.commons.utils.NickUtils;
import org.bukkit.event.HandlerList;

public class HillCaptureEvent extends HillEvent implements Broadcastable {

    private static final HandlerList HANDLERS = new HandlerList();

    private final MatchTeam team;

    public HillCaptureEvent(Hill hill, MatchTeam team) {
        super(hill);
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
    public BaseComponent[] getBroadcastMessage() {
        return new ComponentBuilder(
            HillEvent.BROADCAST_PREFIX + this.hill.getName() + " was captured by " +
                this.team.getColoredName() + "!"
        ).create();
    }

}
