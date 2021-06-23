package net.purelic.cgm.listeners.shop;

import net.purelic.cgm.CGM;
import net.purelic.cgm.core.constants.MatchTeam;
import net.purelic.cgm.core.gamemodes.EnumSetting;
import net.purelic.cgm.core.gamemodes.constants.GameType;
import net.purelic.cgm.core.gamemodes.constants.TeamType;
import net.purelic.cgm.core.managers.ShopManager;
import net.purelic.cgm.core.maps.shop.constants.TeamUpgrade;
import net.purelic.cgm.core.maps.shop.events.ShopItemPurchaseEvent;
import net.purelic.cgm.core.maps.shop.events.TeamUpgradePurchaseEvent;
import net.purelic.cgm.core.maps.shop.events.TrapPurchaseEvent;
import net.purelic.cgm.events.match.MatchEndEvent;
import net.purelic.cgm.events.match.MatchStartEvent;
import net.purelic.cgm.events.participant.ParticipantRespawnEvent;
import net.purelic.commons.Commons;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitRunnable;

public class TeamUpgradePurchase implements Listener {

    @EventHandler
    private void onTeamUpgradePurchase(TeamUpgradePurchaseEvent event) {
        Player player = event.getPlayer();
        MatchTeam team = event.getTeam();
        TeamUpgrade upgrade = event.getUpgrade();

        ShopManager.addUpgrade(player, upgrade);
        event.broadcast(team);

        if (EnumSetting.TEAM_TYPE.is(TeamType.SOLO)) TeamUpgrade.applyUpgrades(player);
        else TeamUpgrade.applyUpgrades(team);

        if ((upgrade == TeamUpgrade.TRAP_I || upgrade == TeamUpgrade.TRAP_II)
                && EnumSetting.GAME_TYPE.is(GameType.BED_WARS)) {
            Commons.callEvent(new TrapPurchaseEvent(player, upgrade));
        }
    }

    @EventHandler
    private void onShopItemPurchase(ShopItemPurchaseEvent event) {
        TeamUpgrade.applyUpgrades(event.getPlayer());
    }

    @EventHandler (priority = EventPriority.HIGHEST)
    private void onParticipantRespawn(ParticipantRespawnEvent event) {
        new BukkitRunnable() {
            @Override
            public void run() {
                TeamUpgrade.applyUpgrades(event.getPlayer());
            }
        }.runTask(CGM.get());
    }

    @EventHandler (priority = EventPriority.LOW)
    private void onMatchStartEvent(MatchStartEvent event) {
        ShopManager.clearUpgrades();
    }

    @EventHandler
    private void onMatchEndEvent(MatchEndEvent event) {
        ShopManager.clearUpgrades();
    }

}
