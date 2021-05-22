package net.purelic.cgm.utils;

import net.md_5.bungee.api.ChatColor;
import net.purelic.cgm.core.constants.MatchTeam;
import net.purelic.cgm.core.gamemodes.EnumSetting;
import net.purelic.cgm.core.gamemodes.NumberSetting;
import net.purelic.cgm.core.gamemodes.ToggleSetting;
import net.purelic.cgm.core.gamemodes.constants.GameType;
import net.purelic.cgm.core.managers.MatchManager;
import net.purelic.cgm.core.maps.flag.Flag;
import net.purelic.cgm.core.maps.flag.constants.FlagState;
import net.purelic.cgm.core.maps.flag.events.FlagsCollectedEvent;
import net.purelic.cgm.core.maps.hill.Hill;
import net.purelic.cgm.core.match.Participant;
import net.purelic.cgm.core.rewards.Medal;
import net.purelic.cgm.core.rewards.RewardBuilder;
import net.purelic.commons.Commons;
import net.purelic.commons.utils.CommandUtils;
import net.purelic.commons.utils.VersionUtils;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.github.paperspigot.Title;

import java.util.*;
import java.util.stream.Collectors;

public class FlagUtils {

    public static final String FLAG_ICON = "\u2691"; // ⚑
    private static final int DROP_LOCATION_CHECK_LIMIT = 10;

    public static List<Flag> getFlags() {
        return MatchManager.getCurrentMap().getLoadedFlags();
    }

    public static boolean hasCarrier(MatchTeam team) {
        return team.getPlayers().stream()
            .anyMatch(player -> isCarrier(MatchManager.getParticipant(player)));
    }

    public static boolean isCarrier(Player player) {
        Participant participant = MatchManager.getParticipant(player);
        return participant != null && isCarrier(participant);
    }

    public static boolean isCarrier(Participant participant) {
        if (!EnumSetting.GAME_TYPE.is(GameType.CAPTURE_THE_FLAG)) return false;
        return FlagUtils.getCarriers().containsKey(participant);
    }

    public static Map<Participant, Flag> getCarriers() {
        Map<Participant, Flag> carriers = new HashMap<>();
        MatchManager.getCurrentMap().getLoadedFlags().stream().filter(Flag::hasCarrier).forEach(flag -> carriers.put(flag.getCarrier(), flag));
        return carriers;
    }

    public static boolean isTeammateCarrier(Player player) {
        return getCarriers().keySet().stream()
            .anyMatch(participant -> MatchTeam.isSameTeam(participant.getPlayer(), player));
    }

    public static boolean respawnOnDrop(Player player) {
        return ToggleSetting.RESPAWN_ON_DROP.isEnabled()
            && MatchManager.getParticipant(player).isDead()
            && FlagUtils.isTeammateCarrier(player);
    }

    public static void teleportToCarrier(Player player) {
        Optional<Participant> optionalCarrier = getCarriers().keySet().stream().findFirst();
        if (optionalCarrier.isPresent()) player.teleport(optionalCarrier.get().getPlayer());
        else SpawnUtils.teleportObsSpawn(player);
    }

    public static boolean canPlace(Flag flag, Block block) {
        return canPlace(flag, block.getLocation());
    }

    public static boolean canPlace(Flag flag, Location location) {
        return MatchManager.getCurrentMap().getLoadedFlags().stream()
            .filter(Flag::isActive)
            .noneMatch(f ->
                (f != flag && f.getHome().getBlock().getLocation().equals(location))
                    || (!f.hasCarrier() && f.getLocation().getBlock().getLocation().equals(location)));
    }

    public static void captureFlag(Participant participant) {
        Flag flag = FlagUtils.getCarriers().get(participant);

        if (flag != null) {
            if (ToggleSetting.FLAG_AT_HOME.isEnabled()
                    && FlagUtils.getFlags().stream()
                        .anyMatch(f -> !f.isNeutral()
                                && f.getOwner() == participant.getTeam()
                                && !f.isState(FlagState.RETURNED, FlagState.RESPAWNING))) {
                return;
            } else if (NumberSetting.FLAG_COLLECTION_INTERVAL.value() > 0) {
                return;
            }

            flag.setState(FlagState.CAPTURED);

            Player player = participant.getPlayer();
            new RewardBuilder(player, 1, "Flag Captured").addMedals(FlagUtils.getMedals(player)).reward();
            participant.addScore(1);
            participant.getStats().addFlag();
        }
    }

    public static void scoreCarrierPoints() {
        int carrierPoints = NumberSetting.FLAG_CARRIER_POINTS.value();

        if (carrierPoints == 0) return;

        FlagUtils.getFlags().stream()
                .filter(Flag::hasCarrier)
                .forEach(flag -> flag.getCarrier().addScore(carrierPoints));
    }

