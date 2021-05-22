package net.purelic.cgm.utils;

import net.purelic.cgm.core.constants.MatchTeam;
import net.purelic.cgm.core.gamemodes.EnumSetting;
import net.purelic.cgm.core.gamemodes.constants.TeamType;
import net.purelic.cgm.core.managers.MatchManager;
import net.purelic.cgm.core.maps.hill.Hill;
import net.purelic.cgm.core.match.Participant;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HillUtils {

    public static List<Hill> getHills() {
        return MatchManager.getCurrentMap().getLoadedHills();
    }

    public static boolean hasCaptured(MatchTeam team) {
        return getHills().stream().anyMatch(hill -> hill.getCapturedByTeam() == team);
    }

    public static boolean hasCaptured(Participant participant) {
        return HillUtils.getCapturedHills().containsKey(participant);
    }

    public static Map<Participant, Hill> getCapturedHills() {
        Map<Participant, Hill> captured = new HashMap<>();
        MatchManager.getCurrentMap().getLoadedHills().stream().filter(Hill::isCaptured).forEach(hill -> captured.put(hill.getCapturedByParticipant(), hill));
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

            if (EnumSetting.TEAM_TYPE.is(TeamType.SOLO)) {
                if (hill.isCaptured() && hill.getCapturedByParticipant() == MatchManager.getParticipant(player)) {
                    continue;
                }
            }

            double tempDist = player.getLocation().distance(hill.getCenter());

            if (tempDist > dist) continue;

            dist = tempDist;
            target = hill;
        }

        return target;
    }

}
