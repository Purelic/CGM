package net.purelic.cgm.listeners.flag;

import net.purelic.cgm.core.constants.MatchTeam;
import net.purelic.cgm.core.gamemodes.ToggleSetting;
import net.purelic.cgm.core.maps.flag.Flag;
import net.purelic.cgm.core.maps.flag.constants.FlagState;
import net.purelic.cgm.core.maps.flag.events.FlagCaptureEvent;
import net.purelic.cgm.utils.SoundUtils;
import net.purelic.cgm.utils.SpawnUtils;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class FlagCapture implements Listener {

    @EventHandler
    public void onFlagCapture(FlagCaptureEvent event) {
        Flag flag = event.getFlag();

        if (flag.hasCarrier()) {
            event.broadcast();

            Player carrier = flag.getCarrier().getPlayer();
            MatchTeam team = MatchTeam.getTeam(carrier);

            if (team == MatchTeam.SOLO) SoundUtils.SFX.FLAG_CAPTURED.play(carrier);
            else SoundUtils.SFX.FLAG_CAPTURED.play(team);

            if (ToggleSetting.TELEPORT_ON_CAPTURE.isEnabled()) {
                SpawnUtils.teleportRandom(carrier, false);
            }
        }

        flag.setState(FlagState.RESPAWNING);
    }

}
