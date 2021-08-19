package net.purelic.cgm.events.modules;

import net.purelic.cgm.core.constants.MatchState;
import net.purelic.cgm.core.gamemodes.EnumSetting;
import net.purelic.cgm.core.gamemodes.constants.TeamSize;
import net.purelic.cgm.core.gamemodes.constants.TeamType;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class ChatEvent extends Event {

    private static final HandlerList HANDLERS = new HandlerList();

    private final Player player;
    private final String message;
    private final boolean global;

    public ChatEvent(Player player, String message, boolean global) {
        this.player = player;
        this.global =
            global
                || !MatchState.isState(MatchState.PRE_GAME, MatchState.STARTING, MatchState.STARTED)
                || EnumSetting.TEAM_TYPE.is(TeamType.SOLO)
                || EnumSetting.TEAM_SIZE.is(TeamSize.SINGLES)
                || (message.startsWith("!") && message.length() > 1);
        this.message = this.global && message.startsWith("!") ? message.replaceFirst("!", "") : message;
    }

    public Player getPlayer() {
        return this.player;
    }

    public String getMessage() {
        return this.message;
    }

    public boolean isGlobal() {
        return this.global;
    }

    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

}
