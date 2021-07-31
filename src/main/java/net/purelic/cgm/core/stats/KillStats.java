package net.purelic.cgm.core.stats;

import net.purelic.cgm.core.stats.constants.KillType;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.stream.Collectors;

public class KillStats {

    private int kills;
    private int finalKills;
    private int deaths;
    private int finalDeaths;
    private int suicides;
    private final Map<Player, Integer> killed;
    private final Map<Player, Integer> killedBy;
    private final Map<KillType, Integer> killTypes;
    private final Map<KillType, Integer> deathTypes;
    private final Map<Material, Integer> weapons;

    public KillStats() {
        this.kills = 0;
        this.finalKills = 0;
        this.deaths = 0;
        this.finalDeaths = 0;
        this.suicides = 0;
        this.killed = new HashMap<>();
        this.killedBy = new HashMap<>();
        this.killTypes = new HashMap<>();
        this.deathTypes = new HashMap<>();
        this.weapons = new HashMap<>();
    }

    public int getKills() {
        return this.kills;
    }

    public int getFinalKills() {
        return this.finalKills;
    }

    public int getDeaths() {
        return this.deaths;
    }

    public int getFinalDeaths() {
        return this.finalDeaths;
    }

    public int getSuicides() {
        return this.suicides;
    }

    public Map<Player, Integer> getKilled() {
        return this.getOrdered(this.killed);
    }

    public Player getKilledMost() {
        return this.getMaxKey(this.killed);
    }

    public Map<Player, Integer> getKilledBy() {
        return this.getOrdered(this.killedBy);
    }

    public Player getKilledByMost() {
        return this.getMaxKey(this.killedBy);
    }

    public Map<KillType, Integer> getKillTypes() {
        return this.killTypes;
    }

    public Map<KillType, Integer> getDeathTypes() {
        return this.deathTypes;
    }

    public Map<Material, Integer> getWeapons() {
        return this.weapons;
    }

    public void addKill(Player killed, KillType killType, Material weapon, boolean elimination) {
        this.kills++;
        this.increment(this.killed, killed);
        this.increment(this.killTypes, killType);
        if (killType == KillType.MELEE) this.increment(this.weapons, weapon);
        if (elimination) this.finalKills++;
    }

    public void addDeath(Player killer, KillType killType, boolean elimination) {
        this.deaths++;
        if (killer == null) this.suicides++;
        else this.increment(this.killedBy, killer);
        this.increment(this.deathTypes, killType);
        if (elimination) this.finalDeaths++;
    }

    private <K> void increment(Map<K, Integer> map, K key) {
        map.merge(key, 1, Integer::sum);
    }

    private <K> K getMaxKey(Map<K, Integer> map) {
        if (map.isEmpty()) return null;
        return Collections.max(map.entrySet(), Comparator.comparingInt(Map.Entry::getValue)).getKey();
    }

    private Map<Player, Integer> getOrdered(Map<Player, Integer> map) {
        return map.entrySet().stream()
            .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));
    }

}
