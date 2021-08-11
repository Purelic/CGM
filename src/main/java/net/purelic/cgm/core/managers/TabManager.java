package net.purelic.cgm.core.managers;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.TextComponent;
import net.purelic.cgm.core.constants.MatchTeam;
import net.purelic.cgm.core.gamemodes.CustomGameMode;
import net.purelic.cgm.core.gamemodes.EnumSetting;
import net.purelic.cgm.core.gamemodes.NumberSetting;
import net.purelic.cgm.core.gamemodes.constants.GameType;
import net.purelic.cgm.core.gamemodes.constants.TeamType;
import net.purelic.cgm.core.maps.CustomMap;
import net.purelic.cgm.core.match.Participant;
import net.purelic.cgm.utils.MatchUtils;
import net.purelic.cgm.utils.TimeUtils;
import net.purelic.cgm.utils.tab.TabList;
import net.purelic.commons.Commons;
import net.purelic.commons.utils.Fetcher;
import net.purelic.commons.utils.ServerUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class TabManager {

    private static final TextComponent DEFAULT_FOOTER = new TextComponent(ChatColor.GRAY + "purelic.net");
    private static final Map<Player, TabList> TAB_LISTS = new HashMap<>();
    private static int time = 0;
    private static boolean blockUpdates = false;

    public static void enable(Player player) {
        TAB_LISTS.put(player, new TabList(player));
    }

    public static void destroy(Player player) {
        TAB_LISTS.get(player).destroy();
        TAB_LISTS.remove(player);
    }

    public static void updatePlayer(Player player) {
        Bukkit.getOnlinePlayers().forEach(online -> updatePlayer(online, player));
    }

    private static void updatePlayer(Player player, Player update) {
        TabList tabList = TAB_LISTS.get(player);
        tabList.updatePlayer(update);
        if (!blockUpdates) tabList.update();
    }

    public static void updateTeam(Player player) {
        updateTeam(MatchTeam.getTeam(player));
    }

    public static void updateTeam(MatchTeam team) {
        updateTeam(team, false);
    }

    public static void updateTeams() {
        TeamType teamType = EnumSetting.TEAM_TYPE.get();
        teamType.getTeams().forEach(team -> updateTeam(team, false));
    }

    public static void updateTeam(MatchTeam team, boolean updateHeader) {
        Bukkit.getOnlinePlayers().forEach(player -> updateTeam(player, team, updateHeader));
    }

    public static void updateTeam(Player player, MatchTeam team, boolean updateHeader) {
        TabList tabList = TAB_LISTS.get(player);
        if (tabList == null) return;
        if (updateHeader) tabList.updateTeam(team);
        else tabList.updatePlayers(team);
        if (!blockUpdates) tabList.update();
    }

    public static void updateTime(int time) {
        TabManager.time = time;
        Bukkit.getOnlinePlayers().forEach(TabManager::updateTime);
    }

    private static void updateTime(Player player) {
        TabList tabList = TAB_LISTS.get(player);
        tabList.updateTime();
        if (!blockUpdates) tabList.update();
    }

    public static void updateRounds() {
        Bukkit.getOnlinePlayers().forEach(TabManager::updateRounds);
    }

    private static void updateRounds(Player player) {
        TabList tabList = TAB_LISTS.get(player);
        tabList.updateRounds();
        if (!blockUpdates) tabList.update();
    }

    public static void reset() {
        Bukkit.getOnlinePlayers().forEach(TabManager::reset);
    }

    private static void reset(Player player) {
        TAB_LISTS.get(player).reset();
    }

    public static TextComponent getHeader() {
        String header = ChatColor.BOLD + ServerUtils.getName();
        if (Commons.hasOwner()) header += "'s Server";

        CustomGameMode gameMode = MatchManager.getCurrentGameMode();
        CustomMap map = MatchManager.getCurrentMap();

        if (gameMode != null && map != null) {
            if (gameMode.getGameType() == GameType.UHC) {
                header += "\n" + gameMode.getColoredNameWithAlias();
                return new TextComponent(header);
            }

            header += "\n" + gameMode.getColoredName() + " on " + map.getColoredName();

            List<UUID> authors = map.getYaml().getAuthors();

            if (authors.size() >= 3) {
                header += "\nby ";
            } else {
                header += " by ";
            }

            if (authors.size() == 1) {
                header += getAuthor(authors.get(0));
            } else if (authors.size() == 2) {
                header += getAuthor(authors.get(0)) + " and " + getAuthor(authors.get(1));
            } else {
                for (int i = 0; i < authors.size(); i++) {
                    if (i == 0) {
                        header += getAuthor(authors.get(i));
                    } else if (i == authors.size() - 1) {
                        header += ", and " + getAuthor(authors.get(i));
                    } else {
                        header += ", " + getAuthor(authors.get(i));
                    }
                }
            }
        }

        return new TextComponent(header);
    }

    private static String getAuthor(UUID uuid) {
        Player player = Bukkit.getPlayer(uuid);

        if (player != null && player.isOnline()) {
            return Fetcher.getBasicName(uuid);
        } else {
            return ChatColor.DARK_AQUA + Fetcher.getNameOf(uuid) + ChatColor.RESET;
        }
    }

    public static TextComponent getFooter(Player player) {
        CustomGameMode gameMode = MatchManager.getCurrentGameMode();
        if (gameMode == null) return DEFAULT_FOOTER;

        String stats = MatchManager.isPlaying(player) ? MatchManager.getParticipant(player).getTabStats() + "\n" : "";
        String rounds = MatchUtils.hasRounds() ? getRounds(false) + "\n" : "";
        String time = getTime(false);

        return new TextComponent(stats + rounds + time);
    }

    public static String getTime(boolean trunc) {
        String label = NumberSetting.TIME_LIMIT.value() == 0 ? "Elapsed" : "Remaining";
        return (trunc ? ChatColor.GRAY + "Time: " : ChatColor.WHITE + "Time " + label + ": ") +
            TimeUtils.getFormattedTime(time);
    }

    public static String getRounds(boolean trunc) {
        return (trunc ? "" : ChatColor.WHITE + "Rounds: ") + MatchManager.getRoundsString();
    }

    public static void updateStatsAll() {
        MatchManager.getParticipants().forEach(TabManager::updateStats);
    }

    public static void updateStats(Participant participant) {
        TabList tabList = TAB_LISTS.get(participant.getPlayer());
        tabList.updateStats();
        if (!blockUpdates) tabList.update();
    }

    public static void removeStats(Player player) {
        TabList tabList = TAB_LISTS.get(player);
        tabList.removeStats();
        if (!blockUpdates) tabList.update();
    }

    public static void updateScore(Participant participant) {
        updateStats(participant);
    }

    public static void updateKills(Participant participant) {
        updateStats(participant);
    }

    public static void updateDeaths(Participant participant) {
        updateStats(participant);
    }

    public static void blockUpdates() {
        blockUpdates = true;
    }

    public static void allowUpdates() {
        blockUpdates = false;
        TAB_LISTS.values().forEach(TabList::update);
    }

}
