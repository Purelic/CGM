package net.purelic.cgm.utils;

import net.md_5.bungee.api.ChatColor;
import net.purelic.cgm.core.constants.MatchState;
import net.purelic.cgm.core.constants.MatchTeam;
import net.purelic.cgm.core.gamemodes.EnumSetting;
import net.purelic.cgm.core.gamemodes.constants.TeamType;
import net.purelic.cgm.core.managers.MatchManager;
import net.purelic.cgm.core.maps.bed.Bed;
import org.bukkit.entity.Player;

import java.util.List;

public class BedUtils {

    public static final String SYMBOL_BED_COMPLETE = "\u2b1c"; // ⬜
    public static final String SYMBOL_BED_INCOMPLETE = "\u2b1b"; // ⬛
    public static final String SYMBOL_TEAM_ELIMINATED = "\u2715"; // ✕

    public static List<Bed> getBeds() {
        return MatchManager.getCurrentMap().getLoadedBeds();
    }

    public static boolean isBedDestroyed(MatchTeam team) {
        return getBeds().stream().filter(bed -> bed.getOwner() == team).allMatch(Bed::isDestroyed);
    }

    public static String getScoreboardScore(MatchTeam team) {
        ChatColor color = team.getColor();

        if (!MatchState.isState(MatchState.STARTED) || !isBedDestroyed(team)) {
            return " " + color + SYMBOL_BED_INCOMPLETE + "  " + team.getColoredName();
        } else if (isBedDestroyed(team) && !MatchUtils.isTeamEliminated(team)) {
            return " " + color + SYMBOL_BED_COMPLETE + "  " + ChatColor.RESET + MatchUtils.getAlive(team) + color + " Alive";
        } else {
            return " " + ChatColor.RED + SYMBOL_TEAM_ELIMINATED + "  " + team.getColoredName();
        }
    }

    public static boolean canUseTracker(Player player) {
        MatchTeam team = MatchTeam.getTeam(player);
        TeamType teamType = EnumSetting.TEAM_TYPE.get();
        return teamType.getTeams().stream().allMatch(t -> t == team || isBedDestroyed(t));
    }

    public static Bed getBed(MatchTeam team) {
        return getBeds().stream().filter(bed -> bed.getOwner() == team).findFirst().orElse(null);
    }

}
