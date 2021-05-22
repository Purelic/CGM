package net.purelic.cgm.core.sfx;

import org.bukkit.Sound;

public enum SFX2 {

    // Goals:
    // Custom sound file (string/path) IF server resource pack is downloaded
    // Fallback vanilla sound (can be from the sound builder)
    // Should also consider if sounds were a cosmetic - could we pull a value from a player
    // Can also use another SFX as a fallback
    // how could we make some of them per team? team + sfx enum name?

    TEST,
    ;

    private static final Sound FALLBACK = Sound.ORB_PICKUP;

    SFX2() {
        // default/todo
    }

    SFX2(String sound, Sound fallback, boolean cosmetic) {
        // if it's cosmetic use the sound string to get it from their preferences
        // if it's not a cosmetic get it from the custom resource pack
        // if they aren't using the pack, use the fallback
    }

}
