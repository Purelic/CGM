package net.purelic.cgm.core.managers;

import net.purelic.cgm.core.constants.MatchTeam;
import net.purelic.cgm.core.gamemodes.EnumSetting;
import net.purelic.cgm.core.gamemodes.NumberSetting;
import net.purelic.cgm.core.gamemodes.ToggleSetting;
import net.purelic.cgm.core.gamemodes.constants.ArmorType;
import net.purelic.cgm.core.gamemodes.constants.BowType;
import net.purelic.cgm.core.gamemodes.constants.SwordType;
import net.purelic.cgm.core.gamemodes.constants.TeamType;
import net.purelic.cgm.listeners.modules.BlockProtectionModule;
import net.purelic.cgm.utils.ColorConverter;
import net.purelic.cgm.utils.PlayerUtils;
import net.purelic.commons.Commons;
import net.purelic.commons.profile.Preference;
import net.purelic.commons.profile.Profile;
import net.purelic.commons.profile.preferences.ArmorColor;
import net.purelic.commons.utils.ItemCrafter;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

public class KitManager {

    public static void getKit(Player player, MatchTeam team) {
        PlayerUtils.clearInventory(player);
        PlayerInventory inventory = player.getInventory();

        int rgb = ColorConverter.convert(team.getColor()).asRGB();

        if (EnumSetting.TEAM_TYPE.is(TeamType.SOLO)) {
            Profile profile = Commons.getProfile(player);
            Object colorPref = profile.getPreference(Preference.ARMOR_COLOR);
            if (colorPref != null) {
                rgb = ((Long) colorPref).intValue();
            } else {
                ArmorColor random = ArmorColor.random(profile.isDonator());
                rgb = ColorConverter.convert(random.getColor()).asRGB();
            }
        }

        inventory.setArmorContents(KitManager.getArmor(rgb));

        Profile profile = Commons.getProfile(player);
        int swordSlot = KitManager.convertSlotToIndex(profile.getPreference(Preference.HOTBAR_SWORD, 1));
        int bowSlot = KitManager.convertSlotToIndex(profile.getPreference(Preference.HOTBAR_BOW, 2));
        int shearsSlot = KitManager.convertSlotToIndex(profile.getPreference(Preference.HOTBAR_SHEARS, 3));
        int arrowsSlot = 3;
        int pickaxeSlot = 4;
        int axeSlot = 5;
        int compassSlot = 6;
        int gapplesSlot = 7;
        int woolSlot = KitManager.convertSlotToIndex(profile.getPreference(Preference.HOTBAR_WOOL, 9));

        SwordType swordType = EnumSetting.PLAYER_SWORD_TYPE.get();
        if (swordType != SwordType.NONE) {
            ItemStack item = KitManager.getSword(swordType.getMaterial(), NumberSetting.PLAYER_SWORD_SHARP.value(), NumberSetting.PLAYER_SWORD_KB.value());
            if (inventory.getItem(swordSlot) != null) inventory.addItem(item);
            else inventory.setItem(swordSlot, item);
        }

        BowType bowType = EnumSetting.PLAYER_BOW_TYPE.get();
        if (bowType != BowType.NONE) {
            ItemStack item = KitManager.getBow(bowType.getMaterial(), NumberSetting.PLAYER_BOW_POWER.value(), NumberSetting.PLAYER_BOW_PUNCH.value(), ToggleSetting.PLAYER_BOW_INFINITY.isEnabled());
            if (inventory.getItem(bowSlot) != null) inventory.addItem(item);
            else inventory.setItem(bowSlot, item);
        }

        if (ToggleSetting.PLAYER_SHEARS_ENABLED.isEnabled() && BlockProtectionModule.canPlaceBlocks()) {
            ItemCrafter shears = new ItemCrafter(Material.SHEARS);
            int efficiency = NumberSetting.PLAYER_SHEAR_EFF.value();
            if (efficiency > 0) shears.enchant(Enchantment.DIG_SPEED, efficiency);
            shears.setTag("locked", "true");
            ItemStack item = shears.setUnbreakable().craft();
            if (inventory.getItem(shearsSlot) != null) inventory.addItem(item);
            else inventory.setItem(shearsSlot, item);
        }

        int wool = NumberSetting.PLAYER_WOOL.value();
        int arrows = NumberSetting.PLAYER_ARROWS.value();
        int gapples = NumberSetting.PLAYER_GAPPLES.value();
        int emeralds = NumberSetting.PLAYER_EMERALDS.value();
        int pearls = NumberSetting.PLAYER_PEARLS.value();

        if (wool > 0 && BlockProtectionModule.canPlaceBlocks()) {
            ItemStack item = new ItemStack(Material.WOOL, wool, ColorConverter.getDyeColor(team.getColor()).getData());
            if (inventory.getItem(woolSlot) != null) inventory.addItem(item);
            else inventory.setItem(woolSlot, item);
        }

        if (arrows > 0) inventory.addItem(new ItemStack(Material.ARROW, arrows));
        if (gapples > 0) inventory.addItem(new ItemStack(Material.GOLDEN_APPLE, gapples));
        if (emeralds > 0) inventory.addItem(new ItemStack(Material.EMERALD, emeralds));
        if (pearls > 0) inventory.addItem(new ItemStack(Material.ENDER_PEARL, pearls));

        if (ToggleSetting.PLAYER_COMPASS_ENABLED.isEnabled() && ToggleSetting.PLAYER_COMPASS_SPAWN_WITH.isEnabled()) {
            ItemCrafter compass = new ItemCrafter(Material.COMPASS).name(ChatColor.BOLD + "Tracker" + ChatColor.RESET + ChatColor.GRAY + " (R-Click)");
            compass.setTag("locked", "true");
            inventory.addItem(compass.craft());
        }
    }

