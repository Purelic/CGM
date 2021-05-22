package net.purelic.cgm.utils.tab;

import net.minecraft.server.v1_8_R3.Packet;
import net.minecraft.server.v1_8_R3.PacketListenerPlayOut;
import net.purelic.cgm.core.constants.MatchTeam;
import net.purelic.cgm.core.managers.MatchManager;
import net.purelic.cgm.core.managers.TabManager;
import net.purelic.cgm.core.match.Participant;
import net.purelic.cgm.utils.MatchUtils;
import net.purelic.commons.utils.VersionUtils;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TabList {

    private final Player player;
    private final boolean legacy;
    private final List<TabItem> tabItems;
    private final List<TabItem> blankItems;
    private final List<Packet<? extends PacketListenerPlayOut>> pendingUpdates;
    private Map<MatchTeam, TabBox> teamBoxes;

    public TabList(Player player) {
        this(player, VersionUtils.isLegacy(player));
    }

    private TabList(Player player, boolean legacy) {
        this.player = player;
        this.legacy = legacy;
        this.tabItems = new ArrayList<>();
        this.blankItems = new ArrayList<>();
        this.pendingUpdates = new ArrayList<>();
        this.teamBoxes = new HashMap<>();
        this.reset();
    }

    public Player getPlayer() {
        return this.player;
    }

    public void reset() {
        // Clear the tab
        this.clearAll();

        // Add teams to tab
        if (MatchUtils.isMatchActive()) {
            this.teamBoxes = TabUtils.getTeamBoxes(this.legacy);
        } else {
            this.teamBoxes.put(MatchTeam.OBS, new TabBox(MatchTeam.OBS, 0, 1, TabUtils.COLUMNS - 1, TabUtils.ROWS - 1));
        }

        // Update header and footer
        this.updateHeaderFooter();

        // Queue and send all the tab packets
        this.teamBoxes.values().forEach(box -> box.updateTeam(this));
        for (TabItem item : this.tabItems) this.pendingUpdates.add(item.getAddPacket());
        this.update();
    }

    public TabItem getTabItem(int index) {
        return this.tabItems.get(this.legacy ? TabUtils.getLegacyIndex(index) : index);
    }

    public void setSlot(int index, String name) {
        this.setPlayerSlot(index, name, null);
    }

    public void setPlayerSlot(int index, String name, Player player) {
        if (!this.isValidIndex(index)) return;
        TabItem item = this.getTabItem(index);
        this.pendingUpdates.addAll(item.setPlayerName(name, player, this.player));
        this.blankItems.remove(item);
    }

    public void setSlot(int index, String name, TabSkin skin) {
        if (!this.isValidIndex(index)) return;
        TabItem item = this.getTabItem(index);
        this.pendingUpdates.addAll(item.setName(name, skin, this.player));
        this.blankItems.remove(item);
    }

    public void setPlayer(int index, Player player) {
        if (!this.isValidIndex(index)) return;
        TabItem item = this.getTabItem(index);
        this.pendingUpdates.addAll(item.setPlayer(player, this.player));
        this.blankItems.remove(item);
    }

    public void updatePlayer(Player player) {
        this.teamBoxes.get(MatchTeam.getTeam(player)).updatePlayer(this, player);
    }

    public void setTeam(int index, MatchTeam team) {
        this.setSlot(index, TabUtils.getTeamSlot(team, this.player, this.legacy), TabSkin.getDot(team));
    }

    public void updateTeam(MatchTeam team) {
        this.teamBoxes.get(team).updateTeam(this);
    }

    public void updatePlayers(MatchTeam team) {
        this.teamBoxes.get(team).updatePlayers(this);
    }

    public void updateTime() {
        if (this.legacy) this.setSlot(59, TabManager.getTime(true));
        else this.updateHeaderFooter();
    }

    public void updateRounds() {
        if (!MatchUtils.hasRounds()) return;
        if (this.legacy) this.setSlot(79, TabManager.getRounds(true));
        else this.updateHeaderFooter();
    }

    private void updateHeaderFooter() {
        if (this.legacy) {
            if (MatchUtils.isMatchActive()) {
                this.setSlot(19, MatchManager.getCurrentGameMode().getColoredName());
                this.setSlot(39, MatchManager.getCurrentMap().getColoredName());
                this.updateTime();
                this.updateRounds();
            } else {
                this.clear(19);
                this.clear(39);
                this.clear(59);
                this.clear(79);
            }
        } else {
            this.player.setPlayerListHeaderFooter(TabManager.getHeader(), TabManager.getFooter(this.player));
        }
    }

    public void updateStats(Participant participant) {
        if (!this.legacy) {
            this.updateHeaderFooter();
        } else {
            if (!this.teamBoxes.containsKey(MatchTeam.OBS)) return;

//            this.setSlot(72, "" + participant.getTeam().getColor() + ChatColor.BOLD + "Your Stats", TabSkin.getDot(participant.getTeam()));
//            this.updateScore(participant);
//            this.updateKills(participant);
//            this.updateDeaths(participant);
//            this.updateKDR(participant);
//            this.updateKillStreak(participant);
        }
    }

    public void removeStats() {
        if (!this.legacy) this.updateHeaderFooter();
        else if (!this.teamBoxes.containsKey(MatchTeam.OBS)) return;

//        this.clear(72); // stats header
//        this.clear(73); // score
//        this.clear(74); // kills / assists
//        this.clear(75); // deaths
//        this.clear(76); // kd / kda
//        this.clear(77); // kill streak
    }

    public void updateScore(Participant participant) {
        if (!this.legacy) {
            this.updateStats(participant);
            return;
        } else if (!this.teamBoxes.containsKey(MatchTeam.OBS)) {
            return;
        }

        // this.setSlot(73, "Score: " + ChatColor.YELLOW + participant.getTotalScore());
    }

    public void updateKills(Participant participant) {
        if (!this.legacy) {
            this.updateStats(participant);
            return;
        } else if (!this.teamBoxes.containsKey(MatchTeam.OBS)) {
            return;
        }

//        int assists = participant.getStats().getAssists();
//        String suffix = assists == 0 ? "" : ChatColor.GRAY + " (" + assists + " Assist" + (assists == 1 ? "" : "s") + ")";
//        this.setSlot(74, "Kills: " + ChatColor.GREEN + participant.getKills() + suffix);
    }

    public void updateDeaths(Participant participant) {
        if (!this.legacy) {
            this.updateStats(participant);
            return;
        } else if (!this.teamBoxes.containsKey(MatchTeam.OBS)) {
            return;
        }

        // this.setSlot(75, "Deaths: " + ChatColor.RED + participant.getDeaths());
    }

    public void updateKDR(Participant participant) {
        if (!this.legacy) {
            this.updateStats(participant);
            return;
        } else if (!this.teamBoxes.containsKey(MatchTeam.OBS)) {
            return;
        }

//        String kd = new DecimalFormat("0.0").format(participant.getStats().getKillDeathRatio());
//        String kda = new DecimalFormat("0.0").format(participant.getStats().getKillDeathAssistRatio());
//        String suffix = participant.getStats().getAssists() > 0 ? ChatColor.GRAY + " (" + kda + " KDA)" : "";
//        this.setSlot(76, "KD: " + ChatColor.AQUA + kd + suffix);
    }

    public void updateKillStreak(Participant participant) {
        if (!this.legacy) {
            this.updateStats(participant);
            return;
        } else if (!this.teamBoxes.containsKey(MatchTeam.OBS)) {
            return;
        }

//        int killStreak = participant.getKillstreak();
//        int bestKillStreak = participant.getBestKillstreak();
//        String suffix = bestKillStreak > 0 && bestKillStreak > killStreak ? ChatColor.GRAY + " (Best " + bestKillStreak + ")" :"";
//        this.setSlot(77, "Streak: " + ChatColor.AQUA + killStreak + suffix);
    }

    public void clear(int index) {
        TabItem item;

        if (index >= this.tabItems.size()) {
            item = new TabItem(index, this.legacy);
            this.tabItems.add(item);
        } else {
            item = this.getTabItem(index);
            if (this.blankItems.contains(item)) return;
            this.setSlot(index, "", TabSkin.BLANK);
        }

        this.blankItems.add(item);
    }

    public void clearAll() {
        for (int i = 0; i < TabUtils.MAX_SLOTS; i++) this.clear(i);
        this.teamBoxes.clear();
    }

    public void update() {
        if (this.pendingUpdates.isEmpty()) return;
        TabUtils.sendUpdates(this.player, new ArrayList<>(this.pendingUpdates)); // make a copy to avoid ConcurrentModificationException
        this.pendingUpdates.clear();
    }

    public void destroy() {
        if (!this.legacy) {
            for (int i = 0; i < TabUtils.MAX_SLOTS; i++) this.destroy(i);
            this.update();
        }

        this.tabItems.clear();
        this.blankItems.clear();
        this.teamBoxes.clear();
    }

    private void destroy(int index) {
        this.pendingUpdates.add(this.getTabItem(index).getRemovePacket());
    }

    private boolean isValidIndex(int index) {
        return index >= 0 && index < TabUtils.MAX_SLOTS;
    }

}
