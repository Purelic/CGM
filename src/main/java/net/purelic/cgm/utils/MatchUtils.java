package net.purelic.cgm.utils;

import net.md_5.bungee.api.ChatColor;
import net.purelic.cgm.core.constants.MatchTeam;
import net.purelic.cgm.core.gamemodes.CustomGameMode;
import net.purelic.cgm.core.gamemodes.EnumSetting;
import net.purelic.cgm.core.gamemodes.NumberSetting;
import net.purelic.cgm.core.gamemodes.constants.GameType;
import net.purelic.cgm.core.gamemodes.constants.TeamSize;
import net.purelic.cgm.core.gamemodes.constants.TeamType;
import net.purelic.cgm.core.managers.MatchManager;
import net.purelic.cgm.core.match.Participant;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class MatchUtils {

    public static boolean hasRounds() {
        return NumberSetting.ROUNDS.value() > 1;
    }

    public static boolean isElimination() {
        return NumberSetting.SCORE_LIMIT.value() == 0 && NumberSetting.LIVES_PER_ROUND.value() > 0;
    }

    public static boolean hasKillScoring() {
        return EnumSetting.GAME_TYPE.is(GameType.DEATHMATCH, GameType.HEAD_HUNTER, GameType.KING_OF_THE_HILL, GameType.INFECTION);
    }

    public static String getEliminationType() {
        return EnumSetting.TEAM_TYPE.is(TeamType.SOLO) ?
            "Other Players" : EnumSetting.TEAM_TYPE.is(TeamType.TEAMS) ? "Enemy Team" : "Enemy Teams";
    }

    public static String getObjectiveString() {
        int points = NumberSetting.SCORE_LIMIT.value();
        return MatchUtils.isElimination() ? ChatColor.RED + "Eliminate" + ChatColor.RESET + " " + MatchUtils.getEliminationType()
            : ChatColor.AQUA + "" + points + ChatColor.RESET + " Point" + (points == 1 ? "" : "s") + " to Win";
    }

    public static boolean isTeamEliminated(MatchTeam team) {
        return getAlive(team) == 0;
    }

    public static List<Player> getAlivePlayers(MatchTeam team) {
        List<Player> alive = new ArrayList<>();

        for (Player player : team.getPlayers()) {
            Participant participant = MatchManager.getParticipant(player);
            if (participant != null && !participant.isEliminated() && !participant.isQueued()) alive.add(player);
        }

        return alive;
    }

    public static int getAlive(MatchTeam team) {
        return getAlivePlayers(team).size();
    }

    public static int getMaxTeamPlayers() {
        return getMaxTeamPlayers(MatchManager.getCurrentGameMode());
    }

    public static int getMaxTeamPlayers(CustomGameMode gameMode) {
        return TeamSize.maxPlayers(
            TeamSize.valueOf(gameMode.getEnumSetting(EnumSetting.TEAM_SIZE)),
            TeamType.valueOf(gameMode.getEnumSetting(EnumSetting.TEAM_TYPE)));
    }

    public static boolean isMatchActive() {
        return MatchManager.getCurrentGameMode() != null;
    }

}
