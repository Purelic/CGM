package net.purelic.cgm.listeners.match;

import net.purelic.cgm.CGM;
import net.purelic.cgm.core.constants.JoinState;
import net.purelic.cgm.core.constants.MatchState;
import net.purelic.cgm.events.match.SpectatorJoinEvent;
import net.purelic.cgm.kit.ControlsKit;
import net.purelic.cgm.kit.SpectatorKit;
import net.purelic.cgm.utils.PlayerUtils;
import net.purelic.cgm.utils.SpawnUtils;
import net.purelic.commons.Commons;
import net.purelic.commons.utils.CommandUtils;
import net.purelic.commons.utils.ServerUtils;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class SpectatorJoin implements Listener {

    private final ControlsKit controlsKit;
    private final SpectatorKit spectatorKit;

    public SpectatorJoin() {
        this.controlsKit = new ControlsKit();
        this.spectatorKit = new SpectatorKit();
    }

    @EventHandler
    public void onSpectatorJoin(SpectatorJoinEvent event) {
        Player player = event.getPlayer();
        boolean initialJoin = event.isInitialJoin();

        if (MatchState.isActive() || MatchState.isState(MatchState.ENDED)) {
            if (!initialJoin) PlayerUtils.reset(player, GameMode.ADVENTURE);
            else PlayerUtils.reset(player, GameMode.SPECTATOR);

            if (MatchState.isState(MatchState.STARTED) && !initialJoin) {
                PlayerUtils.updateVisibility(player);
            }

            if (MatchState.isActive() && (!JoinState.isState(JoinState.LOCKED) || Commons.getProfile(player).isDonator() || ServerUtils.isPrivate())) {
                this.spectatorKit.apply(player);
            }
        } else {
            PlayerUtils.reset(player, GameMode.ADVENTURE);
        }

        if (CommandUtils.isOp(player) || Commons.getProfile(player).isMapDev()) this.controlsKit.apply(player);

        CGM.get().getMatchManager().removeParticipant(player);
        SpawnUtils.teleportObsSpawn(player);
    }

}
