package net.purelic.cgm.core.maps.chest.constants;

public enum LootItemLevel {

    LEVEL_1(1), // blocks
    LEVEL_2(1), // crafting (sticks, flint, feathers, diamond, iron)
    LEVEL_3(1), // items (food, projectiles, exp, buckets)
    LEVEL_4(2), // armor (leather, chain, gold)
    LEVEL_5(2), // weapons (stone, wood, gold) / tools (stone)
    LEVEL_6(2), // armor (chain, iron)
    LEVEL_7(3), // weapons (iron) / tools (iron)
    LEVEL_8(3), // explosives (tnt, redstone, f&s)
    LEVEL_9(2), // better misc items (blocks, diamond, iron, arrows, tnt, f&s, redstone)
    LEVEL_10(3), // armor (iron, diamond)
    LEVEL_11(3), // weapons (iron) / tools (diamond)
    LEVEL_12(4), // rare (pearl, gapple, exp)
    ;

    private final int itemValue;

    LootItemLevel(int itemValue) {
        this.itemValue = itemValue;
    }

    public int getItemValue() {
        return this.itemValue;
    }

}