    public static void collectFlags() {
        Map<MatchTeam, Set<Flag>> collected = new HashMap<>();

        HillUtils.getHills().forEach(hill -> {
            MatchTeam team = hill.getControlledBy();
            Set<Flag> flags = hill.getCollectedFlags();
            collected.putIfAbsent(team, new HashSet<>());
            collected.get(team).addAll(flags);
        });

        collected.forEach((team, flags) -> Commons.callEvent(new FlagsCollectedEvent(team, flags)));
    }

    public static Hill getHill(Flag flag) {
        return HillUtils.getHills().stream()
                .filter(hill ->
                        hill.isInside(flag.hasCarrier() ? flag.getCarrier().getPlayer().getLocation() : flag.getLocation())
                                && hill.getControlledBy() != flag.getOwner())
                .findFirst().orElse(null);
    }

    public static boolean inFlagProximity(MatchTeam owner, Player player, double dist) {
        if (ToggleSetting.NEUTRAL_FLAGS.isEnabled()) return false;
        return FlagUtils.getFlags().stream()
                .filter(flag -> flag.getOwner() == owner)
                .anyMatch(flag -> flag.getLocation().distance(player.getLocation()) <= dist);
    }

    public static boolean inGoalProximity(MatchTeam owner, Player player, double dist) {
        if (!ToggleSetting.FLAG_GOALS.isEnabled()) return false;
        return HillUtils.getHills().stream()
                .filter(Hill::isActive)
                .filter(hill -> hill.getCapturedByTeam() == owner)
                .anyMatch(hill -> hill.getCenter().distance(player.getLocation()) <= dist);
    }

    public static Flag getClosestFlag(Player player) {
        List<Flag> activeFlags = getFlags().stream().filter(Flag::isActive).collect(Collectors.toList());
        MatchTeam team = MatchTeam.getTeam(player);
        Flag target = null;
        double dist = Double.MAX_VALUE;

        for (Flag flag : activeFlags) {
            if (!flag.isNeutral() && flag.getOwner() == team) {
                continue;
            }

            Location loc = getCurrentFlagLocation(flag);
            double tempDist = player.getLocation().distance(loc);

            if (tempDist > dist) continue;

            dist = tempDist;
            target = flag;
        }

        return target;
    }

    public static Location getCurrentFlagLocation(Flag flag) {
        return flag.hasCarrier()
                ? flag.getCarrier().getPlayer().getLocation()
                : flag.isState(FlagState.RESPAWNING) ? flag.getHome() : flag.getLocation();
    }

    private static List<Medal> getMedals(Player player) {
        List<Medal> medals = new ArrayList<>();

        if (player.getHealth() <= 3) medals.add(Medal.CLOSE_CAPTURE);

        return medals;
    }

    public static Flag getRandomFlag() {
        List<Flag> flags = getFlags();
        return flags.get(new Random().nextInt(flags.size()));
    }

    public static void sendRespawnOnDropMessage(Player player) {
        if (VersionUtils.isLegacy(player)) {
            CommandUtils.sendAlertMessage(player, "You will respawn when the flag is dropped!");
        } else {
            player.sendTitle(new Title(
                ChatColor.RED + "You died!",
                "You'll respawn when the flag is dropped!",
                5,
                3 * 20,
                5));
        }
    }

    public static Location getDropLocation(Flag flag, Location location) {
        return getDropLocation(flag, location, 0);
    }

    public static Location getDropLocation(Flag flag, Location location, boolean fullCheck) {
        int y = location.getBlockY();
        int minY = fullCheck ? 0 : Math.min(Math.max(y - DROP_LOCATION_CHECK_LIMIT, 0), 255);
        return getDropLocation(flag, location, minY);
    }

    private static Location getDropLocation(Flag flag, Location location, int minY) {
        World world = location.getWorld();
        int blockX = location.getBlockX();
        int blockY = location.getBlockY();
        int blockZ = location.getBlockZ();
        float yaw = location.getYaw();

        Location dropLocation = null;

        if (location.getBlock().isLiquid()) { // go up until we find an air block above liquid
            for (int i = blockY; i <= 255; i++) {
                Block block = world.getBlockAt(blockX, i, blockZ);

                if (block.isEmpty() && FlagUtils.canPlace(flag, block)) { // this will also go through solid blocks over a liquid
                    dropLocation = block.getLocation();
                    break;
                }
            }
        } else { // go down until we find a solid block
            for (int i = blockY; i >= minY; i--) {
                Block block = world.getBlockAt(blockX, i, blockZ);

                if ((block.isLiquid() || !block.isEmpty())
                    && FlagUtils.canPlace(flag, block)
                    && block.getType() != null
                    && !block.getType().name().contains("_BUTTON")
                ) {
                    dropLocation = block.getLocation().add(0, 1, 0);
                    break;
                }
            }
        }

        if (dropLocation != null) {
            if (!canPlace(flag, dropLocation)) return null;
            dropLocation.setYaw(yaw);
        }

        return dropLocation;
    }

}
