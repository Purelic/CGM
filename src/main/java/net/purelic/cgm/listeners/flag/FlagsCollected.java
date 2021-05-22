package net.purelic.cgm.listeners.flag;

import net.purelic.cgm.core.constants.MatchTeam;
import net.purelic.cgm.core.maps.flag.Flag;
import net.purelic.cgm.core.maps.flag.constants.FlagState;
import net.purelic.cgm.core.maps.flag.events.FlagsCollectedEvent;
import net.purelic.cgm.utils.SoundUtils;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.Set;

public class FlagsCollected implements Listener {

    @EventHandler
    public void onFlagsCollected(FlagsCollectedEvent event) {
        MatchTeam team = event.getTeam();
        Set<Flag> flags = event.getFlags();
        int collected = flags.size();

        if (collected > 0) {
            event.broadcast();
            team.addScore(collected);
            SoundUtils.SFX.FLAG_CAPTURED.play(team);
            flags.forEach(flag -> flag.setState(FlagState.CAPTURED));
        }
    }

}
