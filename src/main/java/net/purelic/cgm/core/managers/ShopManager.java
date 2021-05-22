package net.purelic.cgm.core.managers;

import net.purelic.cgm.core.constants.MatchTeam;
import net.purelic.cgm.core.gamemodes.EnumSetting;
import net.purelic.cgm.core.gamemodes.constants.ArmorType;
import net.purelic.cgm.core.gamemodes.constants.GameType;
import net.purelic.cgm.core.gamemodes.constants.TeamSize;
import net.purelic.cgm.core.gamemodes.constants.TeamType;
import net.purelic.cgm.core.maps.shop.Shop;
import net.purelic.cgm.core.maps.shop.ShopItem;
import net.purelic.cgm.core.maps.shop.constants.TeamUpgrade;
import net.purelic.commons.utils.ItemCrafter;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.Potion;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionType;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ShopManager {

    private static final Map<MatchTeam, Set<TeamUpgrade>> upgrades = new HashMap<>();
    private static final Map<Player, Set<TeamUpgrade>> soloUpgrades = new HashMap<>();
    private static Shop bedWarsShop = null;

    public static void loadBedWarsShop() {
        boolean discount = EnumSetting.TEAM_SIZE.is(TeamSize.SINGLES, TeamSize.DOUBLES);
        bedWarsShop = new Shop("Bed Wars Shop");

        bedWarsShop.addItem(new ShopItem(
                new ItemStack(Material.WOOL, 16), // item
                4, Material.IRON_INGOT, // cost
                10, true)); // metadata

        bedWarsShop.addItem(new ShopItem(
                new ItemStack(Material.LADDER, 12),
                4, Material.IRON_INGOT,
                19));

        bedWarsShop.addItem(new ShopItem(
                new ItemStack(Material.TNT),
                discount ? 4 : 8, Material.GOLD_INGOT,
                28));

        bedWarsShop.addItem(new ShopItem(
                new ItemCrafter(Material.STAINED_GLASS).amount(4).name("Blast Proof Glass").craft(),
                12, Material.IRON_INGOT,
                11, true)
                .withDescription("This glass will protect your bed from explosions!"));

        bedWarsShop.addItem(new ShopItem(
                new ItemStack(Material.WOOD, 16),
                4, Material.GOLD_INGOT,
                20));

        bedWarsShop.addItem(new ShopItem(
                new ItemStack(Material.ENDER_STONE, 12),
                24, Material.IRON_INGOT,
                29));

        ShopItem diamondPickaxe = new ShopItem(
                new ItemCrafter(Material.DIAMOND_PICKAXE).enchant(Enchantment.DIG_SPEED, 4).setTag("locked", "true").setUnbreakable().craft(),
                6, Material.GOLD_INGOT,
                12);

        ShopItem goldPickaxe = new ShopItem(
                new ItemCrafter(Material.GOLD_PICKAXE).enchant(Enchantment.DIG_SPEED, 3).setTag("locked", "true").setUnbreakable().craft(),
                3, Material.GOLD_INGOT,
                12, diamondPickaxe);

        ShopItem stonePickaxe = new ShopItem(
                new ItemCrafter(Material.STONE_PICKAXE).enchant(Enchantment.DIG_SPEED, 2).setTag("locked", "true").setUnbreakable().craft(),
                10, Material.IRON_INGOT,
                12, goldPickaxe);

        bedWarsShop.addItem(new ShopItem(
                new ItemCrafter(Material.WOOD_PICKAXE).enchant(Enchantment.DIG_SPEED).setTag("locked", "true").setUnbreakable().craft(),
                10, Material.IRON_INGOT,
                12, stonePickaxe));

        ShopItem diamondAxe = new ShopItem(
                new ItemCrafter(Material.DIAMOND_AXE).name("DiamondAx").enchant(Enchantment.DIG_SPEED, 4).setTag("locked", "true").setUnbreakable().craft(),
                6, Material.GOLD_INGOT,
                12);

        ShopItem goldAxe = new ShopItem(
                new ItemCrafter(Material.GOLD_AXE).enchant(Enchantment.DIG_SPEED, 3).setTag("locked", "true").setUnbreakable().craft(),
                3, Material.GOLD_INGOT,
                12, diamondAxe);

        ShopItem stoneAxe = new ShopItem(
                new ItemCrafter(Material.STONE_AXE).enchant(Enchantment.DIG_SPEED, 2).setTag("locked", "true").setUnbreakable().craft(),
                10, Material.IRON_INGOT,
                12, goldAxe);

        bedWarsShop.addItem(new ShopItem(
                new ItemCrafter(Material.WOOD_AXE).enchant(Enchantment.DIG_SPEED).setTag("locked", "true").setUnbreakable().craft(),
                10, Material.IRON_INGOT,
                21, stoneAxe));

        ShopItem spleefers = new ShopItem(
                new ItemCrafter(Material.SHEARS).name("Spleefers").enchant(Enchantment.DIG_SPEED, 2).setTag("locked", "true").setUnbreakable().craft(),
                10, Material.IRON_INGOT,
                30);

        bedWarsShop.addItem(new ShopItem(
                new ItemCrafter(Material.SHEARS).setTag("locked", "true").setUnbreakable().craft(),
                20, Material.IRON_INGOT,
                30, spleefers));

        ShopItem diamondSword = new ShopItem(
                new ItemCrafter(Material.DIAMOND_SWORD).setTag("locked", "true").setUnbreakable().craft(),
                3, Material.EMERALD,
                13);

        ShopItem ironSword = new ShopItem(
                new ItemCrafter(Material.IRON_SWORD).setTag("locked", "true").setUnbreakable().craft(),
                7, Material.GOLD_INGOT,
                13, diamondSword);

        bedWarsShop.addItem(new ShopItem(
                new ItemCrafter(Material.STONE_SWORD).setTag("locked", "true").setUnbreakable().craft(),
                10, Material.IRON_INGOT,
                13, ironSword)
                .withDescription("You will be downgraded to Wood Sword upon death!"));

        ShopItem diamondArmor = new ShopItem(
                new ItemCrafter(Material.DIAMOND_BOOTS).name("Diamond Armor").setTag("locked", "true").setUnbreakable().craft(),
                6, Material.EMERALD,
                22, ArmorType.DIAMOND);

        bedWarsShop.addItem(new ShopItem(
                new ItemCrafter(Material.IRON_BOOTS).name("Iron Armor").setTag("locked", "true").setUnbreakable().craft(),
                12, Material.GOLD_INGOT,
                22, ArmorType.IRON, diamondArmor));

        bedWarsShop.addItem(new ShopItem(
                new ItemCrafter(Material.BOW).name("The Inharu").setUnbreakable().setTag("keep_nbt", "true").craft(),
                16, Material.GOLD_INGOT,
                31));

        bedWarsShop.addItem(new ShopItem(
                new ItemCrafter(Material.STICK).enchant(Enchantment.KNOCKBACK).name("Knockback Stick").setTag("keep_nbt", "true").craft(),
                5, Material.GOLD_INGOT,
                14));

        bedWarsShop.addItem(new ShopItem(
                new ItemStack(Material.GOLDEN_APPLE),
                3, Material.GOLD_INGOT,
                23));

        bedWarsShop.addItem(new ShopItem(
                new ItemStack(Material.ARROW, 6),
                2, Material.GOLD_INGOT,
                32));

        bedWarsShop.addItem(new ShopItem(
                new ItemCrafter(getJumpPotion()).name("Jump V (0:30)").setTag("keep_nbt", "true").craft(),
                1, Material.EMERALD,
                15));

        bedWarsShop.addItem(new ShopItem(
                new ItemCrafter(getSpeedPotion()).name("Speed II (0:45)").setTag("keep_nbt", "true").craft(),
                1, Material.EMERALD,
                24));

        bedWarsShop.addItem(new ShopItem(
                new ItemCrafter(getNightVision()).name("Trap Immunity (0:30)").setTag("keep_nbt", "true").craft(),
                4, Material.GOLD_INGOT,
                33)
                .withDescription("You won't set off enemy traps while you have this effect!"));

        bedWarsShop.addItem(new ShopItem(
                new ItemCrafter(Material.FIREBALL).name("Fireball").setTag("keep_nbt", "true").craft(),
                50, Material.IRON_INGOT,
                16));

        bedWarsShop.addItem(new ShopItem(
                new ItemStack(Material.ENDER_PEARL),
                4, Material.EMERALD,
                25)
                .withDescription("Teleporting will play a sound to everyone!"));

        bedWarsShop.addItem(new ShopItem(
                new ItemCrafter(Material.EGG).name("Bridge Egg").setTag("keep_nbt", "true").craft(),
                1, Material.EMERALD,
                34));

//        ShopItem sharp2 = new ShopItem(
//                new ItemCrafter(Material.GOLD_SWORD).name("Sharpness II").setTag("team_upgrade", TeamUpgrade.SHARPNESS_II.name()).craft(),
//                1, Material.IRON_INGOT,
//                46)
//                .withDescription("Permanently gives your team Sharpness II!");

        bedWarsShop.addItem(new ShopItem(
                new ItemCrafter(Material.GOLD_SWORD).name("Sharpness I").setTag("limited", "true").setTag("team_upgrade", TeamUpgrade.SHARPNESS_I.name()).craft(),
                discount ? 4 : 8, Material.DIAMOND,
                46)
                .withDescription("Permanently gives your team Sharpness I!"));

        ShopItem prot4 = new ShopItem(TeamUpgrade.PROTECTION_IV, discount ? 16 : 30, 47).withDescription("Permanently gives your team Protection IV!");

        ShopItem prot3 = new ShopItem(TeamUpgrade.PROTECTION_III, discount ? 8 : 20, 47, prot4).withDescription("Permanently gives your team Protection III!");

        ShopItem prot2 = new ShopItem(TeamUpgrade.PROTECTION_II, discount ? 4 : 10, 47, prot3).withDescription("Permanently gives your team Protection II!");

        bedWarsShop.addItem(new ShopItem(TeamUpgrade.PROTECTION_I, discount ? 2 : 5, 47, prot2).withDescription("Permanently gives your team Protection I!"));

        ShopItem haste2 = new ShopItem(
                new ItemCrafter(Material.GOLD_PICKAXE).name("Haste II").setTag("team_upgrade", TeamUpgrade.HASTE_II.name()).craft(),
                discount ? 4 : 6, Material.DIAMOND,
                48)
                .withDescription("Permanently gives your team Haste II!");

        bedWarsShop.addItem(new ShopItem(
                new ItemCrafter(Material.GOLD_PICKAXE).name("Haste I").setTag("team_upgrade", TeamUpgrade.HASTE_I.name()).craft(),
                discount ? 2 : 4, Material.DIAMOND,
                48, haste2)
                .withDescription("Permanently gives your team Haste I!"));

//        ShopItem featherFalling2 = new ShopItem(
//                new ItemCrafter(Material.FEATHER).name("Feather Falling II").setTag("team_upgrade", TeamUpgrade.FEATHER_FALLING_II.name()).craft(),
//                1, Material.IRON_INGOT,
//                49)
//                .withDescription("Permanently gives your team Feather Falling II!");
//
//        bedWarsShop.addItem(new ShopItem(
//                new ItemCrafter(Material.FEATHER).name("Feather Falling I").setTag("team_upgrade", TeamUpgrade.FEATHER_FALLING_I.name()).craft(),
//                1, Material.IRON_INGOT,
//                49, featherFalling2)
//                .withDescription("Permanently gives your team Feather Falling I!"));

        ShopItem trap3 = new ShopItem(TeamUpgrade.TRAP_III, 1, 49).withDescription("Gives the enemy mining fatigue (8s), slowness (4s), and blindness (4s)!");

        ShopItem trap2 = new ShopItem(TeamUpgrade.TRAP_II, 1, 49, trap3).withDescription("Gives the enemy mining fatigue (8s) and and slowness (4s)!");

        ShopItem trap1 = new ShopItem(TeamUpgrade.TRAP_I, 1, 49, trap2).withDescription("Gives the enemy mining fatigue for 8 seconds!");

        bedWarsShop.addItem(trap1);

        ShopItem respawn2 = new ShopItem(
                new ItemCrafter(Material.SKULL_ITEM).name(TeamUpgrade.QUICK_RESPAWN_II.getName()).setTag("team_upgrade", TeamUpgrade.QUICK_RESPAWN_II.name()).craft(),
                discount ? 6 : 8, Material.DIAMOND,
                50)
                .withDescription("Permanently decreases your team's respawn time by 30%!");

        bedWarsShop.addItem(new ShopItem(
                new ItemCrafter(Material.SKULL_ITEM).name(TeamUpgrade.QUICK_RESPAWN_I.getName()).setTag("team_upgrade", TeamUpgrade.QUICK_RESPAWN_I.name()).craft(),
                discount ? 4 : 6, Material.DIAMOND,
                50, respawn2)
                .withDescription("Permanently decreases your team's respawn time by 15%!"));

        bedWarsShop.addItem(new ShopItem(
                new ItemCrafter(Material.COMPASS).name("Tracking Compass").setTag("locked", "true").setTag("limited", "true").craft(),
                2, Material.EMERALD,
                52)
                .withDescription("Track the closest enemy! Only works after all enemy beds are broken."));
    }

    public static void openShop(Player player) {
        if (EnumSetting.GAME_TYPE.is(GameType.BED_WARS)) {
            bedWarsShop.open(player);
        }
    }

    public static ShopItem getShopItem(String id) {
        if (EnumSetting.GAME_TYPE.is(GameType.BED_WARS)) {
            return bedWarsShop.getItem(id);
        }

        return null;
    }

    public static boolean isShop(Inventory inventory) {
        if (bedWarsShop == null) return false;
        return inventory.getTitle().equals(bedWarsShop.getTitle());
    }

    public static Map<MatchTeam, Set<TeamUpgrade>> getUpgrades() {
        return upgrades;
    }

    public static void addUpgrade(Player player, TeamUpgrade upgrade) {
        MatchTeam team = MatchTeam.getTeam(player);

        soloUpgrades.putIfAbsent(player, new HashSet<>());
        upgrades.putIfAbsent(team, new HashSet<>());

        if (EnumSetting.TEAM_TYPE.is(TeamType.SOLO)) soloUpgrades.get(player).add(upgrade);
        else upgrades.get(team).add(upgrade);
    }

    public static boolean hasUpgrade(Player player, TeamUpgrade upgrade) {
        if (EnumSetting.TEAM_TYPE.is(TeamType.SOLO)) return soloUpgrades.containsKey(player) && soloUpgrades.get(player).contains(upgrade);
        else return hasUpgrade(MatchTeam.getTeam(player), upgrade);
    }

    private static boolean hasUpgrade(MatchTeam team, TeamUpgrade upgrade) {
        return upgrades.containsKey(team) && upgrades.get(team).contains(upgrade);
    }

    public static void clearUpgrades() {
        upgrades.clear();
        soloUpgrades.clear();
    }

    private static ItemStack getJumpPotion() {
        Potion potion = new Potion(PotionType.JUMP);
        ItemStack item = potion.toItemStack(1);
        PotionMeta meta = (PotionMeta) item.getItemMeta();
        meta.addCustomEffect(new PotionEffect(PotionEffectType.JUMP, 600, 4), false);
        meta.addItemFlags(ItemFlag.HIDE_POTION_EFFECTS);
        item.setItemMeta(meta);
        return item;
    }

    private static ItemStack getSpeedPotion() {
        Potion potion = new Potion(PotionType.SPEED);
        ItemStack item = potion.toItemStack(1);
        PotionMeta meta = (PotionMeta) item.getItemMeta();
        meta.addCustomEffect(new PotionEffect(PotionEffectType.SPEED, 900, 1), false);
        meta.addItemFlags(ItemFlag.HIDE_POTION_EFFECTS);
        item.setItemMeta(meta);
        return item;
    }

    private static ItemStack getNightVision() {
        Potion potion = new Potion(PotionType.NIGHT_VISION);
        ItemStack item = potion.toItemStack(1);
        PotionMeta meta = (PotionMeta) item.getItemMeta();
        meta.addCustomEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, 900, 0), false);
        meta.addItemFlags(ItemFlag.HIDE_POTION_EFFECTS);
        item.setItemMeta(meta);
        return item;
    }

}
