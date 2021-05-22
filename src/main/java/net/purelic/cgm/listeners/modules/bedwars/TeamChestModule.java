package net.purelic.cgm.listeners.modules.bedwars;

import net.purelic.cgm.core.constants.MatchState;
import net.purelic.cgm.core.constants.MatchTeam;
import net.purelic.cgm.core.gamemodes.EnumSetting;
import net.purelic.cgm.core.gamemodes.constants.GameType;
import net.purelic.cgm.core.gamemodes.constants.TeamType;
import net.purelic.cgm.core.managers.ShopManager;
import net.purelic.cgm.events.match.MatchStartEvent;
import net.purelic.cgm.events.match.RoundEndEvent;
import net.purelic.cgm.events.participant.MatchTeamEliminateEvent;
import net.purelic.cgm.utils.SpawnUtils;
import net.purelic.commons.utils.CommandUtils;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;

public class TeamChestModule implements Listener {

    private final double distance = 20;
    private final Map<Inventory, MatchTeam> chests = new HashMap<>();

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (!EnumSetting.GAME_TYPE.is(GameType.BED_WARS) || !MatchState.isState(MatchState.STARTED)) return;

        Block block = event.getClickedBlock();
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK
                || block == null
                || block.getType() != Material.CHEST) return;

        Chest chest = (Chest) block.getState();
        Inventory inventory = chest.getInventory();
        Player player = event.getPlayer();
        MatchTeam team = MatchTeam.getTeam(player);
        Location spawnLoc = SpawnUtils.getInitialSpawn(player);
        Location playerLoc = player.getLocation();

        if (this.chests.containsKey(inventory)) {
            MatchTeam owner = this.chests.get(inventory);

            if (owner != team) {
                CommandUtils.sendErrorMessage(player, "You can only open your team's chest!");
                event.setCancelled(true);
            }

            return;
        }

        if (spawnLoc.distance(playerLoc) >= this.distance) {
            CommandUtils.sendErrorMessage(player, "You can only open your team's chest!");
            event.setCancelled(true);
            return;
        }

        this.chests.put(inventory, team);
    }

    @EventHandler
    public void onInventoryOpen(InventoryOpenEvent event) {
        if (!EnumSetting.GAME_TYPE.is(GameType.BED_WARS)) return;

        Inventory inventory = event.getInventory();

        if (ShopManager.isShop(inventory)) return;

        InventoryType type = inventory.getType();

        if (type != InventoryType.CHEST || EnumSetting.TEAM_TYPE.is(TeamType.SOLO)) {
            if (type == InventoryType.PLAYER || type == InventoryType.ENDER_CHEST) {
                return;
            }

            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onMatchStart(MatchStartEvent event) {
        this.chests.keySet().forEach(Inventory::clear);
        this.chests.clear();
    }

    @EventHandler
    public void onRoundEnd(RoundEndEvent event) {
        this.chests.keySet().forEach(Inventory::clear);
        this.chests.clear();
    }

    @EventHandler
    public void onMatchTeamEliminate(MatchTeamEliminateEvent event) {
        if (!EnumSetting.GAME_TYPE.is(GameType.BED_WARS)) return;

        MatchTeam team = event.getTeam();
        Location location = SpawnUtils.getInitialSpawn(team);
        World world = location.getWorld();

        this.chests.forEach((inv, owner) -> {
            if (owner == team) {
                for (ItemStack item : inv.getContents()) {
                    if (item == null) continue;
                    world.dropItemNaturally(location.clone().add(0, 0.5, 0), item);
                }

                inv.clear();
            }
        });
    }

}
