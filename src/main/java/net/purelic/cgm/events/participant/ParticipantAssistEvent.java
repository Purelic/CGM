package net.purelic.cgm.events.participant;

/******************************************************************************
 * Copyright (c) 2016.  Written by Devon "Turqmelon": http://turqmelon.com    *
 * For more information, see LICENSE.TXT.                                     *
 ******************************************************************************/

import net.purelic.cgm.core.damage.KillAssist;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class ParticipantAssistEvent extends Event implements Cancellable {

    private static final HandlerList handlers = new HandlerList();

    private final KillAssist assist;
    private final Player killed;
    private boolean cancelled = false;

    public ParticipantAssistEvent(KillAssist assist, Player killed) {
        this.assist = assist;
        this.killed = killed;
    }

    public KillAssist getAssist() {
        return assist;
    }

    public Player getKilled() {
        return killed;
    }

    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean b) {
        this.cancelled = b;
    }

}