package net.purelic.cgm.core.stats;

import net.purelic.cgm.core.constants.MatchTeam;
import net.purelic.cgm.core.match.Participant;
import net.purelic.cgm.core.stats.constants.MatchResult;
import net.purelic.cgm.utils.PlaceUtils;
import org.bukkit.entity.Player;

public class MatchPlacement {

    private final Player player;
    private final MatchTeam team;
    private final int place;
    private final String suffix;
    private final boolean tied;
    private final MatchResult result;

    public MatchPlacement(Participant participant, int place, boolean tied, MatchResult result) {
        this.player = participant.getPlayer();
        this.team = MatchTeam.getTeam(participant);
        this.place = place;
        this.suffix = PlaceUtils.getPlaceSuffix(place);
        this.tied = tied;
        this.result = result;
    }

    public Player getPlayer() {
        return this.player;
    }

    public MatchTeam getTeam() {
        return this.team;
    }

    public int getPlace() {
        return this.place;
    }

    public String getSuffix() {
        return this.suffix;
    }

    public boolean isTied() {
        return this.tied;
    }

    public MatchResult getStatResult() {
        return this.result;
    }

    public MatchResult getRealResult() {
        return this.tied && this.place == 1 ? MatchResult.DRAW : this.place == 1 ? MatchResult.WIN : MatchResult.LOSS;
    }

}
