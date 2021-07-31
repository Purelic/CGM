package net.purelic.cgm.listeners;

import net.purelic.cgm.commands.toggles.ToggleFriendlyFireCommand;
import net.purelic.cgm.core.constants.MatchState;
import net.purelic.cgm.core.constants.MatchTeam;
import net.purelic.cgm.core.gamemodes.NumberSetting;
import net.purelic.cgm.core.gamemodes.ToggleSetting;
import net.purelic.cgm.core.managers.MatchManager;
import net.purelic.cgm.core.runnables.RoundCountdown;
import net.purelic.cgm.utils.FlagUtils;
import net.purelic.cgm.utils.SpawnUtils;
import net.purelic.commons.utils.TaskUtils;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;

public class EntityDamage implements Listener {

    @EventHandler
    public void onEntityDamage(EntityDamageEvent event) {
        boolean started = MatchState.isState(MatchState.STARTED);

        if (!(event.getEntity() instanceof Player)) {
            return;
        }

        Player player = (Player) event.getEntity();

        if (!started
            || TaskUtils.isRunning(RoundCountdown.getCountdown())
            || MatchTeam.getTeam(player) == MatchTeam.OBS) {
            event.setCancelled(true);
            player.setFireTicks(0);
        }

        if (event.getCause() == EntityDamageEvent.DamageCause.PROJECTILE) {
            if (ToggleSetting.PLAYER_IMMUNE_TO_PROJECTILES.isEnabled()) {
                event.setCancelled(true);
            }
        } else if (event.getCause() == EntityDamageEvent.DamageCause.ENTITY_ATTACK) {
            if (ToggleSetting.PLAYER_IMMUNE_TO_MELEE.isEnabled()) {
                event.setCancelled(true);
            }
        } else if (event.getCause() == EntityDamageEvent.DamageCause.FALL) {
            if (ToggleSetting.PLAYER_IMMUNE_TO_FALL_DAMAGE.isEnabled()) {
                event.setCancelled(true);
            }
        } else if (event.getCause() == EntityDamageEvent.DamageCause.VOID) {
            event.setDamage(200);

            boolean respawnOnDrop = FlagUtils.respawnOnDrop(player);

            if (!started
                || MatchTeam.getTeam(player) == MatchTeam.OBS
                || MatchManager.getParticipant(player).isEliminated()
                || respawnOnDrop
                || TaskUtils.isRunning(RoundCountdown.getCountdown())) {
                event.setCancelled(true);
                player.setFireTicks(0);

                if (respawnOnDrop) FlagUtils.teleportToCarrier(player);
                else SpawnUtils.teleportObsSpawn(player);
            }
        }
    }

    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (!MatchState.isState(MatchState.STARTED) || TaskUtils.isRunning(RoundCountdown.getCountdown())) {
            event.setCancelled(true);
            return;
        }

        if (event.getDamager() instanceof Player) {
            Player damager = (Player) event.getDamager();

            if (MatchTeam.getTeam(damager) == MatchTeam.OBS) {
                event.setCancelled(true);
                return;
            }
        }

        // Don't allow obs to do damage
        if (event.getDamager() instanceof Player && event.getEntity() instanceof Player) {
            Player damager = (Player) event.getDamager();
            MatchTeam damagerTeam = MatchTeam.getTeam(damager);
            Player player = (Player) event.getEntity();

            boolean friendlyFire = ToggleSetting.FRIENDLY_FIRE.isEnabled() || ToggleFriendlyFireCommand.friendlyFire;

            if (damagerTeam == MatchTeam.OBS || (!friendlyFire && MatchTeam.isSameTeam(player, damager))) {
                event.setCancelled(true);
                return;
            }

            // fix bug where punching can sometimes do a lot of damage
            if (damager.getItemInHand() == null) {
                event.setDamage(20);
            }

            if (MatchManager.isPlaying(damager) && FlagUtils.isCarrier(MatchManager.getParticipant(damager))) {
                event.setDamage(event.getDamage() * (NumberSetting.FLAG_CARRIER_MELEE_MODIFIER.value() / 100.0));
            }

            return;
        }

        // Don't allow obs to get damaged
        if (event.getEntity() instanceof Player) {
            Player player = (Player) event.getEntity();
            Entity damager = event.getDamager();

            if (MatchTeam.getTeam(player) == MatchTeam.OBS) event.setCancelled(true);

            // Don't allow players to shoot themselves
            if (damager instanceof Projectile) {
                Projectile proj = (Projectile) damager;

                if (player == proj.getShooter()) {
                    event.setCancelled(true);
                    proj.remove();
                }
            }
        }
    }

}
