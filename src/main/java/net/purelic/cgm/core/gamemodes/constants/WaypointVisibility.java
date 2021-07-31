package net.purelic.cgm.core.gamemodes.constants;

import net.purelic.cgm.core.constants.MatchTeam;
import net.purelic.cgm.core.match.Participant;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

@Deprecated
public enum WaypointVisibility {

    NONE,
    TEAM_ONLY,
    ENEMY_ONLY,
    EVERYONE,
    ;

    public Collection<? extends Player> getPlayers(Participant participant) {
        switch (this) {
            case TEAM_ONLY:
                return new HashSet<>(participant.getTeam().getPlayers());
            case ENEMY_ONLY:
                return new HashSet<>(this.getEnemyPlayers(participant.getTeam()));
            case EVERYONE:
                return Bukkit.getOnlinePlayers();
        }

        return new HashSet<>();
    }

    private Set<Player> getEnemyPlayers(MatchTeam team) {
        Set<Player> players = new HashSet<>();

        for (MatchTeam matchTeam : MatchTeam.values()) {
            if (team == matchTeam) continue;
            players.addAll(team.getPlayers());
        }

        return players;
    }

}
