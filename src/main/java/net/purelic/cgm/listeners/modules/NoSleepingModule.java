package net.purelic.cgm.listeners.modules;

import net.purelic.cgm.core.gamemodes.EnumSetting;
import net.purelic.cgm.core.gamemodes.constants.GameType;
import net.purelic.cgm.core.managers.MatchManager;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerBedEnterEvent;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class NoSleepingModule implements Listener {

    public static final Map<UUID, Location> BED_SPAWNS = new HashMap<>();

    @EventHandler
    public void onPlayerBedEnter(PlayerBedEnterEvent event) {
        event.setCancelled(true);
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getAction() == Action.RIGHT_CLICK_BLOCK && event.getClickedBlock().getType() == Material.BED_BLOCK && EnumSetting.GAME_TYPE.is(GameType.UHC)) {
            BED_SPAWNS.put(event.getPlayer().getUniqueId(), event.getClickedBlock().getLocation());
            event.getPlayer().sendMessage("Respawn point set.");
        }
    }

    public static Location getBedSpawn(Player player) {
        UUID uuid = player.getUniqueId();

        if (BED_SPAWNS.containsKey(uuid)) {
            Location location = BED_SPAWNS.get(uuid);

            if (MatchManager.getCurrentMap() == null || location.getWorld() != MatchManager.getCurrentMap().getWorld()) {
                // bed spawn was set in a previous map/world
                BED_SPAWNS.remove(uuid);
                return null;
            } else {
                Block block = location.getBlock();

                if (block.getType() != Material.BED_BLOCK) {
                    // bed was destroyed
                    BED_SPAWNS.remove(uuid);
                    return null;
                } else {
                    return location;
                }
            }
        } else {
            // bed spawn hasn't been set
            return null;
        }
    }

}
