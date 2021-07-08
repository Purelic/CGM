package net.purelic.cgm.kit;

import net.md_5.bungee.api.ChatColor;
import net.purelic.cgm.core.constants.MatchTeam;
import net.purelic.cgm.core.gamemodes.CustomGameMode;
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
import net.purelic.cgm.utils.PreferenceUtils;
import net.purelic.commons.Commons;
import net.purelic.commons.profile.Preference;
import net.purelic.commons.profile.Profile;
import net.purelic.commons.profile.preferences.ArmorColor;
import net.purelic.commons.utils.ItemCrafter;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.util.ArrayList;
import java.util.List;

public class Kit {

    private final CustomGameMode gameMode;
    private final KitType kitType;
    private final ItemStack helmet;
    private final ItemStack chestplate;
    private final ItemStack leggings;
    private final ItemStack boots;
    private final ItemStack sword;
    private final ItemStack bow;
    private final ItemStack shears;
    private final ItemStack compass;
    private final List<ItemStack> items;

    public Kit(CustomGameMode gameMode, KitType kitType) {
        this.gameMode = gameMode;
        this.kitType = kitType;
        this.helmet = this.getHelmet();
        this.chestplate = this.getChestplate();
        this.leggings = this.getLeggings();
        this.boots = this.getBoots();
        this.sword = this.getSword();
        this.bow = this.getBow();
        this.shears = this.getShears();
        this.compass = this.getCompass();
        this.items = this.getItems();
    }

    public void apply(Player player) {
        PlayerUtils.clearInventory(player);

        Profile profile = Commons.getProfile(player);
        PlayerInventory inventory = player.getInventory();
        ChatColor color = MatchTeam.getTeam(player).getColor();
        int rgb = this.getRGB(player, color);

        int swordSlot = PreferenceUtils.slotToIndex(profile.getPreference(Preference.HOTBAR_SWORD, 1));
        int bowSlot = PreferenceUtils.slotToIndex(profile.getPreference(Preference.HOTBAR_BOW, 2));
        int shearsSlot = PreferenceUtils.slotToIndex(profile.getPreference(Preference.HOTBAR_SHEARS, 3));
        int woolSlot = PreferenceUtils.slotToIndex(profile.getPreference(Preference.HOTBAR_WOOL, 9));

        inventory.setArmorContents(this.getArmor(rgb));
        this.addItem(inventory, swordSlot, this.sword);
        this.addItem(inventory, bowSlot, this.bow);
        this.addItem(inventory, shearsSlot, this.shears);
        this.addItem(inventory, woolSlot, this.getWool(color));
        for (ItemStack item : this.items) inventory.addItem(item);
        if (this.compass != null) inventory.addItem(this.compass);
    }

    private int getRGB(Player player, ChatColor fallback) {
        TeamType teamType = TeamType.valueOf(this.gameMode.getEnumSetting(EnumSetting.TEAM_TYPE));
        int rgb = ColorConverter.convert(fallback).asRGB();

        if (teamType == TeamType.SOLO) {
            Profile profile = Commons.getProfile(player);
            Object colorPref = profile.getPreference(Preference.ARMOR_COLOR);

            if (colorPref != null) {
                rgb = ((Long) colorPref).intValue();
            } else {
                ArmorColor random = ArmorColor.random(profile.isDonator());
                rgb = ColorConverter.convert(random.getColor()).asRGB();
            }
        }

        return rgb;
    }

    private void addItem(PlayerInventory inventory, int slot, ItemStack item) {
        if (item == null) return;
        if (inventory.getItem(slot) != null) inventory.addItem(item);
        else inventory.setItem(slot, item);
    }

    private ItemStack[] getArmor(int rgb) {
        return new ItemStack[]{
            this.getColoredArmor(this.boots, rgb),
            this.getColoredArmor(this.leggings, rgb),
            this.getColoredArmor(this.chestplate, rgb),
            this.getColoredArmor(this.helmet, rgb)
        };
    }

