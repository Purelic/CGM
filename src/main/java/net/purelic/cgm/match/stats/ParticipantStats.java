package net.purelic.cgm.match.stats;

import net.purelic.cgm.match.Match;
import org.bukkit.ChatColor;
import org.bukkit.scheduler.BukkitRunnable;

import java.text.DecimalFormat;

public class ParticipantStats {

    private int totalScore;
    private int roundScore;
    private int roundsWon;
    private int kills;
    private int deaths;
    private int killstreak;
    private int bestKillstreak;
    private int multiKill;
    private int longestShot;
    private BukkitRunnable multiKillTimer;

    public ParticipantStats() {
        this.totalScore = 0;
        this.roundScore = 0;
        this.roundsWon = 0;
        this.kills = 0;
        this.deaths = 0;
        this.killstreak = 0;
        this.bestKillstreak = 0;
        this.multiKill = 0;
        this.longestShot = 0;
    }

    // TODO
    public String getSummary(boolean truncate) {
        return "";
//        int assists = this.getStats().getAssists();
//        String assistsStr = assists == 0 ? "" : ChatColor.GRAY + " (" + assists + " Assist" + (assists == 1 ? "" : "s") + ")";
//
//        String kd = new DecimalFormat("0.0").format(this.getStats().getKillDeathRatio());
//        String kda = new DecimalFormat("0.0").format(this.getStats().getKillDeathAssistRatio());
//        String kdaStr = assists > 0 ? ChatColor.GRAY + " (" + kda + " KDA)" : "";
//
//        int killStreak = this.killstreak;
//        int bestKillStreak = this.bestKillstreak;
//        String bestStreakStr = bestKillStreak > 0 && bestKillStreak > killStreak ? net.md_5.bungee.api.ChatColor.GRAY + " (Best " + bestKillStreak + ")" :"";
//
//        return ChatColor.GRAY + "Score: " + ChatColor.YELLOW + this.totalScore + ChatColor.DARK_GRAY + " | " + ChatColor.GRAY +
//            "Kills: " + ChatColor.GREEN + this.kills + assistsStr + ChatColor.DARK_GRAY + " | " + ChatColor.GRAY +
//            "Deaths: " + ChatColor.RED + this.deaths + ChatColor.DARK_GRAY + " | " + ChatColor.GRAY +
//            "KD: " + ChatColor.AQUA + kd + kdaStr + ChatColor.DARK_GRAY + " | " + ChatColor.GRAY +
//            "Streak: " + ChatColor.AQUA + killStreak + bestStreakStr;
    }

}
