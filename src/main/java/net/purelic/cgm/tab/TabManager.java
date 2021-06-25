package net.purelic.cgm.tab;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.TextComponent;
import net.purelic.cgm.core.constants.MatchTeam;
import net.purelic.cgm.core.gamemodes.EnumSetting;
import net.purelic.cgm.core.gamemodes.NumberSetting;
import net.purelic.cgm.core.gamemodes.constants.TeamType;
import net.purelic.cgm.core.managers.MatchManager;
import net.purelic.cgm.match.Match;
import net.purelic.cgm.match.MatchManager2;
import net.purelic.cgm.utils.TimeUtils;
import net.purelic.commons.Commons;
import net.purelic.commons.utils.ServerUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import shaded.com.google.api.client.util.ArrayMap;

import java.util.HashMap;
import java.util.Map;

public class TabManager {

    private final MatchManager2 matchManager;
    private final TextComponent defaultFooter;
    private final Map<Player, TabList> tabLists;
    private int time;
    private boolean blockUpdates;

    public TabManager(MatchManager2 matchManager) {
        this.matchManager = matchManager;
        this.defaultFooter = new TextComponent(ChatColor.GRAY + "purelic.net");
        this.tabLists = new HashMap<>();
        this.time = 0;
        this.blockUpdates = false;
    }

    public void enable(Player player) {
        this.tabLists.put(player, new TabList(player));
    }

    public void destroy(Player player) {
        this.tabLists.get(player).destroy();
        this.tabLists.remove(player);
    }

    public void updatePlayer(Player player) {
        Bukkit.getOnlinePlayers().forEach(online -> this.updatePlayer(online, player));
    }

    private void updatePlayer(Player player, Player update) {
        TabList tabList = this.tabLists.get(player);
        tabList.updatePlayer(update);
        if (!this.blockUpdates) tabList.update();
    }

    public void updateTeams() {
        TeamType teamType = EnumSetting.TEAM_TYPE.get();
        teamType.getTeams().forEach(this::updateTeam);
    }

    public void updateTeam(Player player) {
        this.updateTeam(MatchTeam.getTeam(player));
    }

    public void updateTeam(MatchTeam team) {
        this.updateTeam(team, false);
    }

    public void updateTeam(MatchTeam team, boolean updateHeader) {
        Bukkit.getOnlinePlayers().forEach(player -> this.updateTeam(player, team, updateHeader));
    }

    public void updateTeam(Player player, MatchTeam team, boolean updateHeader) {
        TabList tabList = this.tabLists.get(player);

        if (tabList == null) return;

        if (updateHeader) tabList.updateTeam(team);
        else tabList.updatePlayers(team);

        if (!this.blockUpdates) tabList.update();
    }

    public void updateTime(int time) {
        this.time = time;
        Bukkit.getOnlinePlayers().forEach(this::updateTime);
    }

    private void updateTime(Player player) {
        TabList tabList = this.tabLists.get(player);
        tabList.updateTime();
        if (!this.blockUpdates) tabList.update();
    }

    public void updateRounds() {
        Bukkit.getOnlinePlayers().forEach(this::updateRounds);
    }

    private void updateRounds(Player player) {
        TabList tabList = this.tabLists.get(player);
        tabList.updateRounds();
        if (!this.blockUpdates) tabList.update();
    }

    public void reset() {
        Bukkit.getOnlinePlayers().forEach(this::reset);
    }

    private void reset(Player player) {
        this.tabLists.get(player).reset();
    }

    public TextComponent getHeader() {
        String header = ChatColor.BOLD + ServerUtils.getName();
        if (Commons.hasOwner())  header += "'s Server";

        Match match = this.matchManager.getCurrentMatch();

        if (match != null) {
            header += "\n" + match.getMatchTitle();
        }

        return new TextComponent(header);
    }

    public TextComponent getFooter(Player player) {
        Match match = this.matchManager.getCurrentMatch();

        if (match == null) return this.defaultFooter;

        String stats = this.matchManager.isPlaying(player) ? this.matchManager.getParticipant(player).getStats().getSummary(false) + "\n" : "";
        String rounds = match.isRoundBased() ? match.getRoundsString(false) + "\n" : "";
        String time = this.getTime(false);

        return new TextComponent(stats + rounds + time);
    }

    public String getTime(boolean trunc) {
        String label = NumberSetting.TIME_LIMIT.value() == 0 ? "Elapsed" : "Remaining";
        return (trunc ? ChatColor.GRAY + "Time: " : ChatColor.WHITE + "Time " + label + ": ") +
            TimeUtils.getFormattedTime(this.time);
    }

    public void updateStatsAll() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (this.matchManager.isPlaying(player)) this.updateStats(player);
        }
    }

    public void updateStats(Player player) {
        TabList tabList = this.tabLists.get(player);
        if (tabList.isLegacy()) return; // legacy players don't have tab stats
        tabList.updateHeaderFooter();
        if (!this.blockUpdates) tabList.update();
    }

    public void blockUpdates() {
        this.blockUpdates = true;
    }

    public void allowUpdates() {
        this.blockUpdates = false;
        this.tabLists.values().forEach(TabList::update);
    }

}