    private ItemStack getColoredArmor(ItemStack item, int rgb) {
        if (item == null) return null;
        else if (!item.getType().name().contains("LEATHER")) return item;
        else return new ItemCrafter(item).color(rgb).craft();
    }

    private ItemStack getHelmet() {
        if (this.kitType == KitType.DEFAULT) {
            return this.getArmorPiece(
                ArmorType.valueOf(this.gameMode.getEnumSetting(EnumSetting.PLAYER_HELMET_TYPE)).getHelmet(),
                this.gameMode.getNumberSetting(NumberSetting.PLAYER_HELMET_PROT),
                this.gameMode.getNumberSetting(NumberSetting.PLAYER_HELMET_PROJ_PROT),
                this.gameMode.getNumberSetting(NumberSetting.PLAYER_HELMET_BLAST_PROT),
                this.gameMode.getNumberSetting(NumberSetting.PLAYER_HELMET_FIRE_PROT),
                this.gameMode.getNumberSetting(NumberSetting.PLAYER_HELMET_THORNS),
                this.gameMode.getNumberSetting(NumberSetting.PLAYER_HELMET_RESPIRATION),
                this.gameMode.getToggleSetting(ToggleSetting.PLAYER_HELMET_AQUA_AFFINITY),
                0,
                0,
                this.gameMode.getToggleSetting(ToggleSetting.PLAYER_HELMET_UNBREAKABLE),
                this.gameMode.getToggleSetting(ToggleSetting.PLAYER_HELMET_LOCKED)
            );
        } else if (this.kitType == KitType.INFECTED) {
            return this.getArmorPiece(
                ArmorType.valueOf(this.gameMode.getEnumSetting(EnumSetting.INFECTED_HELMET_TYPE)).getHelmet(),
                this.gameMode.getNumberSetting(NumberSetting.INFECTED_HELMET_PROT),
                this.gameMode.getNumberSetting(NumberSetting.INFECTED_HELMET_PROJ_PROT),
                this.gameMode.getNumberSetting(NumberSetting.INFECTED_HELMET_BLAST_PROT),
                this.gameMode.getNumberSetting(NumberSetting.INFECTED_HELMET_FIRE_PROT),
                this.gameMode.getNumberSetting(NumberSetting.INFECTED_HELMET_THORNS),
                this.gameMode.getNumberSetting(NumberSetting.INFECTED_HELMET_RESPIRATION),
                this.gameMode.getToggleSetting(ToggleSetting.INFECTED_HELMET_AQUA_AFFINITY),
                0,
                0,
                this.gameMode.getToggleSetting(ToggleSetting.INFECTED_HELMET_UNBREAKABLE),
                this.gameMode.getToggleSetting(ToggleSetting.INFECTED_HELMET_LOCKED)
            );
        } else {
            return null;
        }
    }

    private ItemStack getChestplate() {
        if (this.kitType == KitType.DEFAULT) {
            return this.getArmorPiece(
                ArmorType.valueOf(this.gameMode.getEnumSetting(EnumSetting.PLAYER_CHESTPLATE_TYPE)).getChestplate(),
                this.gameMode.getNumberSetting(NumberSetting.PLAYER_CHESTPLATE_PROT),
                this.gameMode.getNumberSetting(NumberSetting.PLAYER_CHESTPLATE_PROJ_PROT),
                this.gameMode.getNumberSetting(NumberSetting.PLAYER_CHESTPLATE_BLAST_PROT),
                this.gameMode.getNumberSetting(NumberSetting.PLAYER_CHESTPLATE_FIRE_PROT),
                this.gameMode.getNumberSetting(NumberSetting.PLAYER_CHESTPLATE_THORNS),
                0,
                false,
                0,
                0,
                this.gameMode.getToggleSetting(ToggleSetting.PLAYER_CHESTPLATE_UNBREAKABLE),
                this.gameMode.getToggleSetting(ToggleSetting.PLAYER_CHESTPLATE_LOCKED)
            );
        } else if (this.kitType == KitType.INFECTED) {
            return this.getArmorPiece(
                ArmorType.valueOf(this.gameMode.getEnumSetting(EnumSetting.INFECTED_CHESTPLATE_TYPE)).getChestplate(),
                this.gameMode.getNumberSetting(NumberSetting.INFECTED_CHESTPLATE_PROT),
                this.gameMode.getNumberSetting(NumberSetting.INFECTED_CHESTPLATE_PROJ_PROT),
                this.gameMode.getNumberSetting(NumberSetting.INFECTED_CHESTPLATE_BLAST_PROT),
                this.gameMode.getNumberSetting(NumberSetting.INFECTED_CHESTPLATE_FIRE_PROT),
                this.gameMode.getNumberSetting(NumberSetting.INFECTED_CHESTPLATE_THORNS),
                0,
                false,
                0,
                0,
                this.gameMode.getToggleSetting(ToggleSetting.INFECTED_CHESTPLATE_UNBREAKABLE),
                this.gameMode.getToggleSetting(ToggleSetting.INFECTED_CHESTPLATE_LOCKED)
            );
        } else {
            return null;
        }
    }

