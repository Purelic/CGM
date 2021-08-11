package net.purelic.cgm.listeners.modules;

import net.purelic.cgm.core.managers.MatchManager;
import net.purelic.cgm.core.match.Participant;
import net.purelic.cgm.events.match.MatchEndEvent;
import net.purelic.cgm.events.participant.ParticipantDeathEvent;
import net.purelic.cgm.utils.EntityUtils;
import net.purelic.commons.Commons;
import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockCanBuildEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.util.EulerAngle;

import java.util.HashMap;
import java.util.Map;

public class RespawnModule implements Listener {

    private static final Map<ArmorStand, Player> stands = new HashMap<>();

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();

        // "cancel" the death event immediately
        player.setMaxHealth(20);
        player.setHealth(20);

        Participant participant = MatchManager.getParticipant(player);
        Commons.callEvent(new ParticipantDeathEvent(participant, player.getKiller(), event));
    }

    @EventHandler
    public void onEntityDamageEvent(EntityDamageEvent event) {
        // Disallow players who are dead to take damage
        if (event.getEntity() instanceof Player) {
            Player player = (Player) event.getEntity();
            Participant participant = MatchManager.getParticipant(player);
            if (participant != null && participant.isDead()) event.setCancelled(true);
        }

        if (event.getEntity() instanceof ArmorStand) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        // Disallow players who are dead to deal damage
        if (event.getDamager() instanceof Player) {
            Player player = (Player) event.getDamager();
            Participant participant = MatchManager.getParticipant(player);
            if (participant != null && participant.isDead()) event.setCancelled(true);
        }
    }

    @EventHandler
    public void onBlockCanBuild(BlockCanBuildEvent event) {
        event.setBuildable(true); // fixes respawn armor stands from blocking block placement
    }

    @EventHandler
    public void onMatchEnd(MatchEndEvent event) {
        for (ArmorStand stand : stands.keySet()) {
            stand.remove();
        }

        stands.clear();
    }

    private static ArmorStand getStand(Location location) {
        ArmorStand stand = location.getWorld().spawn(location.clone().add(0, Integer.MAX_VALUE, 0), ArmorStand.class);
        stand.setVisible(false);
        stand.setGravity(false);
        stand.setBasePlate(false);
        stand.setArms(false);
        stand.setSmall(true);
        stand.setLeftLegPose(new EulerAngle(3.14159265D, 0.0D, 0.0D));
        stand.setRightLegPose(new EulerAngle(3.14159265D, 0.0D, 0.0D));
        stand.setHeadPose(new EulerAngle(3.14159265D, 0.0D, 0.0D));
        stand.setCanPickupItems(false);
        stand.setMarker(true);
        return stand;
    }

    public static ArmorStand getStand(Player player) {
        for (Map.Entry<ArmorStand, Player> stand : stands.entrySet()) {
            if (stand.getValue() == null) {
                stands.put(stand.getKey(), player);
                return stand.getKey();
            }
        }

        ArmorStand stand = getStand(player.getLocation());
        stands.put(stand, player);
        return stand;
    }

    public static void removePlayer(Player player, ArmorStand stand) {
        EntityUtils.teleportEntity(player, stand, stand.getLocation().clone().add(0, Integer.MAX_VALUE, 0));
        stands.put(stand, null);
    }

}
