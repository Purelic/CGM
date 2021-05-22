package net.purelic.cgm.utils;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.TextComponent;
import net.purelic.cgm.core.constants.MatchTeam;
import net.purelic.cgm.core.gamemodes.CustomGameMode;
import net.purelic.cgm.core.gamemodes.EnumSetting;
import net.purelic.cgm.core.gamemodes.NumberSetting;
import net.purelic.cgm.core.gamemodes.constants.GameType;
import net.purelic.cgm.core.gamemodes.constants.TeamSize;
import net.purelic.cgm.core.gamemodes.constants.TeamType;
import net.purelic.cgm.core.managers.MatchManager;
import net.purelic.cgm.core.maps.CustomMap;
import net.purelic.cgm.core.match.Participant;
import net.purelic.commons.Commons;
import net.purelic.commons.utils.ServerUtils;
import org.bukkit.entity.Player;

public class MatchUtils {

    private static final TextComponent DEFAULT_HEADER = new TextComponent(ChatColor.BOLD + "PuRelic Network");
    private static final TextComponent DEFAULT_FOOTER = new TextComponent(ChatColor.GRAY + "purelic.net");
    private static int time = 0;

    public static void updateTabAll() {
        // Bukkit.getOnlinePlayers().forEach(MatchUtils::updateTab);
    }

    public static void updateTabAll(int seconds) {
        time = seconds;
        // Bukkit.getOnlinePlayers().forEach(MatchUtils::updateTab);
    }

    public static void updateTab(Player player) {
        player.setPlayerListHeaderFooter(getMatchHeader(), getMatchFooter());
    }

    public static TextComponent getMatchHeader() {
        CustomGameMode gameMode = MatchManager.getCurrentGameMode();
        CustomMap map = MatchManager.getCurrentMap();

        if (!Commons.hasOwner()) {
            if (map == null || gameMode == null) {
                return new TextComponent(ChatColor.BOLD + ServerUtils.getName());
            }

            return new TextComponent(
                    ChatColor.BOLD + ServerUtils.getName() + "\n" +
                    gameMode.getColoredName() + " on " + map.getColoredName() +
                            "\n" + MatchUtils.getObjectiveString());
        } else {
            if (map == null || gameMode == null) return new TextComponent(ChatColor.BOLD + ServerUtils.getName() + "'s Server");

            return new TextComponent(
                    ChatColor.BOLD + ServerUtils.getName() + "'s Server\n" +
                    gameMode.getColoredName() + " on " + map.getColoredName());
                    // + "\n" + MatchUtils.getObjectiveString());
        }
    }

    public static String getTimeString() {
        return ChatColor.WHITE + "Time Remaining: " + TimeUtils.getFormattedTime(time);
    }

    public static String getRoundsString() {
        return ChatColor.WHITE + "Rounds:" + MatchManager.getRoundsString();
    }

    public static boolean hasRounds() {
        return NumberSetting.ROUNDS.value() > 1;
    }

    public static TextComponent getMatchFooter() {
        CustomGameMode gameMode = MatchManager.getCurrentGameMode();

        if (gameMode == null) return DEFAULT_FOOTER;

        return new TextComponent(
                (NumberSetting.ROUNDS.value() == 1 ? "" : getRoundsString() + "\n") +
                getTimeString());
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

    public static int getAlive(MatchTeam team) {
        int alive = 0;

        for (Player player : team.getPlayers()) {
            Participant participant = MatchManager.getParticipant(player);
            if (!participant.isEliminated() && !participant.isQueued()) alive++;
        }

        return alive;
    }

    public static int getMaxPlayers(CustomGameMode gameMode) {
        int maxPerTeam = getMaxTeamPlayers(gameMode);
        TeamType teamType = TeamType.valueOf(gameMode.getEnumSetting(EnumSetting.TEAM_TYPE));
        return maxPerTeam * teamType.getTeams().size();
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