    private ItemStack getLeggings() {
        if (this.kitType == KitType.DEFAULT) {
            return this.getArmorPiece(
                ArmorType.valueOf(this.gameMode.getEnumSetting(EnumSetting.PLAYER_LEGGINGS_TYPE)).getLeggings(),
                this.gameMode.getNumberSetting(NumberSetting.PLAYER_LEGGINGS_PROT),
                this.gameMode.getNumberSetting(NumberSetting.PLAYER_LEGGINGS_PROJ_PROT),
                this.gameMode.getNumberSetting(NumberSetting.PLAYER_LEGGINGS_BLAST_PROT),
                this.gameMode.getNumberSetting(NumberSetting.PLAYER_LEGGINGS_FIRE_PROT),
                this.gameMode.getNumberSetting(NumberSetting.PLAYER_LEGGINGS_THORNS),
                0,
                false,
                0,
                0,
                this.gameMode.getToggleSetting(ToggleSetting.PLAYER_LEGGINGS_UNBREAKABLE),
                this.gameMode.getToggleSetting(ToggleSetting.PLAYER_LEGGINGS_LOCKED)
            );
        } else if (this.kitType == KitType.INFECTED) {
            return this.getArmorPiece(
                ArmorType.valueOf(this.gameMode.getEnumSetting(EnumSetting.INFECTED_LEGGINGS_TYPE)).getLeggings(),
                this.gameMode.getNumberSetting(NumberSetting.INFECTED_LEGGINGS_PROT),
                this.gameMode.getNumberSetting(NumberSetting.INFECTED_LEGGINGS_PROJ_PROT),
                this.gameMode.getNumberSetting(NumberSetting.INFECTED_LEGGINGS_BLAST_PROT),
                this.gameMode.getNumberSetting(NumberSetting.INFECTED_LEGGINGS_FIRE_PROT),
                this.gameMode.getNumberSetting(NumberSetting.INFECTED_LEGGINGS_THORNS),
                0,
                false,
                0,
                0,
                this.gameMode.getToggleSetting(ToggleSetting.INFECTED_LEGGINGS_UNBREAKABLE),
                this.gameMode.getToggleSetting(ToggleSetting.INFECTED_LEGGINGS_LOCKED)
            );
        } else {
            return null;
        }
    }

