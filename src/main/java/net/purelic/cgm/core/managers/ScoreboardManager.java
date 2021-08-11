package net.purelic.cgm.core.managers;

import net.md_5.bungee.api.ChatColor;
import net.purelic.cgm.CGM;
import net.purelic.cgm.core.constants.MatchState;
import net.purelic.cgm.core.constants.MatchTeam;
import net.purelic.cgm.core.gamemodes.CustomGameMode;
import net.purelic.cgm.core.gamemodes.EnumSetting;
import net.purelic.cgm.core.gamemodes.constants.TeamType;
import net.purelic.cgm.scoreboards.MatchScoreboard;
import net.purelic.cgm.scoreboards.PlayerScoreboard;
import net.purelic.cgm.scoreboards.TeamScoreboard;
import net.purelic.commons.Commons;
import net.purelic.commons.utils.NickUtils;
import net.purelic.commons.utils.ServerUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.*;

import java.util.List;

public class ScoreboardManager {

    private static Scoreboard board;
    private static Objective sidebar;
    private static MatchScoreboard matchScoreboard;

    public ScoreboardManager() {
        board = Bukkit.getScoreboardManager().getNewScoreboard();
        sidebar = board.registerNewObjective("sidebar", "dummy");
        sidebar.setDisplaySlot(DisplaySlot.SIDEBAR);

        for (MatchTeam matchTeam : MatchTeam.values()) {
            Team team = board.registerNewTeam(matchTeam.getDefaultName());
            team.setPrefix(matchTeam.getColor().toString());

            if (matchTeam != MatchTeam.SOLO) {
                team.setAllowFriendlyFire(false);
                team.setCanSeeFriendlyInvisibles(false);
            } else {
                team.setAllowFriendlyFire(true);
                team.setCanSeeFriendlyInvisibles(true);
            }
        }
    }

    public static void setScoreboard(Player player) {
        if (hasTeam(player)) return;
        Team team = board.registerNewTeam(NickUtils.getRealName(player));
        team.setPrefix(Commons.getProfile(player).getFlairs(true) + MatchTeam.OBS.getColor());
        updateWaitingSidebar(false);
        player.setScoreboard(board);
    }

    public static boolean hasTeam(Player player) {
        return board.getTeam(NickUtils.getRealName(player)) != null;
    }

    public static void setNameVisibility(boolean visible) {
        TeamType teamType = EnumSetting.TEAM_TYPE.get();
        List<MatchTeam> teams = teamType.getTeams();

        for (MatchTeam matchTeam : teams) {
            Team team = board.getTeam(matchTeam.getDefaultName());

            if (visible) {
                team.setNameTagVisibility(NameTagVisibility.ALWAYS);
            } else {
                if (matchTeam == MatchTeam.SOLO) {
                    team.setNameTagVisibility(NameTagVisibility.HIDE_FOR_OWN_TEAM);
                } else {
                    team.setNameTagVisibility(NameTagVisibility.HIDE_FOR_OTHER_TEAMS);
                }
            }
        }
    }

    public static void setFriendlyFire(boolean friendlyFire) {
        TeamType teamType = EnumSetting.TEAM_TYPE.get();
        List<MatchTeam> teams = teamType.getTeams();

        for (MatchTeam matchTeam : teams) {
            Team team = board.getTeam(matchTeam.getDefaultName());

            if (matchTeam == MatchTeam.SOLO) {
                team.setAllowFriendlyFire(true);
            } else {
                team.setAllowFriendlyFire(friendlyFire);
            }
        }
    }

    public static void updateTeam(Player player, MatchTeam matchTeam) {
        String entry = NickUtils.getRealName(player);
        Team team = board.getEntryTeam(entry);

        if (team != null) team.removeEntry(entry);

        if (matchTeam == MatchTeam.OBS) {
            board.getTeam(entry).addEntry(NickUtils.getNick(player));
        } else {
            board.getTeam(matchTeam.getDefaultName()).addEntry(NickUtils.getNick(player));
        }
    }

