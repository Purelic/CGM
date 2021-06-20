package net.purelic.cgm.core.managers;

import net.md_5.bungee.api.ChatColor;
import net.purelic.cgm.CGM;
import net.purelic.cgm.core.constants.MatchState;
import net.purelic.cgm.core.constants.MatchTeam;
import net.purelic.cgm.core.gamemodes.CustomGameMode;
import net.purelic.cgm.core.gamemodes.EnumSetting;
import net.purelic.cgm.core.gamemodes.NumberSetting;
import net.purelic.cgm.core.gamemodes.ToggleSetting;
import net.purelic.cgm.core.gamemodes.constants.GameType;
import net.purelic.cgm.core.gamemodes.constants.LootType;
import net.purelic.cgm.core.gamemodes.constants.TeamType;
import net.purelic.cgm.core.match.Participant;
import net.purelic.cgm.utils.*;
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
    private Objective tab;

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

    public static Scoreboard getBoard() {
        return board;
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

    public static void updateSoloBoard() {
        if (!MatchState.isState(MatchState.STARTED) || !EnumSetting.TEAM_TYPE.is(TeamType.SOLO)) return;

        List<Participant> participants = MatchManager.getOrderedParticipants(true);

        int offset = 0;

        int hills = MatchManager.getCurrentMap().getLoadedHills().size();
        offset += !ToggleSetting.PERMANENT_HILLS.isEnabled() && hills > 0 ? (NumberSetting.HILL_MOVE_INTERVAL.value() > 0 ? 1 : hills) : 0;

        int flags = MatchManager.getCurrentMap().getLoadedFlags().size();
        offset += flags > 0 ? (ToggleSetting.MOVING_FLAG.isEnabled() ? 1 : flags) : 0;

        offset += offset > 0 ? 1 : 0; // add extra blank row to separate objectives and player scores

        if (EnumSetting.GAME_TYPE.is(GameType.SURVIVAL_GAMES) && NumberSetting.REFILL_EVENT.value() > 0 && !EnumSetting.LOOT_TYPE.is(LootType.CUSTOM)) {
            offset += 2;
        }

        if (MatchUtils.isElimination() && !MatchUtils.hasKillScoring()) {
            if (MatchState.isState(MatchState.PRE_GAME, MatchState.STARTING)) return;

            int alive = MatchUtils.getAlive(MatchTeam.SOLO);
            String suffix = MatchTeam.SOLO.getColor() + (alive == 0 ? MatchTeam.SOLO.getName() : "Alive");
            setScore(offset, alive + "  " + suffix);
            return;
        }

        int scored = 0;

        int limit = NumberSetting.SCORE_LIMIT.value();
        String scoreSuffix = limit == 0 ? "" : ChatColor.DARK_GRAY + "/" + ChatColor.GRAY + limit;

        for (int i = 0; i < participants.size(); i++) {
            Participant participant = participants.get(i);
            String scoreColor = getScoreColor(participant);
            int score = participant.getTotalScore();

            if (score > 0) {
                setScore(i + offset, scoreColor + score + scoreSuffix + "  " + NickUtils.getDisplayName(participant.getPlayer()));
                scored++;
            } else {
                resetScores(i + offset);
            }
        }

        resetScores(scored + offset);
    }

    private static String getScoreColor(MatchTeam team) {
        if (EnumSetting.GAME_TYPE.is(GameType.CAPTURE_THE_FLAG)
            && NumberSetting.FLAG_CARRIER_POINTS.value() > 0
            && FlagUtils.hasCarrier(team)) {
            return ChatColor.GREEN.toString();
        } else if (EnumSetting.GAME_TYPE.is(GameType.KING_OF_THE_HILL)
            && !EnumSetting.TEAM_TYPE.is(TeamType.SOLO)
            && HillUtils.hasCaptured(team)) {
            return ChatColor.GREEN.toString();
        }

        return "";
    }

    private static String getScoreColor(Participant participant) {
        if (EnumSetting.GAME_TYPE.is(GameType.CAPTURE_THE_FLAG)
            && NumberSetting.FLAG_CARRIER_POINTS.value() > 0
            && FlagUtils.isCarrier(participant)) {
            return ChatColor.GREEN.toString();
        } else if (EnumSetting.GAME_TYPE.is(GameType.KING_OF_THE_HILL)
            && EnumSetting.TEAM_TYPE.is(TeamType.SOLO)
            && HillUtils.hasCaptured(participant)) {
            return ChatColor.GREEN.toString();
        }

        return "";
    }

    public static void updateTeamBoard() {
        TeamType teamType = EnumSetting.TEAM_TYPE.get();
        CustomGameMode gameMode = MatchManager.getCurrentGameMode();

        if (gameMode == null
            || MatchState.isState(MatchState.ENDED)
            || teamType == TeamType.SOLO) return;

        List<MatchTeam> teams = MatchManager.getOrderedTeams(teamType);

        for (MatchTeam team : teams) {
            int index = teams.indexOf(team);

            if (index < 0) return;

            int limit = NumberSetting.SCORE_LIMIT.value();
            String scoreSuffix = limit == 0 ? "" : ChatColor.DARK_GRAY + "/" + ChatColor.GRAY + limit;

            if (!EnumSetting.GAME_TYPE.is(GameType.BED_WARS)) {
                if (MatchUtils.isElimination() && !MatchUtils.hasKillScoring()) {
                    if (MatchState.isState(MatchState.PRE_GAME, MatchState.STARTING)) continue;

                    int alive = MatchUtils.getAlive(team);
                    String prefix = alive == 0 ? " " + ChatColor.RED + BedUtils.SYMBOL_TEAM_ELIMINATED : " " + alive;
                    String suffix = team.getColor() + (alive == 0 ? team.getName() : "Alive");
                    setScore(index, prefix + "  " + suffix);
                } else {
                    String scoreColor = getScoreColor(team);
                    setScore(index, scoreColor + team.getScore() + scoreSuffix + "  " + team.getColoredName());
                }
            } else {
                setScore(index, BedUtils.getScoreboardScore(team));
            }
        }
    }

//    public static void updateTeamBoard(MatchTeam team) {
//        CustomGameMode gameMode = MatchManager.getCurrentGameMode();
//        if (gameMode != null) updateTeamBoard(team, EnumSetting.TEAM_TYPE.get());
//    }
//
//    public static void updateTeamBoard(MatchTeam team, TeamType teamType) {
//        if (MatchState.isState(MatchState.ENDED)
//            || teamType == TeamType.SOLO) return;
//
//        List<MatchTeam> teams = teamType.getTeams();
//        int index = teams.indexOf(team);
//
//        if (index < 0) return;
//
//        int limit = NumberSetting.SCORE_LIMIT.value();
//        String scoreSuffix = limit == 0 ? "" : ChatColor.DARK_GRAY + "/" + ChatColor.GRAY + limit;
//
//        String scoreColor = getScoreColor(team);
//
//        if (!EnumSetting.GAME_TYPE.is(GameType.BED_WARS)) {
//            setScore(index, scoreColor + team.getScore() + scoreSuffix + "  " + team.getColoredName());
//        } else {
//            setScore(index, BedUtils.getScoreboardScore(team));
//        }
//    }

    public static void setDisplayName(String displayName) {
        sidebar.setDisplayName(displayName);
    }

    public void removePlayer(Player player) {
        board.getTeam(NickUtils.getRealName(player)).unregister();
        board.resetScores(NickUtils.getRealName(player));
        updateWaitingSidebar(true);
    }

    public static void setScore(int row, String score) {
        if (row > 15 || row < 0) return;
        String entry = ChatColor.values()[row].toString();
        setEntry(entry, score);
        sidebar.getScore(entry).setScore(15 - row);
    }

    public static void setEntry(String entry, String value) {
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

            suffix = color + suffix;
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
