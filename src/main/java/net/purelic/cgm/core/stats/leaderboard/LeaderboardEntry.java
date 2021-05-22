package net.purelic.cgm.core.stats.leaderboard;

import net.purelic.commons.utils.Fetcher;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class LeaderboardEntry {

    private final String uuid;
    private long value;

    public LeaderboardEntry(Map<String, Object> data) {
        this((String) data.get("uuid"), (long) data.get("value"));
    }

    public LeaderboardEntry(UUID uuid, int value) {
        this(uuid.toString(), value);
    }

    private LeaderboardEntry(String uuid, long value) {
        this.uuid = uuid;
        this.value = value;
    }

    public UUID getId() {
        return UUID.fromString(this.uuid);
    }

    public long getValue() {
        return this.value;
    }

    public void setValue(long value) {
        this.value = value;
    }

    public Map<String, Object> toData() {
        Map<String, Object> data = new HashMap<>();
        data.put("uuid", this.uuid);
        data.put("name", Fetcher.getNameOf(UUID.fromString(this.uuid)));
        data.put("value", this.value);
        return data;
    }

}
