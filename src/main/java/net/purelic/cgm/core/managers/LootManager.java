package net.purelic.cgm.core.managers;

import net.purelic.cgm.core.gamemodes.constants.LootType;
import net.purelic.cgm.core.maps.chest.LootChestItem;
import net.purelic.cgm.core.maps.chest.constants.LootItemLevel;
import net.purelic.commons.utils.ItemCrafter;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class LootManager {

    private static Map<LootType, Map<LootItemLevel, List<LootChestItem>>> items = new HashMap<>();

    public static void setLootItems() {
        List<LootChestItem> swNormalItems = Arrays.asList(
            new LootChestItem(LootItemLevel.LEVEL_1, Material.DIRT, 12),
            new LootChestItem(LootItemLevel.LEVEL_1, Material.STONE, 8),
            new LootChestItem(LootItemLevel.LEVEL_1, Material.STONE, 16),
            new LootChestItem(LootItemLevel.LEVEL_1, Material.WOOD, 12),
            new LootChestItem(LootItemLevel.LEVEL_1, Material.WOOD, 32),
            new LootChestItem(LootItemLevel.LEVEL_1, Material.LOG, 8),
            new LootChestItem(LootItemLevel.LEVEL_2, Material.STICK, 4),
            new LootChestItem(LootItemLevel.LEVEL_2, Material.FEATHER, 4),
            new LootChestItem(LootItemLevel.LEVEL_2, Material.FLINT, 4),
            new LootChestItem(LootItemLevel.LEVEL_2, Material.DIAMOND, 1),
            new LootChestItem(LootItemLevel.LEVEL_2, Material.DIAMOND, 2),
            new LootChestItem(LootItemLevel.LEVEL_2, Material.IRON_INGOT, 2),
            new LootChestItem(LootItemLevel.LEVEL_2, Material.IRON_INGOT, 4),
            new LootChestItem(LootItemLevel.LEVEL_2, Material.IRON_INGOT, 5),
            new LootChestItem(LootItemLevel.LEVEL_2, Material.IRON_INGOT, 8),
            new LootChestItem(LootItemLevel.LEVEL_2, Material.STONE_PICKAXE),
            new LootChestItem(LootItemLevel.LEVEL_2, Material.STONE_AXE),
            new LootChestItem(LootItemLevel.LEVEL_2, Material.GOLD_PICKAXE),
            new LootChestItem(LootItemLevel.LEVEL_2, Material.GOLD_AXE),
            new LootChestItem(LootItemLevel.LEVEL_3, Material.APPLE, 6),
            new LootChestItem(LootItemLevel.LEVEL_3, Material.COOKED_BEEF, 2),
            new LootChestItem(LootItemLevel.LEVEL_3, Material.COOKED_BEEF, 4),
            new LootChestItem(LootItemLevel.LEVEL_3, Material.COOKED_BEEF, 8),
            new LootChestItem(LootItemLevel.LEVEL_3, Material.WATER_BUCKET),
            new LootChestItem(LootItemLevel.LEVEL_3, Material.LAVA_BUCKET),
            new LootChestItem(LootItemLevel.LEVEL_3, Material.BUCKET),
            new LootChestItem(LootItemLevel.LEVEL_3, Material.SNOW_BALL, 8),
            new LootChestItem(LootItemLevel.LEVEL_3, Material.EXP_BOTTLE, 8),
            new LootChestItem(LootItemLevel.LEVEL_4, Material.LEATHER_HELMET),
            new LootChestItem(LootItemLevel.LEVEL_4, Material.LEATHER_CHESTPLATE),
            new LootChestItem(LootItemLevel.LEVEL_4, Material.LEATHER_LEGGINGS),
            new LootChestItem(LootItemLevel.LEVEL_4, Material.LEATHER_BOOTS),
            new LootChestItem(LootItemLevel.LEVEL_4, Material.CHAINMAIL_HELMET),
            new LootChestItem(LootItemLevel.LEVEL_4, Material.CHAINMAIL_CHESTPLATE),
            new LootChestItem(LootItemLevel.LEVEL_4, Material.CHAINMAIL_LEGGINGS),
            new LootChestItem(LootItemLevel.LEVEL_4, Material.CHAINMAIL_BOOTS),
            new LootChestItem(LootItemLevel.LEVEL_4, Material.GOLD_HELMET),
            new LootChestItem(LootItemLevel.LEVEL_4, Material.GOLD_CHESTPLATE),
            new LootChestItem(LootItemLevel.LEVEL_4, Material.GOLD_LEGGINGS),
            new LootChestItem(LootItemLevel.LEVEL_4, Material.GOLD_BOOTS),
            new LootChestItem(LootItemLevel.LEVEL_4, Material.IRON_HELMET),
            new LootChestItem(LootItemLevel.LEVEL_4, Material.IRON_BOOTS),
            new LootChestItem(LootItemLevel.LEVEL_5, Material.WOOD_SWORD),
            new LootChestItem(LootItemLevel.LEVEL_5, Material.STONE_SWORD),
            new LootChestItem(LootItemLevel.LEVEL_5, Material.BOW),
            new LootChestItem(LootItemLevel.LEVEL_5, Material.ARROW, 4),
            new LootChestItem(LootItemLevel.LEVEL_5, Material.ARROW, 12),
            new LootChestItem(LootItemLevel.LEVEL_5, new ItemCrafter(Material.FLINT_AND_STEEL).durability(61)),
            new LootChestItem(LootItemLevel.LEVEL_5, new ItemCrafter(Material.FISHING_ROD).durability(58)),
            new LootChestItem(LootItemLevel.LEVEL_5, new ItemCrafter(Material.STONE_SWORD).enchant(Enchantment.DAMAGE_ALL)),
            new LootChestItem(LootItemLevel.LEVEL_6, Material.IRON_AXE),
            new LootChestItem(LootItemLevel.LEVEL_6, Material.IRON_PICKAXE),
            new LootChestItem(LootItemLevel.LEVEL_6, Material.DIAMOND_AXE),
            new LootChestItem(LootItemLevel.LEVEL_6, Material.IRON_SPADE),
            new LootChestItem(LootItemLevel.LEVEL_6, Material.STONE_SWORD),
            new LootChestItem(LootItemLevel.LEVEL_7, Material.CHAINMAIL_HELMET),
            new LootChestItem(LootItemLevel.LEVEL_7, Material.CHAINMAIL_LEGGINGS),
            new LootChestItem(LootItemLevel.LEVEL_7, Material.CHAINMAIL_CHESTPLATE),
            new LootChestItem(LootItemLevel.LEVEL_7, Material.CHAINMAIL_BOOTS),
            new LootChestItem(LootItemLevel.LEVEL_7, Material.IRON_HELMET),
            new LootChestItem(LootItemLevel.LEVEL_7, Material.IRON_BOOTS),
            new LootChestItem(LootItemLevel.LEVEL_7, Material.IRON_LEGGINGS),
            new LootChestItem(LootItemLevel.LEVEL_8, Material.TNT, 2),
            new LootChestItem(LootItemLevel.LEVEL_8, Material.TNT, 4),
            new LootChestItem(LootItemLevel.LEVEL_8, Material.TNT, 8),
            new LootChestItem(LootItemLevel.LEVEL_8, Material.TNT, 12),
            new LootChestItem(LootItemLevel.LEVEL_8, Material.REDSTONE_BLOCK, 8),
            new LootChestItem(LootItemLevel.LEVEL_8, Material.REDSTONE, 8),
            new LootChestItem(LootItemLevel.LEVEL_8, Material.REDSTONE_TORCH_ON, 16),
            new LootChestItem(LootItemLevel.LEVEL_8, new ItemCrafter(Material.FLINT_AND_STEEL).durability(56)),
            new LootChestItem(LootItemLevel.LEVEL_9, new ItemCrafter(Material.FLINT_AND_STEEL).durability(56)),
            new LootChestItem(LootItemLevel.LEVEL_9, Material.TNT, 6),
            new LootChestItem(LootItemLevel.LEVEL_9, Material.DIAMOND, 2),
            new LootChestItem(LootItemLevel.LEVEL_9, Material.IRON_INGOT, 8),
            new LootChestItem(LootItemLevel.LEVEL_9, Material.REDSTONE, 8),
            new LootChestItem(LootItemLevel.LEVEL_9, Material.COMPASS),
            new LootChestItem(LootItemLevel.LEVEL_9, Material.ARROW, 12),
            new LootChestItem(LootItemLevel.LEVEL_9, Material.LOG, 32),
            new LootChestItem(LootItemLevel.LEVEL_9, Material.LOG, 16),
            new LootChestItem(LootItemLevel.LEVEL_9, Material.STONE, 48),
            new LootChestItem(LootItemLevel.LEVEL_10, new ItemCrafter(Material.IRON_CHESTPLATE).enchant(Enchantment.PROTECTION_PROJECTILE, 2)),
            new LootChestItem(LootItemLevel.LEVEL_10, new ItemCrafter(Material.IRON_LEGGINGS).enchant(Enchantment.PROTECTION_ENVIRONMENTAL, 2)),
            new LootChestItem(LootItemLevel.LEVEL_10, new ItemCrafter(Material.DIAMOND_HELMET).enchant(Enchantment.PROTECTION_EXPLOSIONS, 2)),
            new LootChestItem(LootItemLevel.LEVEL_10, new ItemCrafter(Material.DIAMOND_BOOTS).enchant(Enchantment.PROTECTION_FALL)),
            new LootChestItem(LootItemLevel.LEVEL_10, Material.IRON_CHESTPLATE),
            new LootChestItem(LootItemLevel.LEVEL_10, Material.IRON_LEGGINGS),
            new LootChestItem(LootItemLevel.LEVEL_10, Material.DIAMOND_HELMET),
            new LootChestItem(LootItemLevel.LEVEL_10, Material.DIAMOND_BOOTS),
            new LootChestItem(LootItemLevel.LEVEL_11, Material.IRON_SWORD),
            new LootChestItem(LootItemLevel.LEVEL_11, new ItemCrafter(Material.IRON_AXE).enchant(Enchantment.DAMAGE_ALL)),
            new LootChestItem(LootItemLevel.LEVEL_11, new ItemCrafter(Material.DIAMOND_AXE).enchant(Enchantment.DIG_SPEED)),
            new LootChestItem(LootItemLevel.LEVEL_11, new ItemCrafter(Material.DIAMOND_PICKAXE).enchant(Enchantment.DIG_SPEED)),
            new LootChestItem(LootItemLevel.LEVEL_11, new ItemCrafter(Material.BOW).enchant(Enchantment.ARROW_DAMAGE)),
            new LootChestItem(LootItemLevel.LEVEL_12, Material.ENDER_PEARL),
            new LootChestItem(LootItemLevel.LEVEL_12, Material.EXP_BOTTLE, 32),
            new LootChestItem(LootItemLevel.LEVEL_12, Material.GOLDEN_APPLE, 2),
            new LootChestItem(LootItemLevel.LEVEL_12, Material.ANVIL)
        );

        List<LootChestItem> swInsaneItems = Arrays.asList(
            new LootChestItem(LootItemLevel.LEVEL_1, Material.DIRT, 32),
            new LootChestItem(LootItemLevel.LEVEL_1, Material.STONE, 24),
            new LootChestItem(LootItemLevel.LEVEL_1, Material.STONE, 48),
            new LootChestItem(LootItemLevel.LEVEL_1, Material.WOOD, 24),
            new LootChestItem(LootItemLevel.LEVEL_1, Material.WOOD, 48),
            new LootChestItem(LootItemLevel.LEVEL_1, Material.LOG, 8),
            new LootChestItem(LootItemLevel.LEVEL_1, Material.LOG, 16),
            new LootChestItem(LootItemLevel.LEVEL_1, Material.ENCHANTMENT_TABLE),
            new LootChestItem(LootItemLevel.LEVEL_2, Material.STICK, 4),
            new LootChestItem(LootItemLevel.LEVEL_2, Material.FEATHER, 4),
            new LootChestItem(LootItemLevel.LEVEL_2, Material.FLINT, 4),
            new LootChestItem(LootItemLevel.LEVEL_2, Material.DIAMOND, 3),
            new LootChestItem(LootItemLevel.LEVEL_2, Material.DIAMOND, 6),
            new LootChestItem(LootItemLevel.LEVEL_2, Material.IRON_INGOT, 6),
            new LootChestItem(LootItemLevel.LEVEL_2, Material.IRON_INGOT, 10),
            new LootChestItem(LootItemLevel.LEVEL_2, Material.IRON_PICKAXE),
            new LootChestItem(LootItemLevel.LEVEL_2, Material.IRON_AXE),
            new LootChestItem(LootItemLevel.LEVEL_2, new ItemCrafter(Material.GOLD_PICKAXE).enchant(Enchantment.DIG_SPEED, 2)),
            new LootChestItem(LootItemLevel.LEVEL_2, new ItemCrafter(Material.GOLD_AXE).enchant(Enchantment.DIG_SPEED, 2)),
            new LootChestItem(LootItemLevel.LEVEL_3, Material.COOKED_BEEF, 8),
            new LootChestItem(LootItemLevel.LEVEL_3, Material.COOKED_BEEF, 16),
            new LootChestItem(LootItemLevel.LEVEL_3, Material.GOLDEN_APPLE, 2),
            new LootChestItem(LootItemLevel.LEVEL_3, Material.WATER_BUCKET),
            new LootChestItem(LootItemLevel.LEVEL_3, Material.LAVA_BUCKET),
            new LootChestItem(LootItemLevel.LEVEL_3, Material.BUCKET),
            new LootChestItem(LootItemLevel.LEVEL_3, Material.SNOW_BALL, 8),
            new LootChestItem(LootItemLevel.LEVEL_3, Material.EXP_BOTTLE, 16),
            new LootChestItem(LootItemLevel.LEVEL_3, Material.EXP_BOTTLE, 32),
            new LootChestItem(LootItemLevel.LEVEL_4, Material.IRON_HELMET),
            new LootChestItem(LootItemLevel.LEVEL_4, Material.IRON_CHESTPLATE),
            new LootChestItem(LootItemLevel.LEVEL_4, Material.IRON_LEGGINGS),
            new LootChestItem(LootItemLevel.LEVEL_4, Material.IRON_BOOTS),
            new LootChestItem(LootItemLevel.LEVEL_4, Material.DIAMOND_HELMET),
            new LootChestItem(LootItemLevel.LEVEL_4, Material.DIAMOND_CHESTPLATE),
            new LootChestItem(LootItemLevel.LEVEL_4, Material.DIAMOND_LEGGINGS),
            new LootChestItem(LootItemLevel.LEVEL_4, Material.DIAMOND_BOOTS),
            new LootChestItem(LootItemLevel.LEVEL_5, Material.ARROW, 4),
            new LootChestItem(LootItemLevel.LEVEL_5, Material.ARROW, 8),
            new LootChestItem(LootItemLevel.LEVEL_5, Material.IRON_SWORD),
            new LootChestItem(LootItemLevel.LEVEL_5, new ItemCrafter(Material.STONE_SWORD).enchant(Enchantment.DAMAGE_ALL)),
            new LootChestItem(LootItemLevel.LEVEL_5, new ItemCrafter(Material.IRON_AXE).enchant(Enchantment.DAMAGE_ALL)),
            new LootChestItem(LootItemLevel.LEVEL_5, Material.BOW),
            new LootChestItem(LootItemLevel.LEVEL_5, new ItemCrafter(Material.BOW).enchant(Enchantment.ARROW_DAMAGE)),
            new LootChestItem(LootItemLevel.LEVEL_5, new ItemCrafter(Material.FLINT_AND_STEEL).durability(61)),
            new LootChestItem(LootItemLevel.LEVEL_5, new ItemCrafter(Material.FISHING_ROD).durability(58)),
            new LootChestItem(LootItemLevel.LEVEL_6, Material.IRON_AXE),
            new LootChestItem(LootItemLevel.LEVEL_6, Material.IRON_PICKAXE),
            new LootChestItem(LootItemLevel.LEVEL_6, new ItemCrafter(Material.IRON_PICKAXE).enchant(Enchantment.DIG_SPEED, 3)),
            new LootChestItem(LootItemLevel.LEVEL_6, Material.DIAMOND_AXE),
            new LootChestItem(LootItemLevel.LEVEL_6, Material.DIAMOND_PICKAXE),
            new LootChestItem(LootItemLevel.LEVEL_6, Material.DIAMOND_SPADE),
            new LootChestItem(LootItemLevel.LEVEL_7, Material.DIAMOND_HELMET),
            new LootChestItem(LootItemLevel.LEVEL_7, Material.DIAMOND_CHESTPLATE),
            new LootChestItem(LootItemLevel.LEVEL_7, Material.DIAMOND_LEGGINGS),
            new LootChestItem(LootItemLevel.LEVEL_7, Material.DIAMOND_BOOTS),
            new LootChestItem(LootItemLevel.LEVEL_7, new ItemCrafter(Material.IRON_HELMET).enchant(Enchantment.PROTECTION_ENVIRONMENTAL)),
            new LootChestItem(LootItemLevel.LEVEL_7, new ItemCrafter(Material.IRON_CHESTPLATE).enchant(Enchantment.PROTECTION_ENVIRONMENTAL)),
            new LootChestItem(LootItemLevel.LEVEL_7, new ItemCrafter(Material.IRON_LEGGINGS).enchant(Enchantment.PROTECTION_ENVIRONMENTAL)),
            new LootChestItem(LootItemLevel.LEVEL_7, new ItemCrafter(Material.IRON_BOOTS).enchant(Enchantment.PROTECTION_ENVIRONMENTAL)),
            new LootChestItem(LootItemLevel.LEVEL_8, Material.TNT, 4),
            new LootChestItem(LootItemLevel.LEVEL_8, Material.TNT, 8),
            new LootChestItem(LootItemLevel.LEVEL_8, Material.TNT,  16),
            new LootChestItem(LootItemLevel.LEVEL_8, Material.REDSTONE_BLOCK, 16),
            new LootChestItem(LootItemLevel.LEVEL_8, Material.REDSTONE, 32),
            new LootChestItem(LootItemLevel.LEVEL_8, Material.REDSTONE_TORCH_ON, 16),
            new LootChestItem(LootItemLevel.LEVEL_8, new ItemCrafter(Material.FLINT_AND_STEEL).durability(56)),
            new LootChestItem(LootItemLevel.LEVEL_9, new ItemCrafter(Material.FLINT_AND_STEEL).durability(56)),
            new LootChestItem(LootItemLevel.LEVEL_9, Material.TNT, 12),
            new LootChestItem(LootItemLevel.LEVEL_9, Material.DIAMOND, 4),
            new LootChestItem(LootItemLevel.LEVEL_9, Material.IRON_INGOT, 16),
            new LootChestItem(LootItemLevel.LEVEL_9, Material.REDSTONE, 32),
            new LootChestItem(LootItemLevel.LEVEL_9, Material.COMPASS),
            new LootChestItem(LootItemLevel.LEVEL_9, Material.ARROW, 16),
            new LootChestItem(LootItemLevel.LEVEL_9, Material.WOOD, 32),
            new LootChestItem(LootItemLevel.LEVEL_9, Material.WOOD, 64),
            new LootChestItem(LootItemLevel.LEVEL_9, Material.STONE, 48),
            new LootChestItem(LootItemLevel.LEVEL_10, new ItemCrafter(Material.DIAMOND_HELMET).enchant(Enchantment.PROTECTION_PROJECTILE, 2)),
            new LootChestItem(LootItemLevel.LEVEL_10, new ItemCrafter(Material.DIAMOND_LEGGINGS).enchant(Enchantment.PROTECTION_ENVIRONMENTAL, 2)),
            new LootChestItem(LootItemLevel.LEVEL_10, new ItemCrafter(Material.DIAMOND_CHESTPLATE).enchant(Enchantment.PROTECTION_EXPLOSIONS, 2)),
            new LootChestItem(LootItemLevel.LEVEL_10, new ItemCrafter(Material.DIAMOND_BOOTS).enchant(Enchantment.PROTECTION_FALL)),
            new LootChestItem(LootItemLevel.LEVEL_10, new ItemCrafter(Material.IRON_HELMET).enchant(Enchantment.PROTECTION_ENVIRONMENTAL)),
            new LootChestItem(LootItemLevel.LEVEL_10, new ItemCrafter(Material.IRON_CHESTPLATE).enchant(Enchantment.PROTECTION_ENVIRONMENTAL)),
            new LootChestItem(LootItemLevel.LEVEL_10, new ItemCrafter(Material.IRON_LEGGINGS).enchant(Enchantment.PROTECTION_ENVIRONMENTAL)),
            new LootChestItem(LootItemLevel.LEVEL_10, new ItemCrafter(Material.IRON_BOOTS).enchant(Enchantment.PROTECTION_ENVIRONMENTAL)),
            new LootChestItem(LootItemLevel.LEVEL_11, Material.DIAMOND_SWORD),
            new LootChestItem(LootItemLevel.LEVEL_11, new ItemCrafter(Material.DIAMOND_SWORD).enchant(Enchantment.DAMAGE_ALL)),
            new LootChestItem(LootItemLevel.LEVEL_11, new ItemCrafter(Material.IRON_AXE).enchant(Enchantment.DAMAGE_ALL, 2)),
            new LootChestItem(LootItemLevel.LEVEL_11, new ItemCrafter(Material.DIAMOND_AXE).enchant(Enchantment.DIG_SPEED, 3)),
            new LootChestItem(LootItemLevel.LEVEL_11, new ItemCrafter(Material.DIAMOND_PICKAXE).enchant(Enchantment.DIG_SPEED, 3)),
            new LootChestItem(LootItemLevel.LEVEL_11, new ItemCrafter(Material.BOW).enchant(Enchantment.ARROW_DAMAGE, 2)),
            new LootChestItem(LootItemLevel.LEVEL_12, Material.ENDER_PEARL, 2),
            new LootChestItem(LootItemLevel.LEVEL_12, Material.EXP_BOTTLE, 32),
            new LootChestItem(LootItemLevel.LEVEL_12, Material.GOLDEN_APPLE, 2),
            new LootChestItem(LootItemLevel.LEVEL_12, Material.ANVIL),
            new LootChestItem(LootItemLevel.LEVEL_12, Material.ENCHANTMENT_TABLE)
        );

        List<LootChestItem> sgNormalItems = Arrays.asList(
            new LootChestItem(LootItemLevel.LEVEL_2, Material.STICK, 1),
            new LootChestItem(LootItemLevel.LEVEL_2, Material.STICK, 2),
            new LootChestItem(LootItemLevel.LEVEL_2, Material.FEATHER, 3),
            new LootChestItem(LootItemLevel.LEVEL_2, Material.FLINT, 3),
            new LootChestItem(LootItemLevel.LEVEL_2, Material.STRING, 1),
            new LootChestItem(LootItemLevel.LEVEL_2, Material.ARROW, 1),
            new LootChestItem(LootItemLevel.LEVEL_2, Material.IRON_INGOT, 1),
            new LootChestItem(LootItemLevel.LEVEL_2, Material.GOLD_INGOT, 2),
            new LootChestItem(LootItemLevel.LEVEL_3, Material.MUSHROOM_SOUP, 1),
            new LootChestItem(LootItemLevel.LEVEL_3, Material.APPLE, 3),
            new LootChestItem(LootItemLevel.LEVEL_3, Material.COOKED_BEEF, 1),
            new LootChestItem(LootItemLevel.LEVEL_3, Material.COOKED_BEEF, 2),
            new LootChestItem(LootItemLevel.LEVEL_4, Material.LEATHER_HELMET),
            new LootChestItem(LootItemLevel.LEVEL_4, Material.LEATHER_CHESTPLATE),
            new LootChestItem(LootItemLevel.LEVEL_4, Material.LEATHER_LEGGINGS),
            new LootChestItem(LootItemLevel.LEVEL_4, Material.LEATHER_BOOTS),
            new LootChestItem(LootItemLevel.LEVEL_4, Material.CHAINMAIL_HELMET),
            new LootChestItem(LootItemLevel.LEVEL_4, Material.CHAINMAIL_CHESTPLATE),
            new LootChestItem(LootItemLevel.LEVEL_4, Material.CHAINMAIL_LEGGINGS),
            new LootChestItem(LootItemLevel.LEVEL_4, Material.CHAINMAIL_BOOTS),
            new LootChestItem(LootItemLevel.LEVEL_4, Material.GOLD_HELMET),
            new LootChestItem(LootItemLevel.LEVEL_4, Material.GOLD_CHESTPLATE),
            new LootChestItem(LootItemLevel.LEVEL_4, Material.GOLD_LEGGINGS),
            new LootChestItem(LootItemLevel.LEVEL_4, Material.GOLD_BOOTS),
            new LootChestItem(LootItemLevel.LEVEL_5, Material.WOOD_SWORD),
            new LootChestItem(LootItemLevel.LEVEL_5, Material.GOLD_SWORD),
            new LootChestItem(LootItemLevel.LEVEL_5, Material.STONE_SWORD),
            new LootChestItem(LootItemLevel.LEVEL_5, Material.STONE_AXE),
            new LootChestItem(LootItemLevel.LEVEL_5, new ItemCrafter(Material.WOOD_SWORD).enchant(Enchantment.DAMAGE_ALL)),
            new LootChestItem(LootItemLevel.LEVEL_5, new ItemCrafter(Material.GOLD_SWORD).enchant(Enchantment.DAMAGE_ALL)),
            new LootChestItem(LootItemLevel.LEVEL_7, Material.CHAINMAIL_HELMET),
            new LootChestItem(LootItemLevel.LEVEL_7, Material.CHAINMAIL_LEGGINGS),
            new LootChestItem(LootItemLevel.LEVEL_7, Material.CHAINMAIL_CHESTPLATE),
            new LootChestItem(LootItemLevel.LEVEL_7, Material.CHAINMAIL_BOOTS),
            new LootChestItem(LootItemLevel.LEVEL_7, Material.IRON_HELMET),
            new LootChestItem(LootItemLevel.LEVEL_7, Material.IRON_BOOTS),
            new LootChestItem(LootItemLevel.LEVEL_8, Material.STONE_AXE),
            new LootChestItem(LootItemLevel.LEVEL_8, Material.STONE_SWORD),
            new LootChestItem(LootItemLevel.LEVEL_8, Material.IRON_AXE),
            new LootChestItem(LootItemLevel.LEVEL_8, Material.EXP_BOTTLE),
            new LootChestItem(LootItemLevel.LEVEL_10, new ItemCrafter(Material.FLINT_AND_STEEL).durability(60)),
            new LootChestItem(LootItemLevel.LEVEL_10, new ItemCrafter(Material.FISHING_ROD).durability(60)),
            new LootChestItem(LootItemLevel.LEVEL_10, Material.SNOW_BALL, 4),
            new LootChestItem(LootItemLevel.LEVEL_10, Material.IRON_INGOT, 2),
            new LootChestItem(LootItemLevel.LEVEL_10, Material.GOLD_INGOT, 4),
            new LootChestItem(LootItemLevel.LEVEL_10, Material.ARROW, 2),
            new LootChestItem(LootItemLevel.LEVEL_11, Material.IRON_CHESTPLATE),
            new LootChestItem(LootItemLevel.LEVEL_11, Material.IRON_LEGGINGS),
            new LootChestItem(LootItemLevel.LEVEL_11, Material.IRON_BOOTS),
            new LootChestItem(LootItemLevel.LEVEL_11, Material.IRON_HELMET),
            new LootChestItem(LootItemLevel.LEVEL_11, new ItemCrafter(Material.CHAINMAIL_CHESTPLATE).enchant(Enchantment.PROTECTION_PROJECTILE)),
            new LootChestItem(LootItemLevel.LEVEL_11, new ItemCrafter(Material.CHAINMAIL_LEGGINGS).enchant(Enchantment.PROTECTION_PROJECTILE)),
            new LootChestItem(LootItemLevel.LEVEL_12, Material.IRON_SWORD),
            new LootChestItem(LootItemLevel.LEVEL_12, Material.IRON_AXE),
            new LootChestItem(LootItemLevel.LEVEL_12, Material.BOW),
            new LootChestItem(LootItemLevel.LEVEL_12, Material.DIAMOND)
            // new LootChestItem(LootItemLevel.LEVEL_12, new ItemCrafter(Material.TNT).name("Instant TNT").amount(1))
        );

        List<LootChestItem> sgInsaneItems = Arrays.asList(
            new LootChestItem(LootItemLevel.LEVEL_2, Material.STICK, 1),
            new LootChestItem(LootItemLevel.LEVEL_2, Material.STICK, 3),
            new LootChestItem(LootItemLevel.LEVEL_2, Material.FEATHER, 3),
            new LootChestItem(LootItemLevel.LEVEL_2, Material.FLINT, 3),
            new LootChestItem(LootItemLevel.LEVEL_2, Material.DIAMOND, 3),
            new LootChestItem(LootItemLevel.LEVEL_2, Material.IRON_INGOT, 4),
            new LootChestItem(LootItemLevel.LEVEL_2, Material.IRON_INGOT, 8),
            new LootChestItem(LootItemLevel.LEVEL_3, Material.APPLE, 6),
            new LootChestItem(LootItemLevel.LEVEL_3, Material.STICK, 2),
            new LootChestItem(LootItemLevel.LEVEL_3, Material.COOKED_BEEF, 6),
            new LootChestItem(LootItemLevel.LEVEL_3, Material.SNOW_BALL, 16),
            new LootChestItem(LootItemLevel.LEVEL_3, Material.EXP_BOTTLE, 8),
            new LootChestItem(LootItemLevel.LEVEL_3, Material.ARROW, 8),
            new LootChestItem(LootItemLevel.LEVEL_4, new ItemCrafter(Material.CHAINMAIL_LEGGINGS).enchant(Enchantment.PROTECTION_PROJECTILE, 3)),
            new LootChestItem(LootItemLevel.LEVEL_4, new ItemCrafter(Material.CHAINMAIL_BOOTS).enchant(Enchantment.PROTECTION_FALL, 2)),
            new LootChestItem(LootItemLevel.LEVEL_4, Material.IRON_CHESTPLATE),
            new LootChestItem(LootItemLevel.LEVEL_4, Material.IRON_LEGGINGS),
            new LootChestItem(LootItemLevel.LEVEL_4, Material.IRON_BOOTS),
            new LootChestItem(LootItemLevel.LEVEL_4, Material.IRON_HELMET),
            new LootChestItem(LootItemLevel.LEVEL_4, Material.IRON_BOOTS),
            new LootChestItem(LootItemLevel.LEVEL_5, Material.IRON_AXE),
            new LootChestItem(LootItemLevel.LEVEL_5, Material.IRON_SWORD),
            new LootChestItem(LootItemLevel.LEVEL_5, Material.BOW),
            new LootChestItem(LootItemLevel.LEVEL_5, Material.ARROW, 12),
            new LootChestItem(LootItemLevel.LEVEL_5, new ItemCrafter(Material.FLINT_AND_STEEL).durability(61)),
            new LootChestItem(LootItemLevel.LEVEL_5, new ItemCrafter(Material.FISHING_ROD).durability(58)),
            new LootChestItem(LootItemLevel.LEVEL_5, new ItemCrafter(Material.STONE_SWORD).enchant(Enchantment.DAMAGE_ALL, 2)),
            new LootChestItem(LootItemLevel.LEVEL_7, Material.IRON_HELMET),
            new LootChestItem(LootItemLevel.LEVEL_7, Material.IRON_LEGGINGS),
            new LootChestItem(LootItemLevel.LEVEL_7, Material.IRON_CHESTPLATE),
            new LootChestItem(LootItemLevel.LEVEL_7, Material.IRON_BOOTS),
            new LootChestItem(LootItemLevel.LEVEL_7, Material.DIAMOND_HELMET),
            new LootChestItem(LootItemLevel.LEVEL_7, Material.DIAMOND_BOOTS),
            new LootChestItem(LootItemLevel.LEVEL_8, Material.IRON_AXE),
            new LootChestItem(LootItemLevel.LEVEL_8, Material.IRON_SWORD),
            new LootChestItem(LootItemLevel.LEVEL_8, Material.BOW),
            new LootChestItem(LootItemLevel.LEVEL_8, Material.ARROW, 10),
            new LootChestItem(LootItemLevel.LEVEL_8, new ItemCrafter(Material.STONE_SWORD).enchant(Enchantment.DAMAGE_ALL, 2)),
            new LootChestItem(LootItemLevel.LEVEL_9, new ItemCrafter(Material.FLINT_AND_STEEL).durability(56)),
            new LootChestItem(LootItemLevel.LEVEL_9, Material.DIAMOND, 4),
            new LootChestItem(LootItemLevel.LEVEL_9, Material.IRON_INGOT, 8),
            new LootChestItem(LootItemLevel.LEVEL_9, Material.ARROW, 12),
            new LootChestItem(LootItemLevel.LEVEL_11, Material.DIAMOND_CHESTPLATE),
            new LootChestItem(LootItemLevel.LEVEL_11, Material.DIAMOND_LEGGINGS),
            new LootChestItem(LootItemLevel.LEVEL_11, Material.DIAMOND_BOOTS),
            new LootChestItem(LootItemLevel.LEVEL_11, Material.DIAMOND_HELMET),
            new LootChestItem(LootItemLevel.LEVEL_11, new ItemCrafter(Material.GOLD_BOOTS).enchant(Enchantment.PROTECTION_EXPLOSIONS, 3)),
            new LootChestItem(LootItemLevel.LEVEL_11, new ItemCrafter(Material.GOLD_HELMET).enchant(Enchantment.PROTECTION_EXPLOSIONS, 3)),
            new LootChestItem(LootItemLevel.LEVEL_12, new ItemCrafter(Material.IRON_SWORD).enchant(Enchantment.DAMAGE_ALL)),
            new LootChestItem(LootItemLevel.LEVEL_12, new ItemCrafter(Material.IRON_AXE).enchant(Enchantment.DAMAGE_ALL, 2)),
            new LootChestItem(LootItemLevel.LEVEL_12, new ItemCrafter(Material.BOW).enchant(Enchantment.ARROW_DAMAGE)),
            new LootChestItem(LootItemLevel.LEVEL_12, Material.EXP_BOTTLE, 16),
            new LootChestItem(LootItemLevel.LEVEL_12, Material.GOLDEN_APPLE, 2),
            new LootChestItem(LootItemLevel.LEVEL_12, new ItemCrafter(Material.TNT).name("Instant TNT").amount(4))
        );

        items.put(LootType.SG_NORMAL, getItemMap(sgNormalItems));
        items.put(LootType.SG_INSANE, getItemMap(sgInsaneItems));
        items.put(LootType.SW_NORMAL, getItemMap(swNormalItems));
        items.put(LootType.SW_INSANE, getItemMap(swInsaneItems));
    }

    private static Map<LootItemLevel, List<LootChestItem>> getItemMap(List<LootChestItem> items) {
        Map<LootItemLevel, List<LootChestItem>> itemMap = new HashMap<>();

        for (LootItemLevel lootItemLevel : LootItemLevel.values()) {
            itemMap.put(lootItemLevel, items.stream()
                .filter(item -> item.getLevel() == lootItemLevel)
                .collect(Collectors.toList()));
        }

        return itemMap;
    }

    public static List<LootChestItem> getItems(LootType type, LootItemLevel level) {
        return items.get(type).get(level);
    }

}