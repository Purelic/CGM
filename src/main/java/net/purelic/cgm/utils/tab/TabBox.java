package net.purelic.cgm.utils.tab;

import net.purelic.cgm.core.constants.MatchTeam;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.*;

public class TabBox {

    private final MatchTeam team;
    private final int col1;
    private final int row1;
    private final int col2;
    private final int row2;
    private final int header;
    private final Map<Player, Integer> players;

    public TabBox(MatchTeam team, int col1, int row1, int col2, int row2) {
        this.team = team;
        this.col1 = col1;
        this.row1 = row1;
        this.col2 = col2;
        this.row2 = row2;
        this.header = this.getIndex(col1, row1 - 1);
        this.players = new HashMap<>();
    }

    public void updateTeam(TabList tabList) {
        tabList.setTeam(this.header, this.team);
        this.updatePlayers(tabList);
    }

    public void updatePlayers(TabList tabList) {
        this.updatePlayers(tabList, TabUtils.sort(new ArrayList<>(this.team.getPlayers())));
    }

    public void updatePlayer(TabList tabList, Player player) {
        tabList.setPlayerSlot(this.players.get(player), player.getPlayerListName(), player);
    }

    private void updatePlayers(TabList tabList, List<Player> players) {
        Map<Integer, Player> slots = new HashMap<>();
        Iterator<Player> iterator = players.iterator();

        this.players.clear();
        int size = players.size();
        int lastIndex = 0;
        Player lastPlayer = null;

        for (int col = this.col1; col <= this.col2; col++) {
            for (int row = this.row1; row <= this.row2; row++) {
                int index = this.getIndex(col, row);
                if (iterator.hasNext()) {
                    Player player = iterator.next();

                    slots.put(index, player);
                    this.players.put(player, index);

                    lastIndex = index;
                    lastPlayer = player;
                } else {
                    tabList.clear(index);
                }
            }
        }

        // Check if all the players fit in tab
        boolean fit = !iterator.hasNext();

        // If they don't all fit, remove the last entry
        if (!fit) {
            slots.remove(lastIndex);
            this.players.remove(lastPlayer);
        }

        // Queue all the player updates
        for (Map.Entry<Integer, Player> entry : slots.entrySet())
            tabList.setPlayer(entry.getKey(), entry.getValue());

        // Label the team overflow (if applicable)
        if (!fit) {
            int overflow = size - slots.size();
            String val = "" + ChatColor.GRAY + ChatColor.ITALIC + "and " + overflow + " others";
            tabList.setSlot(lastIndex, val, TabSkin.BLANK);
        }
    }

    private int getIndex(int column, int row) {
        return row + TabUtils.ROWS * column;
    }

}
