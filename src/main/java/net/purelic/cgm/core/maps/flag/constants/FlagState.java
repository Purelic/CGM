package net.purelic.cgm.core.maps.flag.constants;

import net.purelic.cgm.core.maps.flag.Flag;
import net.purelic.cgm.core.maps.flag.events.*;
import net.purelic.cgm.utils.FlagUtils;
import net.purelic.commons.Commons;
import org.bukkit.event.Event;

import java.lang.reflect.Constructor;

public enum FlagState {

    RETURNED(FlagUtils.FLAG_ICON, FlagReturnEvent.class), // ⚑
    TAKEN("\u2794", FlagTakenEvent.class), // ➔
    DROPPED(FlagUtils.FLAG_ICON, FlagDropEvent.class), // ⚑
    RESPAWNING("\u2690", FlagRespawnEvent.class), // ⚐
    CAPTURED("\u2690", FlagCaptureEvent.class), // ⚐
    ;

    private final String symbol;
    private final Class<? extends FlagEvent> event;

    FlagState(String symbol, Class<? extends FlagEvent> event) {
        this.symbol = symbol;
        this.event = event;
    }

    public String getSymbol() {
        return this.symbol;
    }

    public void callEvent(Flag flag) {
        try {
            Constructor<?> constructor = this.event.getConstructor(Flag.class);
            Event event = (Event) constructor.newInstance(flag);
            Commons.callEvent(event);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
