package net.purelic.cgm.uhc;

import net.purelic.cgm.core.gamemodes.EnumSetting;
import net.purelic.cgm.core.gamemodes.constants.GameType;
import net.purelic.cgm.events.match.MatchEndEvent;
import net.purelic.cgm.events.match.MatchStartEvent;
import net.purelic.cgm.listeners.modules.DynamicModule;
import org.bukkit.Bukkit;
import org.bukkit.Difficulty;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerTeleportEvent;

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
    public void onMatchEnd(MatchEndEvent event) {
        Bukkit.getServer().resetRecipes(); // removes any custom crafting recipes
    }

    @EventHandler
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        boolean end = event.getCause() == PlayerTeleportEvent.TeleportCause.END_PORTAL;
        boolean nether = event.getCause() == PlayerTeleportEvent.TeleportCause.NETHER_PORTAL;

        if ((!UHCScenario.DRAGON_RUSH.isEnabled() && end) || (!UHCScenario.NETHER.isEnabled() && nether)) {
            event.setCancelled(true);
        }
    }

    @Override
    public boolean isValid() {
        return EnumSetting.GAME_TYPE.is(GameType.UHC);
    }

}
