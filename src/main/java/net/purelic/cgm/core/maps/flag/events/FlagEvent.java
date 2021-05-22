package net.purelic.cgm.core.maps.flag.events;

import net.purelic.cgm.core.maps.flag.Flag;
import net.purelic.cgm.core.maps.flag.constants.FlagState;
import org.bukkit.event.Event;

public abstract class FlagEvent extends Event {

    public static final String BROADCAST_PREFIX = " " + FlagState.RETURNED.getSymbol() + " ";

    public final Flag flag;

    public FlagEvent(Flag flag) {
        this.flag = flag;
    }

}