    private ItemStack getBoots() {
        if (this.kitType == KitType.DEFAULT) {
            return this.getArmorPiece(
                ArmorType.valueOf(this.gameMode.getEnumSetting(EnumSetting.PLAYER_BOOTS_TYPE)).getBoots(),
                this.gameMode.getNumberSetting(NumberSetting.PLAYER_BOOTS_PROT),
                this.gameMode.getNumberSetting(NumberSetting.PLAYER_BOOTS_PROJ_PROT),
                this.gameMode.getNumberSetting(NumberSetting.PLAYER_BOOTS_BLAST_PROT),
                this.gameMode.getNumberSetting(NumberSetting.PLAYER_BOOTS_FIRE_PROT),
                this.gameMode.getNumberSetting(NumberSetting.PLAYER_BOOTS_THORNS),
                0,
                false,
                this.gameMode.getNumberSetting(NumberSetting.PLAYER_BOOTS_FF),
                this.gameMode.getNumberSetting(NumberSetting.PLAYER_BOOTS_DEPTH_STRIDER),
                this.gameMode.getToggleSetting(ToggleSetting.PLAYER_BOOTS_UNBREAKABLE),
                this.gameMode.getToggleSetting(ToggleSetting.PLAYER_BOOTS_LOCKED)
            );
        } else if (this.kitType == KitType.INFECTED) {
            return this.getArmorPiece(
                ArmorType.valueOf(this.gameMode.getEnumSetting(EnumSetting.INFECTED_BOOTS_TYPE)).getBoots(),
                this.gameMode.getNumberSetting(NumberSetting.INFECTED_BOOTS_PROT),
                this.gameMode.getNumberSetting(NumberSetting.INFECTED_BOOTS_PROJ_PROT),
                this.gameMode.getNumberSetting(NumberSetting.INFECTED_BOOTS_BLAST_PROT),
                this.gameMode.getNumberSetting(NumberSetting.INFECTED_BOOTS_FIRE_PROT),
                this.gameMode.getNumberSetting(NumberSetting.INFECTED_BOOTS_THORNS),
                0,
                false,
                this.gameMode.getNumberSetting(NumberSetting.INFECTED_BOOTS_FF),
                this.gameMode.getNumberSetting(NumberSetting.INFECTED_BOOTS_DEPTH_STRIDER),
                this.gameMode.getToggleSetting(ToggleSetting.INFECTED_BOOTS_UNBREAKABLE),
                this.gameMode.getToggleSetting(ToggleSetting.INFECTED_BOOTS_LOCKED)
            );
        } else {
            return null;
        }
    }

    private ItemStack getArmorPiece(Material material, int protection, int projectileProtection, int blastProtection,
                                    int fireProtection, int thorns, int respiration, boolean aquaAffinity,
                                    int featherFalling, int depthStrider, boolean unbreakable, boolean locked) {
        if (material == null) return null;
        ItemCrafter item = new ItemCrafter(material).addTag("kit");
        if (unbreakable) item.setUnbreakable();
        if (locked) item.addTag("locked");
        if (protection > 0) item.enchant(Enchantment.PROTECTION_ENVIRONMENTAL, protection);
        if (projectileProtection > 0) item.enchant(Enchantment.PROTECTION_PROJECTILE, projectileProtection);
        if (blastProtection > 0) item.enchant(Enchantment.PROTECTION_EXPLOSIONS, blastProtection);
        if (fireProtection > 0) item.enchant(Enchantment.PROTECTION_FIRE, fireProtection);
        if (thorns > 0) item.enchant(Enchantment.THORNS, thorns);
        if (respiration > 0) item.enchant(Enchantment.OXYGEN, respiration);
        if (aquaAffinity) item.enchant(Enchantment.WATER_WORKER);
        if (featherFalling > 0) item.enchant(Enchantment.PROTECTION_FALL, featherFalling);
        if (depthStrider > 0) item.enchant(Enchantment.DEPTH_STRIDER, depthStrider);
        return item.craft();
    }

