package net.purelic.cgm.utils;

public class PreferenceUtils {

    public static int slotToIndex(Object slot) {
        return (slot instanceof Integer ? (int) slot : ((Long) slot).intValue()) - 1;
    }

}
