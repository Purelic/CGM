package net.purelic.cgm.core.maps.flag.events;

import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.purelic.cgm.core.maps.flag.Flag;
import net.purelic.cgm.events.Broadcastable;
import org.bukkit.event.HandlerList;

public class FlagReturnEvent extends FlagEvent implements Broadcastable {

    private static final HandlerList HANDLERS = new HandlerList();

    public FlagReturnEvent(Flag flag) {
        super(flag);
    }

    public Flag getFlag() {
        return this.flag;
    }

    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

    @Override
    public BaseComponent[] getBroadcastMessage() {
        return new ComponentBuilder(FlagEvent.BROADCAST_PREFIX + this.flag.getColoredName() + " has been returned!").create();
    }

}