    public static int convertSlotToIndex(Object slot) {
        return (slot instanceof Integer ? (int) slot : ((Long) slot).intValue()) - 1;
    }

    private static ItemStack[] getArmor(int rgb) {
        ArmorType helmetType = EnumSetting.PLAYER_HELMET_TYPE.get();
        ArmorType chestplateType = EnumSetting.PLAYER_CHESTPLATE_TYPE.get();
        ArmorType leggingsType = EnumSetting.PLAYER_LEGGINGS_TYPE.get();
        ArmorType bootsType = EnumSetting.PLAYER_BOOTS_TYPE.get();

        return new ItemStack[]{
                KitManager.getArmorPiece(bootsType.getBoots(), rgb, NumberSetting.PLAYER_BOOTS_PROT.value(), 1, NumberSetting.PLAYER_BOOTS_FF.value(), 1, ToggleSetting.PLAYER_BOOTS_LOCKED.isEnabled()),
                KitManager.getArmorPiece(leggingsType.getLeggings(), rgb, NumberSetting.PLAYER_LEGGINGS_PROT.value(), 2, 0, 0, ToggleSetting.PLAYER_LEGGINGS_LOCKED.isEnabled()),
                KitManager.getArmorPiece(chestplateType.getChestplate(), rgb, NumberSetting.PLAYER_CHESTPLATE_PROT.value(), 2, 0 , 0, ToggleSetting.PLAYER_CHESTPLATE_LOCKED.isEnabled()),
                KitManager.getArmorPiece(helmetType.getHelmet(), rgb, NumberSetting.PLAYER_HELMET_PROT.value(), 0, 0, 0, ToggleSetting.PLAYER_HELMET_LOCKED.isEnabled())
        };
    }

    public static ItemStack getArmorPiece(Material material, int rgb, int protection, int projProt, int featherFalling, int depthStrider, boolean locked) {
        if (material == null) return null;

        ItemCrafter item = new ItemCrafter(material).setUnbreakable();

        if (material.name().contains("LEATHER")) item.color(rgb);
        if (protection > 0) item.enchant(Enchantment.PROTECTION_ENVIRONMENTAL, protection);
        if (projProt > 0) item.enchant(Enchantment.PROTECTION_PROJECTILE, protection);
        if (depthStrider > 0) item.enchant(Enchantment.DEPTH_STRIDER, depthStrider);
        if (material.name().contains("BOOTS") && featherFalling > 0) item.enchant(Enchantment.PROTECTION_FALL, featherFalling);
        if (locked) item.setTag("locked", "true");
        item.setTag("kit", "true");

        return item.craft();
    }

    private static ItemStack getSword(Material material, int sharpness, int knockback) {
        ItemCrafter item = new ItemCrafter(material).setUnbreakable();

        if (sharpness > 0) item.enchant(Enchantment.DAMAGE_ALL, sharpness);
        if (knockback > 0) item.enchant(Enchantment.KNOCKBACK, knockback);

        if (ToggleSetting.PLAYER_SWORD_INSTANT_KILL.isEnabled()) {
            item.name(ChatColor.BOLD + "ONE HIT KILL");
            item.setTag("one_hit_kill", "true");
        }

        if (ToggleSetting.PLAYER_SWORD_LOCKED.isEnabled()) {
            item.setTag("locked", "true");
        }

        item.setTag("kit", "true");

        return item.craft();
    }

    private static ItemStack getBow(Material material, int power, int punch, boolean infinity) {
        ItemCrafter item = new ItemCrafter(material).setUnbreakable();

        if (power > 0) item.enchant(Enchantment.ARROW_DAMAGE, power);
        if (punch > 0) item.enchant(Enchantment.ARROW_KNOCKBACK, punch);
        if (infinity) item.enchant(Enchantment.ARROW_INFINITE);
        if (ToggleSetting.PLAYER_BOW_INSTANT_KILL.isEnabled()) item.name(ChatColor.BOLD + "ONE SHOT KILL");
        if (ToggleSetting.PLAYER_BOW_LOCKED.isEnabled()) item.setTag("locked", "true");

        item.setTag("kit", "true");

        return item.craft();
    }

}