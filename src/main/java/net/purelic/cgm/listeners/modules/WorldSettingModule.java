package net.purelic.cgm.listeners.modules;

import net.purelic.cgm.core.gamemodes.EnumSetting;
import net.purelic.cgm.core.gamemodes.ToggleSetting;
import net.purelic.cgm.core.gamemodes.constants.GameType;
import net.purelic.cgm.events.match.MatchEndEvent;
import net.purelic.cgm.events.match.MatchStartEvent;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

public class WorldSettingModule implements Listener {

    @EventHandler(priority = EventPriority.MONITOR)
    public void onMatchStart(MatchStartEvent event) {
        World world = event.getMap().getWorld();
        world.setGameRuleValue("doMobSpawning", "" + ToggleSetting.MOB_SPAWNING.isEnabled());
        world.setGameRuleValue("mobGriefing", "" + (ToggleSetting.MOB_SPAWNING.isEnabled() || EnumSetting.GAME_TYPE.is(GameType.BED_WARS))); // this will also disable fireballs
        world.setGameRuleValue("doFireTick", "" + ToggleSetting.FIRE_SPREAD.isEnabled());
        world.setGameRuleValue("doTileDrops", "" + ToggleSetting.BLOCK_DROPS.isEnabled());
        world.setGameRuleValue("doMobLoot", "" + ToggleSetting.ENTITY_DROPS.isEnabled());
        world.setGameRuleValue("doEntityDrops", "" + ToggleSetting.ENTITY_DROPS.isEnabled());
        world.setGameRuleValue("doDaylightCycle", "" + ToggleSetting.DAYLIGHT_CYCLE.isEnabled());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onMatchEnd(MatchEndEvent event) {
        World world = event.getMap().getWorld();
        world.setGameRuleValue("doMobSpawning", "" + false);
        world.setGameRuleValue("mobGriefing", "" + false);
        world.setGameRuleValue("doMobLoot", "" + false);
        world.setGameRuleValue("doFireTick", "" + false);
        world.setGameRuleValue("doTileDrops", "" + false);
        world.setGameRuleValue("doEntityDrops", "" + false);
        world.setGameRuleValue("doDaylightCycle", "" + false);
    }

}
