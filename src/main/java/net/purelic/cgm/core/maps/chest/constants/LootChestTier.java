package net.purelic.cgm.core.maps.chest.constants;

import java.util.Random;

public enum LootChestTier {

    TIER_1(11, LootItemLevel.LEVEL_3, LootItemLevel.LEVEL_9, LootItemLevel.LEVEL_10, LootItemLevel.LEVEL_11, LootItemLevel.LEVEL_12),
    TIER_2(10, LootItemLevel.LEVEL_1, LootItemLevel.LEVEL_2, LootItemLevel.LEVEL_3, LootItemLevel.LEVEL_6, LootItemLevel.LEVEL_7, LootItemLevel.LEVEL_8),
    TIER_3(8, LootItemLevel.LEVEL_1, LootItemLevel.LEVEL_2, LootItemLevel.LEVEL_3, LootItemLevel.LEVEL_4, LootItemLevel.LEVEL_5),
    ;

    private final int totalItemValue;
    private final LootItemLevel[] levels;

    LootChestTier(int totalItemValue, LootItemLevel... levels) {
        this.totalItemValue = totalItemValue;
        this.levels = levels;
    }

    public int getTotalItemValue() {
        return this.totalItemValue;
    }

    public LootItemLevel[] getLevels() {
        return this.levels;
    }

    public LootItemLevel getRandomLevel() {
        return this.levels[new Random().nextInt(this.levels.length)];
    }

}