    private ItemStack getSword() {
        if (this.kitType == KitType.DEFAULT) {
            return this.getSword(
                SwordType.valueOf(this.gameMode.getEnumSetting(EnumSetting.PLAYER_SWORD_TYPE)).getMaterial(),
                this.gameMode.getNumberSetting(NumberSetting.PLAYER_SWORD_SHARP),
                this.gameMode.getNumberSetting(NumberSetting.PLAYER_SWORD_KB),
                this.gameMode.getToggleSetting(ToggleSetting.PLAYER_SWORD_INSTANT_KILL),
                this.gameMode.getToggleSetting(ToggleSetting.PLAYER_SWORD_LOCKED),
                this.gameMode.getToggleSetting(ToggleSetting.PLAYER_BOOTS_UNBREAKABLE)
            );
        } else if (this.kitType == KitType.INFECTED) {
            return this.getSword(
                SwordType.valueOf(this.gameMode.getEnumSetting(EnumSetting.INFECTED_SWORD_TYPE)).getMaterial(),
                this.gameMode.getNumberSetting(NumberSetting.INFECTED_SWORD_SHARP),
                this.gameMode.getNumberSetting(NumberSetting.INFECTED_SWORD_KB),
                this.gameMode.getToggleSetting(ToggleSetting.INFECTED_SWORD_INSTANT_KILL),
                this.gameMode.getToggleSetting(ToggleSetting.INFECTED_SWORD_LOCKED),
                this.gameMode.getToggleSetting(ToggleSetting.INFECTED_BOOTS_UNBREAKABLE)
            );
        } else {
            return null;
        }
    }

    private ItemStack getSword(Material material, int sharpness, int knockback, boolean instantKill, boolean locked, boolean unbreakable) {
        if (material == null) return null;
        ItemCrafter item = new ItemCrafter(material).addTag("kit");
        if (unbreakable) item.setUnbreakable();
        if (sharpness > 0) item.enchant(Enchantment.DAMAGE_ALL, sharpness);
        if (knockback > 0) item.enchant(Enchantment.KNOCKBACK, knockback);
        if (locked) item.addTag("locked");
        if (instantKill) {
            item.name(ChatColor.BOLD + "ONE HIT KILL");
            item.addTag("one_hit_kill");
        }
        return item.craft();
    }

    private ItemStack getBow() {
        if (this.kitType == KitType.DEFAULT) {
            return this.getBow(
                BowType.valueOf(this.gameMode.getEnumSetting(EnumSetting.PLAYER_BOW_TYPE)).getMaterial(),
                this.gameMode.getNumberSetting(NumberSetting.PLAYER_BOW_POWER),
                this.gameMode.getNumberSetting(NumberSetting.PLAYER_BOW_PUNCH),
                this.gameMode.getToggleSetting(ToggleSetting.PLAYER_BOW_INFINITY),
                this.gameMode.getToggleSetting(ToggleSetting.PLAYER_BOW_INSTANT_KILL),
                this.gameMode.getToggleSetting(ToggleSetting.PLAYER_BOW_LOCKED),
                this.gameMode.getToggleSetting(ToggleSetting.PLAYER_BOW_UNBREAKABLE)
            );
        } else if (this.kitType == KitType.INFECTED) {
            return this.getBow(
                BowType.valueOf(this.gameMode.getEnumSetting(EnumSetting.INFECTED_BOW_TYPE)).getMaterial(),
                this.gameMode.getNumberSetting(NumberSetting.INFECTED_BOW_POWER),
                this.gameMode.getNumberSetting(NumberSetting.INFECTED_BOW_PUNCH),
                this.gameMode.getToggleSetting(ToggleSetting.INFECTED_BOW_INFINITY),
                this.gameMode.getToggleSetting(ToggleSetting.INFECTED_BOW_INSTANT_KILL),
                this.gameMode.getToggleSetting(ToggleSetting.INFECTED_BOW_LOCKED),
                this.gameMode.getToggleSetting(ToggleSetting.INFECTED_BOW_UNBREAKABLE)
            );
        } else {
            return null;
        }
    }

    private ItemStack getBow(Material material, int power, int punch, boolean infinity, boolean instantKill, boolean locked, boolean unbreakable) {
        if (material == null) return null;
        ItemCrafter item = new ItemCrafter(material).addTag("kit");
        if (power > 0) item.enchant(Enchantment.ARROW_DAMAGE, power);
        if (punch > 0) item.enchant(Enchantment.ARROW_KNOCKBACK, punch);
        if (infinity) item.enchant(Enchantment.ARROW_INFINITE);
        if (instantKill) item.name(ChatColor.BOLD + "ONE SHOT KILL");
        if (locked) item.addTag("locked");
        if (unbreakable) item.setUnbreakable();
        return item.craft();
    }

