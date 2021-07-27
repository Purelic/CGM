package net.purelic.cgm.core.maps.flag.runnables;

import net.purelic.cgm.core.constants.MatchState;
import net.purelic.cgm.core.managers.MatchManager;
import net.purelic.cgm.core.maps.flag.Flag;
import net.purelic.cgm.core.maps.flag.constants.FlagState;
import net.purelic.cgm.core.match.Participant;
import org.bukkit.scheduler.BukkitRunnable;

public class FlagChecker extends BukkitRunnable {

    private final Flag flag;

    public FlagChecker(Flag flag) {
        this.flag = flag;
    }

    @Override
    public void run() {
        if (!MatchState.isState(MatchState.STARTED) || this.flag.isState(FlagState.RESPAWNING, FlagState.TAKEN)) {
            this.cancel();
            return;
        }

        for (Participant participant : MatchManager.getParticipants()) {
            if (this.flag.getHome().getWorld() != participant.getPlayer().getWorld()) {
                // TODO need to figure out what causes this
                System.err.println("ERROR: " + this.flag.getTitle() + " from previous match was not reset properly!");
                this.cancel();
                return;
            }

            boolean canTake = this.flag.canTake(participant);
            boolean touching = this.flag.isTouching(participant);

            if (!touching) continue;

            if (canTake) {
                this.flag.setCarrier(participant);
                this.cancel();
                break;
            } else if (this.flag.isState(FlagState.DROPPED) && this.flag.getOwner() == participant.getTeam()) {
                this.flag.startReturn();
            }
        }
    }

}
