package net.purelic.cgm.uhc;

import net.purelic.cgm.core.gamemodes.EnumSetting;
import net.purelic.cgm.core.gamemodes.ToggleSetting;
import net.purelic.cgm.core.gamemodes.constants.CompassTrackingType;
import net.purelic.cgm.core.gamemodes.constants.GameType;
import net.purelic.cgm.events.match.MatchEndEvent;
import net.purelic.cgm.events.match.MatchStartEvent;
import net.purelic.cgm.listeners.modules.DynamicModule;
import net.purelic.commons.utils.ItemCrafter;
import org.bukkit.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.PrepareItemCraftEvent;

public class UHCModule implements DynamicModule {

    @EventHandler
    public void onMatchStart(MatchStartEvent event) {
        World world = event.getMap().getWorld();

        // When a UHC world is loaded it's set to peaceful to improve
        // performance when loading a potentially large world. So, when
        // the match starts we want to set the difficulty back to hard.
        world.setDifficulty(Difficulty.HARD);
    }

    @EventHandler
    public void onCraftingPrepare(PrepareItemCraftEvent event) {
        if (ToggleSetting.PLAYER_COMPASS_ENABLED.isEnabled()
            && EnumSetting.PLAYER_COMPASS_TYPE.is(CompassTrackingType.PLAYER)
            && event.getRecipe().getResult().getType() == Material.COMPASS
        ) {
            event.getInventory().setResult(new ItemCrafter(Material.COMPASS)
                .name("" + ChatColor.RESET + ChatColor.BOLD + "Tracking Compass")
                .lore("R-Click to track the closest enemy!")
                .addTag("tracking_compass")
                .craft());
        }
    }

    @EventHandler
    public void onMatchEnd(MatchEndEvent event) {
        Bukkit.getServer().resetRecipes(); // removes any custom crafting recipes
    }

    @Override
    public boolean isValid() {
        return EnumSetting.GAME_TYPE.is(GameType.UHC);
    }

}
