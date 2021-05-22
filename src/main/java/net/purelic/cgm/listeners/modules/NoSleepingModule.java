package net.purelic.cgm.listeners.modules;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerBedEnterEvent;

public class NoSleepingModule implements Listener {

    @EventHandler
    public void onPlayerBedEnter(PlayerBedEnterEvent event) {
        event.setCancelled(true);

//        Player player = event.getPlayer();
//
//        if (!MatchState.isState(MatchState.STARTED)
//            || !MatchManager.isPlaying(player)
//            || MatchManager.getParticipant(player).isDead()
//            || EnumSetting.GAME_TYPE.is(GameType.BED_WARS)) {
//            event.setCancelled(true);
//        }
    }

}
