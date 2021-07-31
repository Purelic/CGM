package net.purelic.cgm.core.match;

import net.purelic.cgm.CGM;
import net.purelic.cgm.core.constants.MatchState;
import net.purelic.cgm.core.constants.MatchTeam;
import net.purelic.cgm.core.gamemodes.EnumSetting;
import net.purelic.cgm.core.gamemodes.NumberSetting;
import net.purelic.cgm.core.gamemodes.constants.GameType;
import net.purelic.cgm.core.managers.TabManager;
import net.purelic.cgm.core.match.constants.ParticipantState;
import net.purelic.cgm.core.runnables.RoundCountdown;
import net.purelic.cgm.core.stats.PlayerStats;
import net.purelic.cgm.events.participant.ParticipantScoreEvent;
import net.purelic.cgm.listeners.match.MatchEnd;
import net.purelic.cgm.listeners.modules.stats.MatchStatsModule;
import net.purelic.cgm.utils.MatchUtils;
import net.purelic.commons.Commons;
import net.purelic.commons.utils.ChatUtils;
import net.purelic.commons.utils.TaskUtils;
import net.purelic.commons.utils.VersionUtils;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.text.DecimalFormat;
import java.util.Arrays;

public class Participant {

    private final Player player;
    private ParticipantState state;
    private int roundsWon;
    private int totalScore;
    private int score;
    private boolean dead;
    private int lives;
    private int kills;
    private int deaths;
    private int killstreak;
    private int bestKillstreak;
    private boolean queued;
    private int multiKill;
    private int longestShot;
    private BukkitRunnable multiKillTimer;

    private PlayerStats stats;

    public Participant(Player player) {
        this.player = player;
        this.state = ParticipantState.ALIVE;

        if (MatchStatsModule.hasStats(player)) {
            this.stats = MatchStatsModule.getStats(player);
            resetLives();

            if (NumberSetting.ROUNDS.value() > 1) {
                this.score = 0;
            } else {
                this.score = this.stats.getScore();
            }

            this.kills = this.stats.getKills();
            this.totalScore = this.stats.getScore();
            this.dead = false;
            this.deaths = this.stats.getKillStats().getDeaths();
            this.killstreak = 0;
            this.roundsWon = 0;
            this.queued = false;
            this.multiKill = 0;
            this.longestShot = (int) this.stats.getLongestKill();
            this.multiKillTimer = null;
        } else {
            this.reset();
            this.stats = MatchStatsModule.getStats(player);
        }
    }

    public void setState(ParticipantState state) {
        this.state = state;
    }

    public boolean isState(ParticipantState... states) {
        return Arrays.asList(states).contains(this.state);
    }

    public PlayerStats getStats() {
        if (this.stats == null) this.stats = MatchStatsModule.getStats(this.player);
        return this.stats;
    }

    public String getTabStats() {
        int assists = this.getStats().getAssists();
        String assistsStr = assists == 0 ? "" : ChatColor.GRAY + " (" + assists + " Assist" + (assists == 1 ? "" : "s") + ")";

        String kd = new DecimalFormat("0.0").format(this.getStats().getKillDeathRatio());
        String kda = new DecimalFormat("0.0").format(this.getStats().getKillDeathAssistRatio());
        String kdaStr = assists > 0 ? ChatColor.GRAY + " (" + kda + " KDA)" : "";

        int killStreak = this.killstreak;
        int bestKillStreak = this.bestKillstreak;
        String bestStreakStr = bestKillStreak > 0 && bestKillStreak > killStreak ? net.md_5.bungee.api.ChatColor.GRAY + " (Best " + bestKillStreak + ")" : "";

        return ChatColor.GRAY + "Score: " + ChatColor.YELLOW + this.totalScore + ChatColor.DARK_GRAY + " | " + ChatColor.GRAY +
            "Kills: " + ChatColor.GREEN + this.kills + assistsStr + ChatColor.DARK_GRAY + " | " + ChatColor.GRAY +
            "Deaths: " + ChatColor.RED + this.deaths + ChatColor.DARK_GRAY + " | " + ChatColor.GRAY +
            "KD: " + ChatColor.AQUA + kd + kdaStr + ChatColor.DARK_GRAY + " | " + ChatColor.GRAY +
            "Streak: " + ChatColor.AQUA + killStreak + bestStreakStr;
    }

    public Player getPlayer() {
        return this.player;
    }

    public int getTotalScore() {
        return this.totalScore;
    }

    public int getFinalScore() {
        if (MatchUtils.isElimination() && !MatchUtils.hasKillScoring()) return this.getEliminatedScore();
        return this.totalScore;
    }

    public int getScore() {
        return this.score;
    }

