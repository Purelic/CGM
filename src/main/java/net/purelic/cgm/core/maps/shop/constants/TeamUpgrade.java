package net.purelic.cgm.core.maps.shop.constants;

import net.purelic.cgm.core.constants.MatchTeam;
import net.purelic.cgm.core.managers.ShopManager;
import net.purelic.cgm.utils.PlayerUtils;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffectType;

public enum TeamUpgrade {

    SHARPNESS_I("Sharpness I", Material.GOLD_SWORD),
    SHARPNESS_II("Sharpness II", Material.GOLD_SWORD),
    PROTECTION_I("Protection I", Material.GOLD_CHESTPLATE),
    PROTECTION_II("Protection II", Material.GOLD_CHESTPLATE),
    PROTECTION_III("Protection III", Material.GOLD_CHESTPLATE),
    PROTECTION_IV("Protection IV", Material.GOLD_CHESTPLATE),
    HASTE_I("Haste I", Material.GOLD_PICKAXE),
    HASTE_II("Haste II", Material.GOLD_PICKAXE),
    FEATHER_FALLING_I("Feather Falling I", Material.FEATHER),
    FEATHER_FALLING_II("Feather Falling II", Material.FEATHER),
    TRAP_I("Mining Fatigue Trap", Material.TRIPWIRE_HOOK),
    TRAP_II("Slowness Trap", Material.TRIPWIRE_HOOK),
    TRAP_III("Blindness Trap", Material.TRIPWIRE_HOOK),
    QUICK_RESPAWN_I("15% Faster Respawn", Material.SKULL_ITEM),
    QUICK_RESPAWN_II("30% Faster Respawn", Material.SKULL_ITEM),
    ;

    private final String name;
    private final Material material;

    TeamUpgrade(String name, Material material) {
        this.name = name;
        this.material = material;
    }

    public String getName() {
        return this.name;
    }

    public Material getMaterial() {
        return this.material;
    }

    public static void applyUpgrades(MatchTeam team) {
        team.getPlayers().forEach(TeamUpgrade::applyUpgrades);
    }

    public static void applyUpgrades(Player player) {
        applyUpgrades(player, player.getInventory());
    }

    public static void applyUpgrades(Player player, Inventory inventory) {
        for (TeamUpgrade upgrade : values()) {
            if (!ShopManager.hasUpgrade(player, upgrade)) continue;

            switch (upgrade) {
                case SHARPNESS_I:
                    applySharpnessUpgrade(inventory, 1);
                    break;
                case SHARPNESS_II:
                    applySharpnessUpgrade(inventory, 2);
                    break;
                case PROTECTION_I:
                    applyProtectionUpgrade(player, 1);
                    break;
                case PROTECTION_II:
                    applyProtectionUpgrade(player, 2);
                    break;
                case PROTECTION_III:
                    applyProtectionUpgrade(player, 3);
                    break;
                case PROTECTION_IV:
                    applyProtectionUpgrade(player, 4);
                    break;
                case HASTE_I:
                    PlayerUtils.addPermanentEffect(player, PotionEffectType.FAST_DIGGING);
                    break;
                case HASTE_II:
                    player.removePotionEffect(PotionEffectType.FAST_DIGGING);
                    PlayerUtils.addPermanentEffect(player, PotionEffectType.FAST_DIGGING, 1);
                    break;
                case FEATHER_FALLING_I:
                    // applyFeatherFallingUpgrade(player, 1);
                    break;
                case FEATHER_FALLING_II:
                    // applyFeatherFallingUpgrade(player, 2);
                    break;
            }
        }

        player.updateInventory();
    }

    private static void applySharpnessUpgrade(Inventory inventory, int level) {
        for (ItemStack itemStack : inventory.getContents()) {
            if (itemStack != null && itemStack.getType().name().contains("_SWORD")) {
                itemStack.addEnchantment(Enchantment.DAMAGE_ALL, level);
            }
        }
    }

    private static void applyProtectionUpgrade(Player player, int level) {
        for (ItemStack itemStack : player.getInventory().getArmorContents()) {
            if (itemStack != null && itemStack.getType() != Material.AIR) {
                itemStack.addEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, level);
            }
        }
    }

    private static void applyFeatherFallingUpgrade(Player player, int level) {
        ItemStack itemStack = player.getInventory().getArmorContents()[0];
        if (itemStack != null) {
            itemStack.addEnchantment(Enchantment.PROTECTION_FALL, level);
        }
    }

}
