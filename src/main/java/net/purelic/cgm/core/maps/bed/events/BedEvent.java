package net.purelic.cgm.core.maps.bed.events;

import net.purelic.cgm.core.maps.bed.Bed;
import org.bukkit.event.Event;

public abstract class BedEvent extends Event {

    public static final String SYMBOL_BED_COMPLETE = "\u2b1c"; // ⬜
    public static final String SYMBOL_BED_INCOMPLETE = "\u2b1b"; // ⬛
    public static final String BROADCAST_PREFIX = " " + SYMBOL_BED_INCOMPLETE + " ";

    public final Bed bed;

    public BedEvent(Bed bed) {
        this.bed = bed;
    }

}
