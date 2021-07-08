package net.purelic.cgm.core.maps.hill.events;

import net.purelic.cgm.core.maps.hill.Hill;
import org.bukkit.event.Event;

public abstract class HillEvent extends Event {

    public static final String BROADCAST_PREFIX = " ⦿ ";

    public final Hill hill;

    public HillEvent(Hill hill) {
        this.hill = hill;
    }

    public Hill getHill() {
        return this.hill;
    }

}
