package net.purelic.cgm.listeners.modules;

import net.purelic.cgm.core.gamemodes.EnumSetting;
import net.purelic.cgm.core.gamemodes.constants.GameType;
import net.purelic.cgm.events.match.MatchStartEvent;
import org.bukkit.Difficulty;
import org.bukkit.World;
import org.bukkit.event.EventHandler;

public class UHCWorldModule implements DynamicModule {

    @EventHandler
    public void onMatchStart(MatchStartEvent event) {
        World world = event.getMap().getWorld();

        // When a UHC world is loaded it's set to peaceful to improve
        // performance when loading a potentially large world. So, when
        // the match starts we want to set the difficulty back to hard.
        world.setDifficulty(Difficulty.HARD);
    }

    @Override
    public boolean isValid() {
        return EnumSetting.GAME_TYPE.is(GameType.UHC);
    }

}
