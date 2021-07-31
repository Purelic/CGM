package net.purelic.cgm.uhc;

import net.purelic.cgm.uhc.scenarios.*;
import net.purelic.commons.modules.Module;
import net.purelic.commons.utils.ItemCrafter;
import org.bukkit.ChatColor;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public enum UHCScenario {

    CUT_CLEAN(CutCleanScenario.class, "Cut Clean", Material.COOKED_BEEF, "Ore and animal drops are smelted.", "All trees drop apples.", "Apple rates are 3%.", "Flint rates are 50%."),
    GOLDEN_HEADS(GoldenHeadScenario.class, "Golden Heads", Material.SKULL_ITEM, "Crafted with player heads instead of apples.", "Heals 4 hearts instead of 2."),
    BOWLESS(BowlessScenario.class, "Bowless", Material.BOW, "You can't craft bows."),
    RODLESS(RodlessScenario.class, "Rodless", Material.FISHING_ROD, "You can't craft rods."),
    FIRELESS(FirelessScenario.class, "Fireless", Material.FLINT_AND_STEEL, "You can't take fire damage."),
    HORSELESS(HorselessScenario.class, "Horseless", Material.SADDLE, "You can't tame horses."),
    RANDOM_DROPS(RandomDropsScenario.class, "Random Drops", Material.NOTE_BLOCK, "Every block drops a randomized item.", "Drop loot tables reset every 5 minutes.", "Logs and stone will always drop."),
    FLOWER_POWER(FlowerPowerScenario.class, "Flower Power", Material.RED_ROSE, "Flowers drop items for brewing."),
    MOB_EGGS(MobEggsScenario.class, "Mob Eggs", Material.EGG, "A random mob will spawn where an egg lands.", "Chickens have a 25% egg drop rate."),
    NETHER(NetherScenario.class, "Nether", Material.NETHERRACK, "Allows travel to the nether."),
    DRAGON_RUSH(DragonRushScenario.class, "Dragon Rush", Material.DRAGON_EGG, "First to kill the dragon wins!", "End portal spawns at 0 0."),
    CENTER_SPAWN(CenterSpawnScenario.class, "Center Spawn", Material.BED, "Player start and respawn at 0 0.", "Players will respawn at their bed if set."),
    NINE_SLOT(NineSlotsScenario.class, "Nine Slot", Material.CHEST, "You can only store items in your hotbar."),
    CYCLE_GRACE(CycleGraceScenario.class, "Cycle Grace", Material.GOLD_SWORD, "You can only PvP every 10 minutes."),
    ;

    private final Module module;
    private final String name;
    private final Material material;
    private final String[] description;
    private boolean enabled;

    @SuppressWarnings("deprecation")
    UHCScenario(Class<? extends Module> moduleClass, String name, Material material, String... description) {
        Module module;

        try {
            module = moduleClass.newInstance();
        } catch (InstantiationException | IllegalAccessException ignored) {
            module = null; // won't happen
        }

        this.module = module;
        this.name = name;
        this.material = material;
        this.description = description;
        this.enabled = false;
    }

    public Module getModule() {
        return this.module;
    }

    public String getName() {
        return this.name;
    }

    public ItemStack getScenarioItem() {
        ItemCrafter itemCrafter = new ItemCrafter(this.material)
            .name("" + (this.enabled ? ChatColor.GREEN : ChatColor.RED) + ChatColor.BOLD + this.name)
            .command("scenario toggle " + this.name(), true)
            .addTag("gui");

        for (String desc : this.description) itemCrafter.lore("- " + desc);

        return itemCrafter.craft();
    }

    public ItemStack getToggleItem() {
        ItemStack dye = new ItemStack(
            Material.INK_SACK,
            1,
            (this.enabled ? DyeColor.LIME : DyeColor.GRAY).getDyeData()
        );

        return new ItemCrafter(dye)
            .name((this.enabled ? ChatColor.RED + "Disable " : ChatColor.GREEN + "Enable ") + this.name)
            .command("scenario toggle " + this.name(), true)
            .addTag("gui")
            .craft();
    }

    public boolean isEnabled() {
        return this.enabled;
    }

    public void toggle() {
        if (this.enabled) this.module.unregister();
        else this.module.register();

        this.enabled = !this.enabled;
    }

}
