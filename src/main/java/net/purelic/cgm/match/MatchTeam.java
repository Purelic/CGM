package net.purelic.cgm.match;

import net.purelic.cgm.CGM;
import net.purelic.cgm.core.managers.ScoreboardManager;
import net.purelic.cgm.match.constants.ParticipantState;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.*;

public enum MatchTeam {

    OBS("Spectators", ChatColor.AQUA, 0, 0),
    SOLO("FFA", ChatColor.YELLOW, 0, 0),
    ;

    private final String defaultName;
    private final ChatColor defaultColor;
    private String name;
    private ChatColor color;
    private final int tabOrder;
    private final int tabCol;
    private final List<Player> players;
    private final Set<UUID> allowed;

    MatchTeam(String name, ChatColor color, int tabCol, int tabOrder) {
        this.defaultName = name;
        this.defaultColor = color;
        this.name = name;
        this.color = color;
        this.tabCol = tabCol;
        this.tabOrder = tabOrder;
        this.players = new ArrayList<>();
        this.allowed = new HashSet<>();
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
        ScoreboardManager.updateTeamBoard();
    }

    public String getDefaultName() {
        return this.defaultName;
    }

    public String getColoredName() {
        return this.color + this.name + net.md_5.bungee.api.ChatColor.RESET;
    }

    public net.md_5.bungee.api.ChatColor getColor() {
        return this.color;
    }

    public void setColor(net.md_5.bungee.api.ChatColor color) {
        this.color = color;
    }

    public String getColoredName() {
        return "" + ChatColor.RESET;
    }

    public boolean has(Player player) {
        return this.players.contains(player);
    }

    public boolean isEliminated() {
        return this.getAlive() > 0;
    }

    public int getAlive() {
        int alive = 0;

        for (Player player : this.players) {
            MatchParticipant participant = CGM.getMatchManager2().getParticipant(player);
            if (participant.isState(ParticipantState.ALIVE)) alive++;
        }

        return alive;
    }

    public static MatchTeam getTeam(Player player) {
        for (MatchTeam team : values()) {
            if (team.has(player)) return team;
        }

        return MatchTeam.OBS;
    }

}
