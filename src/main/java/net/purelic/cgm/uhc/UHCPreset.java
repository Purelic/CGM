package net.purelic.cgm.uhc;

import net.purelic.commons.utils.ItemCrafter;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.List;

public enum UHCPreset {

    RESET("Reset", Material.BARRIER),
    SPEED("Speed", Material.GOLD_PICKAXE, UHCScenario.CUT_CLEAN, UHCScenario.GOLDEN_HEADS, UHCScenario.BOWLESS, UHCScenario.RODLESS, UHCScenario.FIRELESS, UHCScenario.HORSELESS),
    PARTY("Party", Material.FIREWORK, UHCScenario.CUT_CLEAN, UHCScenario.GOLDEN_HEADS, UHCScenario.RANDOM_DROPS, UHCScenario.MOB_EGGS),
    MAN_HUNT("Man Hunt", Material.EYE_OF_ENDER, UHCScenario.NETHER, UHCScenario.DRAGON_RUSH, UHCScenario.CENTER_SPAWN),
    ;

    private final String name;
    private final Material material;
    private final List<UHCScenario> scenarios;

    UHCPreset(String name, Material material, UHCScenario... scenarios) {
        this.name = name;
        this.material = material;
        this.scenarios = Arrays.asList(scenarios);
    }

    public String getName() {
        return this.name;
    }

    public ItemStack getItem() {
        ItemCrafter itemCrafter = new ItemCrafter(this.material)
            .name("" + ChatColor.AQUA + ChatColor.BOLD + this.name)
            .command("scenario preset " + this.name(), true)
            .addTag("gui");

        for (UHCScenario scenario : this.scenarios) {
            ChatColor color = scenario.isEnabled() ? ChatColor.GREEN : ChatColor.RED;
            itemCrafter.lore("- " + color + scenario.getName());
        }

        itemCrafter.lore().lore("Click to apply preset!");

        return itemCrafter.craft();
    }

    public void apply() {
        for (UHCScenario scenario : UHCScenario.values()) {
            if ((this.scenarios.contains(scenario) && !scenario.isEnabled())
                || (!this.scenarios.contains(scenario) && scenario.isEnabled())) {
                scenario.toggle();
            }
        }
    }

}
