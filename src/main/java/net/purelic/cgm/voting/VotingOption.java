package net.purelic.cgm.voting;

import net.purelic.cgm.core.gamemodes.CustomGameMode;
import net.purelic.cgm.core.maps.CustomMap;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class VotingOption {

    private final String id;
    private final CustomMap map;
    private final CustomGameMode gameMode;
    private final List<UUID> voters;

    public VotingOption(CustomMap map, CustomGameMode gameMode) {
        this.id = UUID.randomUUID().toString();
        this.map = map;
        this.gameMode = gameMode;
        this.voters = new ArrayList<>();
    }

    public String getId() {
        return this.id;
    }

    public CustomMap getMap() {
        return this.map;
    }

    public CustomGameMode getGameMode() {
        return this.gameMode;
    }

    public int getVotes() {
        return this.voters.size();
    }

    public void vote(Player player) {
        this.voters.add(player.getUniqueId());
    }

    public void unvote(Player player) {
        this.voters.remove(player.getUniqueId());
    }

    public boolean voted(Player player) {
        return this.voters.contains(player.getUniqueId());
    }

    public void clearVotes() {
        this.voters.clear();
    }

}
