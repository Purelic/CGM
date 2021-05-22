package net.purelic.cgm.listeners.flag;

import net.purelic.cgm.core.constants.MatchTeam;
import net.purelic.cgm.core.gamemodes.NumberSetting;
import net.purelic.cgm.core.gamemodes.ToggleSetting;
import net.purelic.cgm.core.managers.MatchManager;
import net.purelic.cgm.core.managers.ScoreboardManager;
import net.purelic.cgm.core.maps.flag.Flag;
import net.purelic.cgm.core.maps.flag.constants.FlagState;
import net.purelic.cgm.core.maps.flag.events.FlagDropEvent;
import net.purelic.cgm.core.match.Participant;
import net.purelic.cgm.events.participant.ParticipantRespawnEvent;
import net.purelic.cgm.utils.FlagUtils;
import net.purelic.commons.Commons;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class FlagDrop implements Listener {

    @EventHandler
    public void onFlagDrop(FlagDropEvent event) {
        Flag flag = event.getFlag();

        Player carrier = flag.getCarrier().getPlayer();
        MatchTeam team = MatchTeam.getTeam(carrier);
        Location location = carrier.getLocation();
        Location dropLocation = FlagUtils.getDropLocation(flag, location);

        if (dropLocation == null) {
            Location lastLocation = flag.getLastLocation();

            if (lastLocation != null && NumberSetting.FLAG_VOIDED_DELAY.value() == 0) {
                this.drop(event, lastLocation);
            } else {
                flag.setRespawnAtHome(lastLocation == null);
                flag.setState(FlagState.RESPAWNING);
            }
        } else {
            this.drop(event, dropLocation);
        }

        flag.clearCarrier();

        if (ToggleSetting.RESPAWN_ON_DROP.isEnabled()) {
            for (Player player : team.getPlayers()) {
                Participant participant = MatchManager.getParticipant(player);
                if (participant.isDead()) Commons.callEvent(new ParticipantRespawnEvent(participant, false));
            }
        }

        // Clears the green score color if applicable
        ScoreboardManager.updateSoloBoard();
        ScoreboardManager.updateTeamBoard();
    }

    private void drop(FlagDropEvent event, Location location) {
        if (NumberSetting.FLAG_COLLECTION_INTERVAL.value() == 0) {
            event.broadcast(); // don't broadcast when flags are collected
            // SoundUtils.SFX.FLAG_DROPPED.playAll();
        }

        event.getFlag().place(location, NumberSetting.FLAG_RESET_DELAY.value() == 0);
    }

}
