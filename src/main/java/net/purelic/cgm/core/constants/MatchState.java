package net.purelic.cgm.core.constants;

import net.md_5.bungee.api.ChatColor;
import net.purelic.cgm.events.match.MatchStateChangeEvent;
import net.purelic.commons.Commons;

import java.util.Arrays;

public enum MatchState {

    WAITING("Waiting", ChatColor.WHITE),
    VOTING("Voting", ChatColor.GREEN),
    PRE_GAME("Pre-Game", ChatColor.GREEN),
    STARTING("Starting", ChatColor.GREEN),
    STARTED("Started", ChatColor.YELLOW),
    ENDED("Ended", ChatColor.GREEN),
    RESTARTING("Restarting", ChatColor.RED);

    private static MatchState state;
    private final String title;
    private final ChatColor color;

    MatchState(String title, ChatColor color) {
        this.title = title;
        this.color = color;
    }

    public static MatchState getState() {
        return state;
    }

    public static void setState(MatchState state) {
        MatchState.setState(state, false, 15);
    }

    public static void setState(MatchState state, boolean forced) {
        MatchState.setState(state, forced, 15);
    }

    public static void setState(MatchState state, boolean forced, int seconds) {
        MatchState current = MatchState.state;
        MatchState.state = state;
        Commons.callEvent(new MatchStateChangeEvent(current, state, forced, seconds));
    }

    public String getTitle() {
        return this.title;
    }

    @Override
    public String toString() {
        return this.color + this.title;
    }

    public static boolean isState(MatchState... states) {
        return Arrays.asList(states).contains(state);
    }

    public static boolean isActive() {
        return MatchState.isState(MatchState.PRE_GAME, MatchState.STARTING, MatchState.STARTED);
    }

}
