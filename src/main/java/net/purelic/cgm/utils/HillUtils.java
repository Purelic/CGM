package net.purelic.cgm.utils;

import net.purelic.cgm.core.constants.MatchTeam;
import net.purelic.cgm.core.managers.MatchManager;
import net.purelic.cgm.core.maps.hill.Hill;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HillUtils {

    public static List<Hill> getHills() {
        return MatchManager.getCurrentMap().getLoadedHills();
    }

    public static boolean hasCaptured(MatchTeam team) {
        return getHills().stream().anyMatch(hill -> hill.getCapturedBy() == team && hill.getOwner() != team);
    }

    public static boolean hasCaptured(Player player) {
        return HillUtils.getCapturedHills().values().stream().anyMatch(hill -> hill.getPlayers().contains(player));
    }

    public static Map<MatchTeam, Hill> getCapturedHills() {
        Map<MatchTeam, Hill> captured = new HashMap<>();
        MatchManager.getCurrentMap().getLoadedHills().stream().filter(Hill::isCaptured).forEach(hill -> captured.put(hill.getCapturedBy(), hill));
        return captured;
    }

    public static Hill getClosestHill(Player player) {
        MatchTeam team = MatchTeam.getTeam(player);
        Hill target = null;
        double dist = Double.MAX_VALUE;

        for (Hill hill : getHills()) {
            if ((!hill.isNeutral() && hill.getControlledBy() == team)
                || !hill.isActive()) {
                continue;
            }

            double tempDist = player.getLocation().distance(hill.getCenter());

            if (tempDist > dist) continue;

            dist = tempDist;
            target = hill;
        }

        return target;
    }

}
