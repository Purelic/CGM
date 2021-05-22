package net.purelic.cgm.core.stats.leaderboard;

import net.purelic.commons.utils.DatabaseUtils;

import java.util.*;

public class Leaderboard {

    private final String id;
    private final String name;
    private final String valueLabel;
    private final long limit;
    private long minValue;
    private final List<LeaderboardEntry> entries;
    private final Map<UUID, LeaderboardEntry> leaders;

    public Leaderboard(String id, String name) {
        this.id = id;
        this.name = name;
        this.valueLabel = "Rating";
        this.limit = 50;
        this.minValue = 0;
        this.entries = new ArrayList<>();
        this.leaders = new HashMap<>();
    }

    public Leaderboard(String id, Map<String, Object> data) {
        this.id = id;
        this.name = (String) data.get("name");
        this.valueLabel = (String) data.get("value_label");
        this.limit = (long) data.get("limit");
        this.minValue = (long) data.get("min_value");
        this.entries = new ArrayList<>();
        this.leaders = new HashMap<>();
        this.loadLeaders((List<Map<String, Object>>) data.getOrDefault("leaders", new HashMap<>()));
    }

    private void loadLeaders(List<Map<String, Object>> leaders) {
        leaders.forEach(leader -> {
            LeaderboardEntry entry = new LeaderboardEntry(leader);
            this.entries.add(entry);
            this.leaders.put(entry.getId(), entry);
        });
    }

    public void updateLeaders(Map<UUID, Integer> stats) {
        List<LeaderboardEntry> copy = new ArrayList<>(this.entries);
        boolean updated = false;

        for (Map.Entry<UUID, Integer> e : stats.entrySet()) {
            UUID uuid = e.getKey();
            Integer value = e.getValue();

            if (this.leaders.containsKey(uuid)) {
                LeaderboardEntry entry = this.leaders.get(uuid);

                if (entry.getValue() != value) {
                    entry.setValue(value);
                    updated = true;
                }
            } else {
                LeaderboardEntry entry = new LeaderboardEntry(uuid, value);
                this.entries.add(entry);
                this.leaders.put(uuid, entry);
            }
        }

        this.sortLeaders();
        if (!copy.equals(this.entries) || updated) this.save();
    }

    private void sortLeaders() {
        // sort leaders by descending value
        this.entries.sort(Comparator.comparing(LeaderboardEntry::getValue));
        Collections.reverse(this.entries);

        // limit the number of leaders
        int size = this.entries.size();
        if (size > this.limit) {
            List<LeaderboardEntry> toRemove = this.entries.subList((int) this.limit, size);
            toRemove.forEach(entry -> this.leaders.remove(entry.getId()));
            toRemove.clear();
        }

        // update min value
        this.minValue = this.entries.get(size - 1).getValue();
    }

    private void save() {
        Map<String, Object> data = new HashMap<>();
        data.put("name", this.name);
        data.put("value_label", this.valueLabel);
        data.put("min_value", this.minValue);
        data.put("limit", this.limit);

        List<Map<String, Object>> leaders = new ArrayList<>();
        this.entries.forEach(leader -> leaders.add(leader.toData()));
        data.put("leaders", leaders);

        DatabaseUtils.getFirestore()
                .collection("leaderboards")
                .document(this.id).set(data);
    }

}
