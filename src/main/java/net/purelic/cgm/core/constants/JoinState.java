package net.purelic.cgm.core.constants;

import java.util.Arrays;
import java.util.HashSet;

public enum JoinState {

    LOCKED,
    PARTY_PRIORITY,
    EVERYONE;

    private static JoinState state = JoinState.LOCKED;

    public static void setState(JoinState state) {
        JoinState.state = state;
    }

    public static boolean isState(JoinState... states) {
        return (new HashSet<>(Arrays.asList(states))).contains(state);
    }

}
