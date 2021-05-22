package net.purelic.cgm.core.stats;

import net.purelic.cgm.core.constants.MatchTeam;
import net.purelic.cgm.core.match.Participant;
import net.purelic.cgm.core.rewards.Medal;
import net.purelic.cgm.core.stats.constants.KillType;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import shaded.com.google.cloud.Timestamp;

import java.util.HashMap;
import java.util.Map;

public class PlayerStats {

    private final Player player;
    private final MatchTeam team;
    private final Timestamp created;

    // Kills & Deaths
    private final KillStats killStats;
    private int assists;

    // Combat
    private double damageReceived;
    private double damageDealt;
    private int arrowsShot;
    private int arrowsHit;
    private double longestShot;
    private double longestKill;

    // Medals
    private final Map<Medal, Integer> medals;
    private int bestKillstreak;
    private int bestMultiKill;

    // Objectives
    private int score;
    private int beds;
    private int flags;
    private int headsCollected;
    private int headsStolen;
    private int headsRecovered;

    public PlayerStats(Player player) {
        this.player = player;
        this.team = MatchTeam.getTeam(player);
        this.created = Timestamp.now();
        this.killStats = new KillStats();
        this.assists = 0;
        this.damageReceived = 0;
        this.damageDealt = 0;
        this.arrowsShot = 0;
        this.arrowsHit = 0;
        this.longestShot = 0;
        this.longestKill = 0;
        this.medals = new HashMap<>();
        this.bestKillstreak = 0;
        this.bestMultiKill = 0;
        this.score = 0;
        this.beds = 0;
        this.flags = 0;
        this.headsCollected = 0;
        this.headsStolen = 0;
        this.headsCollected = 0;
    }

    public Player getPlayer() {
        return this.player;
    }

    public MatchTeam getTeam() {
        return this.team;
    }

    public Timestamp getCreated() {
        return this.created;
    }

    public KillStats getKillStats() {
        return killStats;
    }

    public int getAssists() {
        return this.assists;
    }

    public double getDamageReceived() {
        return this.damageReceived;
    }

    public double getDamageDealt() {
        return this.damageDealt;
    }

    public int getArrowsShot() {
        return this.arrowsShot;
    }

    public int getArrowsHit() {
        return this.arrowsHit;
    }

    public double getLongestShot() {
        return this.longestShot;
    }

    public double getLongestKill() {
        return this.longestKill;
    }

    public Map<Medal, Integer> getMedals() {
        return this.medals;
    }

    public int getBestKillstreak() {
        return this.bestKillstreak;
    }

    public int getBestMultiKill() {
        return this.bestMultiKill;
    }

    public int getScore() {
        return this.score;
    }

    public int getKills() {
        return this.killStats.getKills();
    }

    public int getBeds() {
        return this.beds;
    }

    public int getFlags() {
        return this.flags;
    }

    public int getHeadsCollected() {
        return this.headsCollected;
    }

    public int getHeadsStolen() {
        return this.headsStolen;
    }

    public int getHeadsRecovered() {
        return this.headsRecovered;
    }

    public double getKillDeathRatio() {
        return this.getRatio(this.getKills(), this.killStats.getDeaths());
    }

    public double getKillDeathAssistRatio() {
        return this.getRatio(this.getKills() + this.assists, this.killStats.getDeaths());
    }

    public double getFinalKillDeathRatio() {
        return this.getRatio(this.killStats.getFinalKills(), this.killStats.getFinalDeaths());
    }

    public double getArrowAccuracy() {
        return this.getRatio(this.arrowsHit, this.arrowsShot);
    }

    public double getNetDamage() {
        return this.damageDealt - this.damageReceived;
    }

    public double getNetDamageRatio() {
        return this.getRatio(this.damageDealt, this.damageReceived);
    }

    public void addKill(Participant killed, ItemStack weapon, boolean elimination) {
        this.addKill(killed.getPlayer(), weapon, elimination);
    }

    public void addKill(Player killed, ItemStack weapon, boolean elimination) {
        this.killStats.addKill(killed, KillType.getKillType(killed), weapon == null ? Material.AIR : weapon.getType(), elimination);
    }

    public void addDeath(Participant killer, boolean elimination) {
        this.killStats.addDeath(killer == null ? null : killer.getPlayer(), KillType.getKillType(this.player), elimination);
    }

    public void addAssist() {
        this.assists++;
    }

    public void addDamageReceived(double damage) {
        this.damageReceived += damage;
    }

    public void addDamageDealt(double damage) {
        this.damageDealt += damage;
    }

    public void addArrowShot() {
        this.arrowsShot++;
    }

    public void addArrowHit(double distance) {
        this.arrowsHit++;
        this.setLongestShot(distance);
    }

    private void setLongestShot(double distance) {
        this.longestShot = Math.max(this.longestShot, distance);
    }

    public void setLongestKill(double distance) {
        this.longestKill = Math.max(this.longestShot, distance);
    }

    public void addMedals(Map<Medal, Integer> medals) {
        medals.forEach((medal, amount) -> this.medals.merge(medal, amount, Integer::sum));
    }

    public void setBestKillstreak(int killstreak) {
        this.bestKillstreak = Math.max(this.bestKillstreak, killstreak);
    }

    public void setBestMultiKill(int multiKill) {
        this.bestMultiKill = Math.max(this.bestMultiKill, multiKill);
    }

    public void addScore(int score) {
        this.score += score;
    }

    public void addBed() {
        this.beds++;
    }

    public void addFlag() {
        this.flags++;
    }

    public void addHeadsCollected(int heads) {
        this.headsCollected += heads;
    }

    public void addHeadsStolen(int heads) {
        this.headsStolen += heads;
    }

    public void addHeadsRecovered(int heads) {
        this.headsRecovered += heads;
    }

    private double getRatio(double numerator, double denominator) {
        return denominator == 0 ? numerator : numerator / denominator;
    }

}
