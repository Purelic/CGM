package net.purelic.cgm.core.maps.hill.events;

import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.purelic.cgm.core.constants.MatchTeam;
import net.purelic.cgm.core.maps.hill.Hill;
import net.purelic.cgm.events.Broadcastable;
import net.purelic.cgm.utils.SoundUtils;
import org.bukkit.event.HandlerList;

public class HillReclaimedEvent extends HillEvent implements Broadcastable {

    private static final HandlerList HANDLERS = new HandlerList();

    public HillReclaimedEvent(Hill hill) {
        super(hill);
    }

    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

    @Override
    public SoundUtils.SFX getSFX() {
        return SoundUtils.SFX.HILL_RECLAIMED;
    }

    @Override
    public BaseComponent[] getBroadcastMessage() {
        return new ComponentBuilder(
            HillEvent.BROADCAST_PREFIX + this.hill.getName() + " has been reclaimed!"
        ).create();
    }

}
