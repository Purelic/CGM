package net.purelic.cgm.core.maps.flag.events;

import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.purelic.cgm.core.maps.flag.Flag;
import net.purelic.cgm.events.Broadcastable;
import net.purelic.cgm.utils.SoundUtils;
import net.purelic.commons.utils.NickUtils;
import org.bukkit.event.HandlerList;

public class FlagTakenEvent extends FlagEvent implements Broadcastable {

    private static final HandlerList HANDLERS = new HandlerList();

    public FlagTakenEvent(Flag flag) {
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
        return new ComponentBuilder(FlagEvent.BROADCAST_PREFIX + this.flag.getColoredName() + " was taken by " + NickUtils.getDisplayName(this.flag.getCarrier().getPlayer()) + "!").create();
    }

    @Override
    public SoundUtils.SFX getSFX() {
        return null;
    }

}
