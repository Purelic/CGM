package net.purelic.cgm.listeners.modules.bedwars;

import net.purelic.cgm.core.gamemodes.EnumSetting;
import net.purelic.cgm.core.gamemodes.constants.GameType;
import net.purelic.cgm.events.match.MatchJoinEvent;
import net.purelic.cgm.events.participant.ParticipantEliminateEvent;
import net.purelic.cgm.utils.SpawnUtils;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;

public class EnderChestModule implements Listener {

    @EventHandler(priority = EventPriority.LOW)
    public void onParticipantEliminate(ParticipantEliminateEvent event) {
        if (!EnumSetting.GAME_TYPE.is(GameType.BED_WARS)) return;

        Player player = event.getPlayer();
        Location location = SpawnUtils.getInitialSpawn(player);

        for (ItemStack item : player.getEnderChest()) {
            if (item == null) continue;
            player.getWorld().dropItemNaturally(location.clone().add(0, 0.5, 0), item);
        }

        player.getEnderChest().clear();
    }

    @EventHandler
    public void onMatchJoin(MatchJoinEvent event) {
        if (event.isFirstJoin()) event.getPlayer().getEnderChest().clear();
    }

}