    private ItemStack getShears() {
        if (this.kitType == KitType.DEFAULT && this.gameMode.getToggleSetting(ToggleSetting.PLAYER_SHEARS_ENABLED)) {
            return this.getShears(this.gameMode.getNumberSetting(NumberSetting.PLAYER_SHEAR_EFF));
        } else if (this.kitType == KitType.INFECTED && this.gameMode.getToggleSetting(ToggleSetting.INFECTED_SHEARS_ENABLED)) {
            return this.getShears(this.gameMode.getNumberSetting(NumberSetting.INFECTED_SHEAR_EFF));
        } else {
            return null;
        }
    }

    private ItemStack getShears(int efficiency) {
        ItemCrafter shears = new ItemCrafter(Material.SHEARS)
            .addTag("kit")
            .addTag("locked")
            .setUnbreakable();
        if (efficiency > 0) shears.enchant(Enchantment.DIG_SPEED, efficiency);
        return shears.craft();
    }

    private ItemStack getCompass() {
        if ((this.kitType == KitType.DEFAULT
            && this.gameMode.getToggleSetting(ToggleSetting.PLAYER_COMPASS_ENABLED)
            && this.gameMode.getToggleSetting(ToggleSetting.PLAYER_COMPASS_SPAWN_WITH))
            ||
            (this.kitType == KitType.INFECTED
            && this.gameMode.getToggleSetting(ToggleSetting.INFECTED_COMPASS_ENABLED))
            && this.gameMode.getToggleSetting(ToggleSetting.INFECTED_COMPASS_SPAWN_WITH)) {
            return new ItemCrafter(Material.COMPASS)
                .name(ChatColor.BOLD + "Tracker" + ChatColor.RESET + ChatColor.GRAY + " (R-Click)")
                .addTag("locked")
                .craft();
        } else {
            return null;
        }
    }

    private List<ItemStack> getItems() {
        List<ItemStack> items = new ArrayList<>();

        int arrows = 0;
        int gapples = 0;
        int emeralds = 0;
        int pearls = 0;

        if (this.kitType == KitType.DEFAULT) {
            arrows = this.gameMode.getNumberSetting(NumberSetting.PLAYER_ARROWS);
            gapples = this.gameMode.getNumberSetting(NumberSetting.PLAYER_GAPPLES);
            emeralds = this.gameMode.getNumberSetting(NumberSetting.PLAYER_EMERALDS);
            pearls = this.gameMode.getNumberSetting(NumberSetting.PLAYER_PEARLS);
        } else if (this.kitType == KitType.INFECTED) {
            arrows = this.gameMode.getNumberSetting(NumberSetting.INFECTED_ARROWS);
            gapples = this.gameMode.getNumberSetting(NumberSetting.INFECTED_GAPPLES);
            emeralds = this.gameMode.getNumberSetting(NumberSetting.INFECTED_EMERALDS);
            pearls = this.gameMode.getNumberSetting(NumberSetting.INFECTED_PEARLS);
        }

        if (arrows > 0) items.add(new ItemStack(Material.ARROW, arrows));
        if (gapples > 0) items.add(new ItemStack(Material.GOLDEN_APPLE, gapples));
        if (emeralds > 0) items.add(new ItemStack(Material.EMERALD, emeralds));
        if (pearls > 0) items.add(new ItemStack(Material.ENDER_PEARL, pearls));

        return items;
    }

    private ItemStack getWool(ChatColor color) {
        int wool = 0;

        if (this.kitType == KitType.DEFAULT) {
            wool = this.gameMode.getNumberSetting(NumberSetting.PLAYER_WOOL);
        } else if (this.kitType == KitType.INFECTED) {
            wool = this.gameMode.getNumberSetting(NumberSetting.INFECTED_WOOL);
        }

        if (wool > 0 && BlockProtectionModule.canPlaceBlocks()) {
            return new ItemStack(Material.WOOL, wool, ColorConverter.getDyeColor(color).getData());
        } else {
            return null;
        }
    }

}
