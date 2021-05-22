package net.purelic.cgm.utils;

import net.purelic.cgm.core.constants.MatchState;
import net.purelic.cgm.core.constants.MatchTeam;
import net.purelic.cgm.core.managers.MatchManager;
import net.purelic.cgm.core.maps.CustomMap;
import net.purelic.cgm.core.maps.MapYaml;
import net.purelic.cgm.core.maps.SpawnPoint;
import net.purelic.cgm.core.match.Participant;
import net.purelic.commons.Commons;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;

import java.util.List;

public class SpawnUtils {

    public static void teleportObsSpawn(Player player) {
        if (player.getVehicle() != null) {
            player.getVehicle().eject();
        }

        if (MatchState.isActive() || MatchState.isState(MatchState.ENDED)) {
            CustomMap currentMap = MatchManager.getCurrentMap();
            player.setAllowFlight(true);
            player.setFlying(true);
            player.setSneaking(false);
            player.setSprinting(false);
            currentMap.getYaml().getObsSpawn().teleport(player, currentMap.getWorld());
            if (currentMap.getYaml().hasNightVision()) PlayerUtils.addPermanentEffect(player, PotionEffectType.NIGHT_VISION);
        } else {
            player.teleport(Commons.getLobby().getSpawnLocation());
        }

        // PacketUtils.removeBorder(player);
    }

    public static void teleportRandom(Player player, boolean initialSpawn) {
        MatchTeam team = MatchTeam.getTeam(player);
        MapYaml yaml = MatchManager.getCurrentMap().getYaml();

        if (team == MatchTeam.BLUE) teleportRandom(player, yaml.getBlueSpawns(), initialSpawn);
        else if (team == MatchTeam.RED) teleportRandom(player, yaml.getRedSpawns(), initialSpawn);
        else if (team == MatchTeam.GREEN) teleportRandom(player, yaml.getGreenSpawns(), initialSpawn);
        else if (team == MatchTeam.YELLOW) teleportRandom(player, yaml.getYellowSpawns(), initialSpawn);
        else if (team == MatchTeam.AQUA) teleportRandom(player, yaml.getAquaSpawns(), initialSpawn);
        else if (team == MatchTeam.PINK) teleportRandom(player, yaml.getPinkSpawns(), initialSpawn);
        else if (team == MatchTeam.GRAY) teleportRandom(player, yaml.getGraySpawns(), initialSpawn);
        else if (team == MatchTeam.WHITE) teleportRandom(player, yaml.getWhiteSpawns(), initialSpawn);
        else teleportRandom(player, yaml.getSoloSpawns(), false);
    }

    private static void teleportRandom(Player player, List<SpawnPoint> spawnPoints, boolean initialSpawn) {
        SpawnPoint spawn = spawnPoints.get(0);

        if (spawnPoints.size() > 1 && !initialSpawn) {
            double dist = Double.MIN_VALUE;

            for (SpawnPoint point : spawnPoints) {
                if (point == spawn) continue;

                double tempDist = Double.MAX_VALUE;

                for (Participant participant : MatchManager.getParticipants()) {
                    Player participantPlayer = participant.getPlayer();

                    if (participantPlayer == player
                        || MatchTeam.isSameTeam(player, participantPlayer)
                        || !participant.isAlive()) continue;

                    double playerDist = participantPlayer.getLocation().distance(point.getLocation(participantPlayer.getWorld()));
                    if (playerDist < tempDist) tempDist = playerDist;
                }

                if (tempDist > dist) {
                    dist = tempDist;
                    spawn = point;
                }
            }
        }

        spawn.teleport(player);
    }

    public static Location getInitialSpawn(Player player) {
        return getInitialSpawn(MatchTeam.getTeam(player));
    }

    public static Location getInitialSpawn(MatchTeam team) {
        return getInitialSpawnPoint(team).getLocation(MatchManager.getCurrentMap().getWorld());
    }

    private static SpawnPoint getInitialSpawnPoint(Player player) {
        return getInitialSpawnPoint(MatchTeam.getTeam(player));
    }

    private static SpawnPoint getInitialSpawnPoint(MatchTeam team) {
        MapYaml yaml = MatchManager.getCurrentMap().getYaml();

        if (team == MatchTeam.BLUE) return yaml.getBlueSpawns().get(0);
        else if (team == MatchTeam.RED) return yaml.getRedSpawns().get(0);
        else if (team == MatchTeam.GREEN) return yaml.getGreenSpawns().get(0);
        else if (team == MatchTeam.YELLOW) return yaml.getYellowSpawns().get(0);
        else if (team == MatchTeam.AQUA) return yaml.getAquaSpawns().get(0);
        else if (team == MatchTeam.PINK) return yaml.getPinkSpawns().get(0);
        else if (team == MatchTeam.GRAY) return yaml.getGraySpawns().get(0);
        else if (team == MatchTeam.WHITE) return yaml.getWhiteSpawns().get(0);
        else return yaml.getSoloSpawns().get(0);
    }

}