    public static void updateWaitingSidebar(boolean quit) {
        if (MatchState.isState(MatchState.WAITING) && !ServerUtils.isRanked()) {
            setDisplayName("Waiting");
            int online = Bukkit.getOnlinePlayers().size();
            setScore(
                0,
                (quit ? online - 1 : online) + "" + ChatColor.DARK_GRAY + "/" +
                    ChatColor.GRAY + CGM.getVotingManager().getSettings().getMinPlayers() + " " + MatchTeam.OBS.getColor() + "Players");
        }
    }

    public static MatchScoreboard getMatchScoreboard() {
        return matchScoreboard;
    }

    public static void initMatchScoreboard() {
        if (EnumSetting.TEAM_TYPE.is(TeamType.SOLO)) {
            matchScoreboard = new PlayerScoreboard();
        } else {
            matchScoreboard = new TeamScoreboard();
        }

        matchScoreboard.init();
    }

    public static void updateSoloBoard() {
        if (!MatchState.isState(MatchState.STARTED) || !(matchScoreboard instanceof PlayerScoreboard)) return;
        ((PlayerScoreboard) matchScoreboard).update();
    }

    public static void updateTeamBoard() {
        // TODO unsure if this check is necessary anymore
        CustomGameMode gameMode = MatchManager.getCurrentGameMode();

        if (gameMode == null
            || MatchState.isState(MatchState.ENDED)
            || !(matchScoreboard instanceof TeamScoreboard)) return;

        ((TeamScoreboard) matchScoreboard).update();
    }

    public static void setDisplayName(String displayName) {
        sidebar.setDisplayName(displayName);
    }

    public void removePlayer(Player player) {
        board.getTeam(NickUtils.getRealName(player)).unregister();
        board.resetScores(NickUtils.getRealName(player));
        updateWaitingSidebar(true);
    }

    public static void setScore(int row, String score) {
        setScore(row, score, "");
    }

    public static void setScore(int row, String score, String forceColor) {
        if (row > 15 || row < 0) return;
        String entry = ChatColor.values()[row].toString();
        setEntry(entry, score, forceColor);
        sidebar.getScore(entry).setScore(15 - row);
    }

    public static void setEntry(String entry, String value) {
        setEntry(entry, value, "");
    }

    public static void setEntry(String entry, String value, String forceColor) {
        Team team = registerTeam(entry);

        // player display names end with ChatColor.RESET
        value = endsWithChatColor(value) ? value.substring(0, value.length() - 2) : value;

        if (value.length() <= 16) {
            if (!team.getPrefix().equals(value)) team.setPrefix(value);
            if (!team.getSuffix().equals("")) team.setSuffix("");
        } else {
            String prefix = value.substring(0, 16);
            String suffix = value.substring(16);

            int colorIndex = prefix.lastIndexOf(ChatColor.COLOR_CHAR);
            String color = colorIndex >= 0 ? value.substring(colorIndex, colorIndex + 2) : "";

            if (endsWithColorChar(prefix)) {
                prefix = value.substring(0, 15);
                suffix = value.substring(17);
            }

            suffix = color + forceColor + suffix;
            suffix = suffix.length() > 16 ? suffix.substring(0, 16) : suffix;

            if (!team.getPrefix().equals(prefix)) team.setPrefix(prefix);
            if (!team.getSuffix().equals(suffix)) team.setSuffix(suffix);
        }
    }

    public static boolean endsWithChatColor(String score) {
        if (score.length() < 2) return false;
        return score.charAt(score.length() - 2) == ChatColor.COLOR_CHAR;
    }

    public static boolean endsWithColorChar(String score) {
        if (score.length() < 1) return false;
        return score.charAt(score.length() - 1) == ChatColor.COLOR_CHAR;
    }

    public static Team registerTeam(String entry) {
        Team team = board.getTeam(entry);

        if (team == null) {
            team = board.registerNewTeam(entry);
            team.addEntry(entry);
        }

        return team;
    }

    public static void resetScores(int row) {
        for (int i = row; i <= 15; i++) {
            resetScore(i);
        }
    }

    public static void resetScore(int row) {
        String entry = ChatColor.values()[row].toString();
        if (sidebar.getScore(entry).isScoreSet()) {
            board.resetScores(entry);
        }
    }

}
