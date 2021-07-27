package net.purelic.cgm.listeners.modules.bedwars;

import net.purelic.cgm.core.constants.MatchTeam;
import net.purelic.cgm.core.gamemodes.EnumSetting;
import net.purelic.cgm.core.gamemodes.constants.GameType;
import net.purelic.cgm.core.gamemodes.constants.TeamType;
import net.purelic.cgm.utils.SoundUtils;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;

public class EnderPearlModule implements Listener {

    private final Map<Player, Long> cooldowns = new HashMap<>();
    private final double cooldownTime = 0.5D;

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Action action = event.getAction();
        Player player = event.getPlayer();

        if (!action.name().contains("RIGHT_CLICK")) return;

        ItemStack inHand = player.getItemInHand();

        if (inHand == null
            || inHand.getType() != Material.ENDER_PEARL) return;

        if (this.cooldowns.containsKey(player)) {
            double timeLeft = (this.cooldowns.get(player) + this.cooldownTime * 1000L) - System.currentTimeMillis();
            if (timeLeft > 0) {
                event.setCancelled(true);
                player.updateInventory();
                return;
            }
        }

        this.cooldowns.put(player, System.currentTimeMillis());
    }

    @EventHandler
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        if (event.getCause() != PlayerTeleportEvent.TeleportCause.ENDER_PEARL
            || !EnumSetting.GAME_TYPE.is(GameType.BED_WARS)) return;

        MatchTeam playerTeam = MatchTeam.getTeam(event.getPlayer());
        TeamType teamType = EnumSetting.TEAM_TYPE.get();

        for (MatchTeam team : teamType.getTeams()) {
            if (team == playerTeam) continue;
            SoundUtils.SFX.ENDER_PEARL_TELEPORT.play(team);
        }

        SoundUtils.SFX.ENDER_PEARL_TELEPORT.play(MatchTeam.OBS);
    }

}
