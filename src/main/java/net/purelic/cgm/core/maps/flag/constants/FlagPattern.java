package net.purelic.cgm.core.maps.flag.constants;

import org.bukkit.DyeColor;
import org.bukkit.block.banner.Pattern;
import org.bukkit.block.banner.PatternType;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

public enum FlagPattern {

    POKEBALL("Pokéball", true,
        new Pattern(DyeColor.WHITE, PatternType.BASE),
        new Pattern(DyeColor.RED, PatternType.HALF_HORIZONTAL),
        new Pattern(DyeColor.RED, PatternType.HALF_HORIZONTAL),
        new Pattern(DyeColor.BLACK, PatternType.RHOMBUS_MIDDLE),
        new Pattern(DyeColor.RED, PatternType.STRIPE_TOP),
        new Pattern(DyeColor.WHITE, PatternType.STRIPE_BOTTOM),
        new Pattern(DyeColor.BLACK, PatternType.STRIPE_MIDDLE),
        new Pattern(DyeColor.WHITE, PatternType.CIRCLE_MIDDLE)
    ),

    ZOMBIE("Zombie", false,
        new Pattern(DyeColor.BLACK, PatternType.BASE),
        new Pattern(DyeColor.GREEN, PatternType.SKULL),
        new Pattern(DyeColor.BLUE, PatternType.HALF_HORIZONTAL_MIRROR),
        new Pattern(DyeColor.LIGHT_BLUE, PatternType.CREEPER),
        new Pattern(DyeColor.BLACK, PatternType.STRIPE_LEFT),
        new Pattern(DyeColor.BLACK, PatternType.STRIPE_RIGHT),
        new Pattern(DyeColor.BLACK, PatternType.GRADIENT)
    ),

    LIGHTSABER("Lightsaber", false,
        new Pattern(DyeColor.WHITE, PatternType.BASE),
        new Pattern(DyeColor.PURPLE, PatternType.STRIPE_SMALL),
        new Pattern(DyeColor.BLACK, PatternType.HALF_VERTICAL),
        new Pattern(DyeColor.BLACK, PatternType.HALF_VERTICAL_MIRROR),
        new Pattern(DyeColor.BLACK, PatternType.SQUARE_BOTTOM_LEFT),
        new Pattern(DyeColor.BLACK, PatternType.SQUARE_BOTTOM_RIGHT),
        new Pattern(DyeColor.BLACK, PatternType.TRIANGLES_BOTTOM)
    ),

    CAMERA("Camera", false,
        new Pattern(DyeColor.BLACK, PatternType.BASE),
        new Pattern(DyeColor.WHITE, PatternType.SQUARE_TOP_LEFT),
        new Pattern(DyeColor.SILVER, PatternType.SKULL),
        new Pattern(DyeColor.SILVER, PatternType.CIRCLE_MIDDLE),
        new Pattern(DyeColor.WHITE, PatternType.HALF_HORIZONTAL),
        new Pattern(DyeColor.BLACK, PatternType.RHOMBUS_MIDDLE),
        new Pattern(DyeColor.WHITE, PatternType.HALF_HORIZONTAL_MIRROR)
    ),
    ;

    private final String name;
    private final boolean premium;
    private final List<Pattern> patterns;

    FlagPattern(String name, boolean premium, Pattern... patterns) {
        this.name = name;
        this.premium = premium;
        this.patterns = Arrays.asList(patterns);
    }

    public String getName() {
        return this.name;
    }

    public boolean isPremium() {
        return this.premium;
    }

    public List<Pattern> getPatterns() {
        return this.patterns;
    }

    public static FlagPattern random() {
        return values()[new Random().nextInt(values().length)];
    }

}
