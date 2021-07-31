package net.purelic.cgm.core.rewards;

import net.md_5.bungee.api.ChatColor;
import org.apache.commons.lang.WordUtils;

public enum Medal {

    AFTERLIFE(MedalType.STYLE, "Kill an enemy from the respawn screen"),
    CLOSE_CALL(MedalType.STYLE, "Kill an enemy while on low heath"),
    DECAKILL(MedalType.MULTI_KILL, "Get a 10 kill multikill"),
    DOMINATING(MedalType.KILLSTREAK, "Get a 15 killstreak"),
    DOUBLE_KILL(MedalType.MULTI_KILL, "Get a double kill"),
    ENNEAKILL(MedalType.MULTI_KILL, "Get a 9 kill multikill"),
    FIRST_BLOOD(MedalType.STYLE, "Get the first kill of the match"),
    GAME_BREAKER(MedalType.MULTI_KILL, "Break the game"),
    GODLIKE(MedalType.KILLSTREAK, "Get a 25 killstreak"),
    HEPTAKILL(MedalType.MULTI_KILL, "Get a 7 kill multikill"),
    HEXAKILL(MedalType.MULTI_KILL, "Get a 6 kill multikill"),
    KILLING_SPREE(MedalType.KILLSTREAK, "Get a 5 killstreak"),
    LONG_SHOT(MedalType.STYLE, "Get a bow kill from at least 35 blocks away"),
    MAKE_IT_COUNT(MedalType.STYLE, "Get a kill with your final arrow"),
    MASSACRE(MedalType.KILLSTREAK, "Get a 30+ killstreak"),
    OCTOKILL(MedalType.MULTI_KILL, "Get an 8 kill multikill"),
    PENTAKILL(MedalType.MULTI_KILL, "Get a 5 kill multikill"),
    PRECISION_SHOT(MedalType.STYLE, "Kill an enemy that's in mid air with your bow"),
    MLG(MedalType.STYLE, "MLG", "Kill an enemy with your bow while you're in mid air"),
    RAMPAGE(MedalType.KILLSTREAK, "Get a 10 killstreak"),
    STREAK_STRIKER(MedalType.STYLE, "Kill an enemy that's currently on a killstreak"),
    BUZZ_KILL(MedalType.STYLE, "Kill an enemy that's one kill away from a killstreak"),
    TETRAKILL(MedalType.MULTI_KILL, "Get a 4 kill multikill"),
    TRIPLE_KILL(MedalType.MULTI_KILL, "Get a triple kill"),
    UNSTOPPABLE(MedalType.KILLSTREAK, "Get a 20 kill streak"),
    CLUTCH_KILL(MedalType.STYLE, "Kill an enemy with less than 15 seconds left "),
    QUICK_KILL(MedalType.STYLE, "Kill an enemy within 20 seconds"),
    WATCH_DOG(MedalType.OBJECTIVE, "Defend your hill from an enemy from afar"),
    CONQUEROR(MedalType.OBJECTIVE, "Kill an enemy while holding a hill"),
    DEFENDER(MedalType.OBJECTIVE, "Defend your hill from an enemy"),
    TAKEDOWN(MedalType.OBJECTIVE, "Kill an enemy capturing a hill from afar"),
    HEAD_STOLEN(MedalType.OBJECTIVE, "Steal an enemy head"),
    HEAD_RECOVERED(MedalType.OBJECTIVE, "Recover your own head"),
    BOUNTY_HUNTER(MedalType.OBJECTIVE, "Collect 5 or more heads at once"),
    HEAD_MASTER(MedalType.OBJECTIVE, "Collect 15 or more heads at once"),
    SO_NO_HEAD(MedalType.STYLE, "So No Head?", "Kill an enemy carrying 5 or more heads"),
    GREAT_BALLS_OF_FIRE(MedalType.STYLE, "Great Balls of Fire", "Kill a player by directly hitting them with a fireball"),
    PYROMANIAC(MedalType.STYLE, "Kill a player with TNT"),

    // Capture the Flag
    CARRIER_KILL(MedalType.OBJECTIVE, "Kill an enemy that's carrying a flag"),
    CARRIER_SNIPE(MedalType.OBJECTIVE, "Shoot an enemy that's carrying a flag"),
    FLAG_KILL(MedalType.OBJECTIVE, "Kill an enemy while carrying a flag"),
    FLAG_SNIPE(MedalType.OBJECTIVE, "Shoot an enemy while carrying a flag"),
    FLAG_JOUST(MedalType.STYLE, "Kill an enemy flag carrier while also carrying a flag"),
    CLOSE_CAPTURE(MedalType.STYLE, "Capture a flag while on low heath"),
    CLOSE_DROP(MedalType.STYLE, "Kill an enemy while carrying a flag on low heath"),
    FLAG_DEFENSE(MedalType.STYLE, "Kill an enemy that's close to your flag"),
    STOPPED_SHORT(MedalType.STYLE, "Kill an enemy carrier that's close to collecting your flag"),

    // UHC
    DRAGON_TAMER(MedalType.OBJECTIVE, "Kill the Ender Dragon during a UHC match")
    ;

    private final String medalId;
    private final String name;
    private final MedalType medalType;
    private final String description;
    private final ChatColor color;

    Medal(MedalType medalType, String description) {
        this.medalId = this.name().toLowerCase().replaceAll("_", "-");
        this.name = WordUtils.capitalizeFully(this.name().replaceAll("_", " "));
        this.medalType = medalType;
        this.description = description;
        this.color = medalType.getColor();
    }

    Medal(MedalType medalType, String name, String description) {
        this.medalId = this.name().toLowerCase().replaceAll("_", "-");
        this.name = name;
        this.medalType = medalType;
        this.description = description;
        this.color = medalType.getColor();
    }

    public String getMedalId() {
        return this.medalId;
    }

    public String getName() {
        return this.name;
    }

    public ChatColor getColor() {
        return this.color;
    }

    public MedalType getMedalType() {
        return this.medalType;
    }

    public String getFancyName(boolean appendComma) {
        return (appendComma ? ChatColor.RESET + ", " : "") + this.color + this.name;
    }

    public String getDescription() {
        return this.description;
    }

}