    public void addScore(int score) {
        this.addScore(score, false);
    }

    public void addScore(int score, boolean skipEvent) {
        if (!MatchState.isState(MatchState.STARTED) || TaskUtils.isRunning(RoundCountdown.getCountdown())) {
            return;
        }

        this.score += score;
        this.totalScore += score;
        this.getStats().addScore(score);
        TabManager.updateScore(this);
        if (!skipEvent) Commons.callEvent(new ParticipantScoreEvent(this, score));
    }

    public boolean isAlive() {
        return !(this.isDead() || this.isEliminated());
    }

    public boolean isDead() {
        return this.dead;
    }

    public boolean isEliminated() {
        return NumberSetting.LIVES_PER_ROUND.value() != 0 && this.lives == 0;
    }

    public void setDead(boolean dead) {
        this.dead = dead;
        if (dead) {
            if (!this.isQueued()) this.addDeath();
            if (this.lives != -1 && NumberSetting.LIVES_PER_ROUND.value() > 0) this.lives--;
        }
    }

    public void resetLives() {
        if (EnumSetting.GAME_TYPE.is(GameType.BED_WARS)) this.lives = -1;
        else this.lives = NumberSetting.LIVES_PER_ROUND.value();
    }

    public void setLives(int lives) {
        this.lives = lives;
    }

    public int getLives() {
        return this.lives;
    }

    public void addKill() {
        this.kills++;
        this.killstreak++;
        this.addMultiKill();
        this.player.playSound(this.player.getLocation(), Sound.LEVEL_UP, 10.0F, 2.0F);
        this.bestKillstreak = Math.max(this.bestKillstreak, this.killstreak);

        this.getStats().setBestKillstreak(this.killstreak);
        this.getStats().setBestMultiKill(this.multiKill);

        TabManager.updateKills(this);
        this.sendStatsMessage();
    }

    private void addMultiKill() {
        this.multiKill++;

        if (TaskUtils.isRunning(this.multiKillTimer)) {
            this.multiKillTimer.cancel();
        }

        this.multiKillTimer = new BukkitRunnable() {
            @Override
            public void run() {
                multiKill = 0;
            }
        };

        this.multiKillTimer.runTaskLater(CGM.get(), 200);
    }

    public int getMultiKill() {
        return this.multiKill;
    }

    private void addDeath() {
        this.deaths++;
        this.killstreak = 0;
        this.multiKill = 0;
        TabManager.updateDeaths(this);
        this.sendStatsMessage();
    }

    public int getKills() {
        return this.kills;
    }

    public void addShot(double dist) {
        this.longestShot = Math.max((int) (dist + 0.5), this.longestShot);
    }

    public int getLongestShot() {
        return this.longestShot;
    }

    public int getDeaths() {
        return this.deaths;
    }

    public int getKillstreak() {
        return this.killstreak;
    }

    public int getBestKillstreak() {
        return this.bestKillstreak;
    }

    public int getRoundsWon() {
        return this.roundsWon;
    }

    public void addRoundWin() {
        this.roundsWon++;
    }

    public boolean isQueued() {
        return this.isState(ParticipantState.QUEUED);
    }

    public void setQueued(boolean queued) {
        this.queued = queued;
    }

    public MatchTeam getTeam() {
        return MatchTeam.getTeam(this);
    }

    private void sendStatsMessage() {
        String message = "" + ChatColor.GREEN + this.kills + ChatColor.WHITE + " Kill" + (this.kills == 1 ? "" : "s") +
            ChatColor.DARK_GRAY + " | " + ChatColor.AQUA + this.killstreak + ChatColor.WHITE + " Streak" +
            ChatColor.DARK_GRAY + " | " + ChatColor.RED + this.deaths + ChatColor.WHITE + " Death" + (this.deaths == 1 ? "" : "s");

        if (VersionUtils.isLegacy(this.player)) this.player.sendMessage(message);
        else ChatUtils.sendActionBar(this.player, message);
    }

    public int getEliminatedScore() {
        int score = MatchEnd.ELIMINATED_PLAYERS.indexOf(this);
        return score == -1 ? MatchEnd.ELIMINATED_PLAYERS.size() : score;
    }

    public void resetScore() {
        this.score = 0;
    }

    public void reset() {
        this.state = ParticipantState.ALIVE;
        this.score = 0;
        resetLives();
        this.totalScore = 0;
        this.dead = false;
        this.kills = 0;
        this.deaths = 0;
        this.killstreak = 0;
        this.roundsWon = 0;
        this.queued = false;
        this.multiKill = 0;
        this.longestShot = 0;
        this.multiKillTimer = null;
        this.stats = null;
    }

}
