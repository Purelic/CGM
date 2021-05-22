package net.purelic.cgm.core.maps;

import net.purelic.cgm.core.constants.MatchTeam;

public interface Objective {

    MatchTeam getOwner();

    boolean isLoaded();

    void reset();

}
