package net.purelic.cgm.utils;

import net.purelic.cgm.core.constants.MatchState;
import net.purelic.cgm.core.constants.MatchTeam;
import net.purelic.cgm.core.gamemodes.EnumSetting;
import net.purelic.cgm.core.gamemodes.constants.GameType;
import net.purelic.cgm.core.managers.MatchManager;
import net.purelic.cgm.core.maps.CustomMap;
import net.purelic.cgm.core.maps.MapYaml;
import net.purelic.cgm.core.maps.SpawnPoint;
import net.purelic.cgm.core.match.Participant;
import net.purelic.cgm.listeners.modules.NoSleepingModule;
import net.purelic.cgm.uhc.UHCScenario;
import net.purelic.commons.Commons;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class SpawnUtils {

    private static final Random RANDOM = new Random();

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

            Location obs = currentMap.getYaml().getObsSpawn().getLocation(currentMap.getWorld());
            Block block = obs.getBlock();

            if (block.getType() != Material.AIR) {
                while (block.getType() != Material.AIR) {
                    block = block.getLocation().clone().add(0, 1, 0).getBlock();
                }

                player.teleport(block.getLocation());
            } else {
                player.teleport(obs);
            }

            if (currentMap.getYaml().hasNightVision()) {
                PlayerUtils.addPermanentEffect(player, PotionEffectType.NIGHT_VISION);
            }
        } else {
            player.teleport(Commons.getLobby().getSpawnLocation());
        }

        // PacketUtils.removeBorder(player);
    }

    public static void teleportRandom(Player player, boolean initialSpawn) {
        // When it's UHC we spread players vs using respawn points.
        // For initial spawn/match start we do a massive spread of everyone so that's why we skip it here.
        if (EnumSetting.GAME_TYPE.is(GameType.UHC)) {
            Location bedLocation = NoSleepingModule.getBedSpawn(player);

            if (bedLocation != null) {
                player.teleport(bedLocation);
            } else {
                if (!initialSpawn) spread(Collections.singletonList(MatchManager.getParticipant(player)));
            }

            return;
        }

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

    public static void spread(Collection<Participant> participants) {
        CustomMap current = MatchManager.getCurrentMap();
        int border = (int) (current.getWorld().getWorldBorder().getSize() / 2) - 10;

        if (UHCScenario.CENTER_SPAWN.isEnabled()) {
            border = 10;
        }

        for (MatchTeam team : MatchTeam.values()) {
            int x = RANDOM.nextInt(border * 2) - border;
            int z = RANDOM.nextInt(border * 2) - border;

            for (Player player : team.getPlayers()) {
                // randomize x and y for every player if the team is SOLO/FFA
                if (team == MatchTeam.SOLO) {
                    x = RANDOM.nextInt(border * 2) - border;
                    z = RANDOM.nextInt(border * 2) - border;
                }

                Participant participant = MatchManager.getParticipant(player);
                if (participants.contains(participant)) {
                    player.teleport(MatchManager.getCurrentMap()
                        .getWorld()
                        .getHighestBlockAt(x, z)
                        .getLocation().clone().add(0.5, 1, 0.5));
                }
            }
        }
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
