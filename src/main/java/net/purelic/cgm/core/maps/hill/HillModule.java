package net.purelic.cgm.core.maps.hill;

import net.purelic.cgm.core.constants.MatchTeam;
import net.purelic.cgm.core.gamemodes.ToggleSetting;
import net.purelic.cgm.core.maps.hill.events.HillCaptureEvent;
import net.purelic.cgm.core.maps.hill.events.HillLostEvent;
import net.purelic.cgm.core.maps.hill.events.HillReclaimedEvent;
import net.purelic.cgm.events.match.RoundEndEvent;
import net.purelic.cgm.utils.HillUtils;
import net.purelic.cgm.utils.SoundUtils;
import net.purelic.commons.Commons;
import net.purelic.commons.modules.Module;
import org.bukkit.event.EventHandler;

public class HillModule implements Module {

    @EventHandler
    public void onHillCapture(HillCaptureEvent event) {
        MatchTeam team = event.getTeam();
        Hill hill = event.getHill();

        if (ToggleSetting.SINGLE_CAPTURE_HILLS.isEnabled()) {
            hill.setLocked(true);
            hill.destroyWaypoint();
        }

        if (ToggleSetting.ALL_HILLS_WIN.isEnabled() && this.allHillsCapturedBy(team)) {
            event.broadcast();
            Commons.callEvent(new RoundEndEvent(team));
            return;
        }

        if (team == MatchTeam.SOLO) return;

        event.broadcast();
        SoundUtils.SFX.HILL_CAPTURED.play(team);

        if (!hill.isNeutral()) SoundUtils.SFX.HILL_LOST.play(hill.getOwner());
    }

    private boolean allHillsCapturedBy(MatchTeam team) {
        return HillUtils.getHills().stream()
            .filter(Hill::isActive)
            .allMatch(hill -> hill.getCapturedBy() == team);
    }

    @EventHandler
    public void onHillLost(HillLostEvent event) {
        MatchTeam team = event.getTeam();
        if (team != MatchTeam.SOLO) event.broadcast(team);
    }

    @EventHandler
    public void onHillReclaimed(HillReclaimedEvent event) {
        Hill hill = event.getHill();
        if (!hill.isNeutral()) event.broadcast(hill.getOwner());
    }

}
