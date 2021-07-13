package net.purelic.cgm.listeners.modules;

import net.purelic.cgm.core.constants.MatchState;
import net.purelic.cgm.core.gamemodes.ToggleSetting;
import net.purelic.cgm.core.managers.MatchManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.FoodLevelChangeEvent;

public class NoHungerModule implements Listener {

    @EventHandler
    public void onFoodLevelChange(FoodLevelChangeEvent event) {
        Player player = ((Player) event.getEntity()).getPlayer();

        if (MatchState.isState(MatchState.STARTED)
            && MatchManager.isPlaying(player)
            && MatchManager.getParticipant(player).isAlive()
            && ToggleSetting.HUNGER.isEnabled()) {
            return;
        }

        event.setCancelled(true);
    }

}
