package net.purelic.cgm.core.maps.flag.runnables;

import net.purelic.cgm.core.gamemodes.NumberSetting;
import net.purelic.cgm.core.managers.MatchManager;
import net.purelic.cgm.core.maps.ProgressBar;
import net.purelic.cgm.core.maps.flag.Flag;
import net.purelic.cgm.core.maps.flag.constants.FlagState;
import net.purelic.cgm.core.maps.flag.events.FlagEvent;
import net.purelic.cgm.core.match.Participant;
import net.purelic.commons.utils.ChatUtils;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class FlagReturnChecker extends BukkitRunnable {

    private final Flag flag;
    private final int delay;
    private final double multiplier;
    private float progress;
    private int tick;
    private Set<Participant> tempParticipants;

    public FlagReturnChecker(Flag flag) {
        this.flag = flag;
        this.delay = NumberSetting.FLAG_RETURN_DELAY.value();
        this.multiplier = NumberSetting.FLAG_RETURN_MULTIPLIER.value();
        this.progress = 0;
        this.tick = 0;
        this.tempParticipants = new HashSet<>();
    }

    @Override
    public void run() {
        this.tick++;

        Set<Participant> participants = this.getReturningParticipants();

        if (participants.isEmpty() || !this.flag.isState(FlagState.DROPPED)) {
            participants.forEach(participant -> ChatUtils.sendActionBar(participant.getPlayer(), ""));
            this.cancel();
        } else if (this.progress >= this.delay) {
            this.flag.setState(FlagState.RESPAWNING);
            participants.forEach(participant ->
                    ChatUtils.sendActionBar(participant.getPlayer(),""));
            this.cancel();
        } else {
            double bonus = (0.1 * (this.multiplier / 100)) * (participants.size() - 1);
            this.progress += (0.1 + bonus);

            String barTitle = FlagEvent.BROADCAST_PREFIX + "Returning " + this.flag.getColoredName();
            float percent = this.progress / this.delay;

            new ProgressBar(barTitle, percent, this.tick % 4 == 0).sendBar(participants);
        }

        // clear the action bar for participants who are no longer returning the flag
        this.tempParticipants.stream()
                .filter(participant -> !participants.contains(participant))
                .forEach(participant -> ChatUtils.sendActionBar(participant.getPlayer(), ""));

        this.tempParticipants = participants;
    }

    private Set<Participant> getReturningParticipants() {
        return MatchManager.getParticipants().stream().filter(this.flag::isTouching).collect(Collectors.toSet());
    }

}
