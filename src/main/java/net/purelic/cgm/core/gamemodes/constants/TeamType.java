package net.purelic.cgm.core.gamemodes.constants;

import net.purelic.cgm.core.constants.MatchTeam;
import org.apache.commons.lang.ArrayUtils;

import java.util.Arrays;
import java.util.List;

public enum TeamType {

    // TODO use parent team to simplify the arrayutil.addall

    SOLO("Solo", 16, MatchTeam.SOLO),
    TEAMS("Teams", 8, MatchTeam.BLUE, MatchTeam.RED),
    MULTI_TEAM("Multi-Team", 4, (MatchTeam[]) ArrayUtils.addAll(TEAMS.teams.toArray(), new MatchTeam[] { MatchTeam.GREEN, MatchTeam.YELLOW })),
    SQUADS("Squads", 2, (MatchTeam[]) ArrayUtils.addAll(MULTI_TEAM.teams.toArray(), new MatchTeam[] { MatchTeam.AQUA, MatchTeam.PINK, MatchTeam.GRAY, MatchTeam.WHITE }));

    private final String name;
    private final int size;
    private final List<MatchTeam> teams;


    TeamType(String name, int size, MatchTeam... teams) {
        this.name = name;
        this.size = size;
        this.teams = Arrays.asList(teams);
    }

    public String getName() {
        return this.name;
    }

    public int getSize() {
        return this.size;
    }

    public List<MatchTeam> getTeams() {
        return this.teams;
    }

}
