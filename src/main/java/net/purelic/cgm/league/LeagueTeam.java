package net.purelic.cgm.league;

import net.purelic.cgm.core.constants.MatchTeam;
import net.purelic.commons.Commons;

import java.util.List;
import java.util.UUID;

public class LeagueTeam {

    private final MatchTeam matchTeam;
    private final List<UUID> players;
    private final String name;

    public LeagueTeam(MatchTeam matchTeam, List<UUID> players) {
        this(matchTeam, players, null);
    }

    public LeagueTeam(MatchTeam matchTeam, List<UUID> players, String name) {
        this.matchTeam = matchTeam;
        this.players = players;
        this.name = name;
    }

    public MatchTeam getMatchTeam() {
        return matchTeam;
    }

    public List<UUID> getPlayers() {
        return this.players;
    }

    public void addPlayers(List<UUID> players) {
        this.players.addAll(players);
    }

    public String getName() {
        return this.name;
    }

    public int getTotalRating() {
        return this.players.stream().mapToInt(player -> Commons.getProfile(player).getRating()).sum();
    }

    public double getAverageRating() {
        return this.getTotalRating() / (double) this.players.size();
    }

}
