package net.purelic.cgm.uhc;

import net.purelic.cgm.uhc.scenarios.CutCleanScenario;
import net.purelic.cgm.uhc.scenarios.GoldenHeadScenario;
import net.purelic.commons.modules.Module;
import net.purelic.commons.utils.ItemCrafter;
import org.bukkit.ChatColor;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public enum UHCScenario {

    CUT_CLEAN(CutCleanScenario.class, "Cut Clean", Material.COOKED_BEEF, "Ore and animal drops are smelted.", "Apple rates are 5%.", "Flint rates are 50%."),
    GOLDEN_HEADS(GoldenHeadScenario.class, "Golden Heads", Material.SKULL_ITEM, "Crafted with player heads instead of apples.", "Heals 4 hearts instead of 2."),
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
